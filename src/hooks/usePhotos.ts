import { useState, useCallback } from 'react'
import { supabase } from '../supabase/client'

interface Photo {
  id: string
  user_id: string
  filename: string
  file_path: string
  title: string
  description: string
  uploaded_at: string
  tags: string[]
}

export const usePhotos = (userId: string | undefined) => {
  const [photos, setPhotos] = useState<Photo[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // 获取用户照片列表
  const fetchPhotos = useCallback(async () => {
    if (!userId) return

    setLoading(true)
    setError(null)

    try {
      const { data, error } = await supabase
        .from('photos')
        .select('*')
        .eq('user_id', userId)
        .order('uploaded_at', { ascending: false })

      if (error) throw error
      setPhotos(data || [])
    } catch (err) {
      setError('获取照片失败')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 上传照片
  const uploadPhoto = useCallback(async (file: File, title: string, description: string, tags: string[]) => {
    if (!userId) return { error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      // 生成唯一文件名
      const fileName = `${Date.now()}-${file.name}`
      const filePath = `photos/${userId}/${fileName}`

      // 上传文件到Supabase Storage
      const { error: uploadError } = await supabase.storage
        .from('photos')
        .upload(filePath, file)

      if (uploadError) throw uploadError

      // 获取文件URL
      const { data: { publicUrl } } = supabase.storage
        .from('photos')
        .getPublicUrl(filePath)

      // 保存照片信息到数据库
      const { data, error: dbError } = await supabase
        .from('photos')
        .insert({
          user_id: userId,
          filename: file.name,
          file_path: publicUrl,
          title,
          description,
          tags
        })
        .select()
        .single()

      if (dbError) throw dbError

      // 更新照片列表
      setPhotos(prev => [data as Photo, ...prev])
      return { data: data as Photo, error: null }
    } catch (err) {
      setError('上传照片失败')
      console.error(err)
      return { data: null, error: '上传照片失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 删除照片
  const deletePhoto = useCallback(async (photoId: string) => {
    if (!userId) return { error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      // 从数据库中删除照片记录
      const { error: dbError } = await supabase
        .from('photos')
        .delete()
        .eq('id', photoId)
        .eq('user_id', userId)

      if (dbError) throw dbError

      // 更新照片列表
      setPhotos(prev => prev.filter(photo => photo.id !== photoId))
      return { error: null }
    } catch (err) {
      setError('删除照片失败')
      console.error(err)
      return { error: '删除照片失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 更新照片信息
  const updatePhoto = useCallback(async (photoId: string, updates: Partial<Photo>) => {
    if (!userId) return { error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      const { data, error: dbError } = await supabase
        .from('photos')
        .update(updates)
        .eq('id', photoId)
        .eq('user_id', userId)
        .select()
        .single()

      if (dbError) throw dbError

      // 更新照片列表
      setPhotos(prev => prev.map(photo => photo.id === photoId ? data as Photo : photo))
      return { data: data as Photo, error: null }
    } catch (err) {
      setError('更新照片失败')
      console.error(err)
      return { data: null, error: '更新照片失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  return { photos, loading, error, fetchPhotos, uploadPhoto, deletePhoto, updatePhoto }
}