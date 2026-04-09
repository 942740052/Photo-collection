import { useState, useCallback } from 'react'
import { supabase } from '../supabase/client'

interface Album {
  id: string
  user_id: string
  name: string
  description: string
  cover_photo_id: string | null
  created_at: string
}

export const useAlbums = (userId: string | undefined) => {
  const [albums, setAlbums] = useState<Album[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // 获取用户相册列表
  const fetchAlbums = useCallback(async () => {
    if (!userId) return

    setLoading(true)
    setError(null)

    try {
      const { data, error } = await supabase
        .from('albums')
        .select('*')
        .eq('user_id', userId)
        .order('created_at', { ascending: false })

      if (error) throw error
      setAlbums(data || [])
    } catch (err) {
      setError('获取相册失败')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 创建相册
  const createAlbum = useCallback(async (name: string, description: string, coverPhotoId: string | null) => {
    if (!userId) return { error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      const { data, error: dbError } = await supabase
        .from('albums')
        .insert({
          user_id: userId,
          name,
          description,
          cover_photo_id: coverPhotoId
        })
        .select()
        .single()

      if (dbError) throw dbError

      // 更新相册列表
      setAlbums(prev => [data as Album, ...prev])
      return { data: data as Album, error: null }
    } catch (err) {
      setError('创建相册失败')
      console.error(err)
      return { data: null, error: '创建相册失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 删除相册
  const deleteAlbum = useCallback(async (albumId: string) => {
    if (!userId) return { error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      // 从数据库中删除相册记录
      const { error: dbError } = await supabase
        .from('albums')
        .delete()
        .eq('id', albumId)
        .eq('user_id', userId)

      if (dbError) throw dbError

      // 更新相册列表
      setAlbums(prev => prev.filter(album => album.id !== albumId))
      return { error: null }
    } catch (err) {
      setError('删除相册失败')
      console.error(err)
      return { error: '删除相册失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 更新相册信息
  const updateAlbum = useCallback(async (albumId: string, updates: Partial<Album>) => {
    if (!userId) return { error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      const { data, error: dbError } = await supabase
        .from('albums')
        .update(updates)
        .eq('id', albumId)
        .eq('user_id', userId)
        .select()
        .single()

      if (dbError) throw dbError

      // 更新相册列表
      setAlbums(prev => prev.map(album => album.id === albumId ? data as Album : album))
      return { data: data as Album, error: null }
    } catch (err) {
      setError('更新相册失败')
      console.error(err)
      return { data: null, error: '更新相册失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 添加照片到相册
  const addPhotoToAlbum = useCallback(async (albumId: string, photoId: string) => {
    if (!userId) return { error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      // 检查相册是否属于当前用户
      const { data: album, error: albumError } = await supabase
        .from('albums')
        .select('id')
        .eq('id', albumId)
        .eq('user_id', userId)
        .single()

      if (albumError || !album) {
        throw new Error('相册不存在或无权限')
      }

      // 检查照片是否属于当前用户
      const { data: photo, error: photoError } = await supabase
        .from('photos')
        .select('id')
        .eq('id', photoId)
        .eq('user_id', userId)
        .single()

      if (photoError || !photo) {
        throw new Error('照片不存在或无权限')
      }

      // 检查照片是否已在相册中
      const { data: existing, error: existingError } = await supabase
        .from('album_photos')
        .select('id')
        .eq('album_id', albumId)
        .eq('photo_id', photoId)
        .single()

      if (!existingError) {
        throw new Error('照片已在相册中')
      }

      // 添加照片到相册
      const { error: dbError } = await supabase
        .from('album_photos')
        .insert({
          album_id: albumId,
          photo_id: photoId
        })

      if (dbError) throw dbError

      return { error: null }
    } catch (err) {
      setError('添加照片到相册失败')
      console.error(err)
      return { error: '添加照片到相册失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 从相册移除照片
  const removePhotoFromAlbum = useCallback(async (albumId: string, photoId: string) => {
    if (!userId) return { error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      // 检查相册是否属于当前用户
      const { data: album, error: albumError } = await supabase
        .from('albums')
        .select('id')
        .eq('id', albumId)
        .eq('user_id', userId)
        .single()

      if (albumError || !album) {
        throw new Error('相册不存在或无权限')
      }

      // 从相册移除照片
      const { error: dbError } = await supabase
        .from('album_photos')
        .delete()
        .eq('album_id', albumId)
        .eq('photo_id', photoId)

      if (dbError) throw dbError

      return { error: null }
    } catch (err) {
      setError('从相册移除照片失败')
      console.error(err)
      return { error: '从相册移除照片失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  // 获取相册中的照片
  const getAlbumPhotos = useCallback(async (albumId: string) => {
    if (!userId) return { data: [], error: '用户未登录' }

    setLoading(true)
    setError(null)

    try {
      // 检查相册是否属于当前用户
      const { data: album, error: albumError } = await supabase
        .from('albums')
        .select('id')
        .eq('id', albumId)
        .eq('user_id', userId)
        .single()

      if (albumError || !album) {
        throw new Error('相册不存在或无权限')
      }

      // 获取相册中的照片
      const { data, error: dbError } = await supabase
        .from('album_photos')
        .select('photo_id, photos(*)')
        .eq('album_id', albumId)

      if (dbError) throw dbError

      const photos = data.map((item: any) => item.photos)
      return { data: photos, error: null }
    } catch (err) {
      setError('获取相册照片失败')
      console.error(err)
      return { data: [], error: '获取相册照片失败' }
    } finally {
      setLoading(false)
    }
  }, [userId])

  return { 
    albums, 
    loading, 
    error, 
    fetchAlbums, 
    createAlbum, 
    deleteAlbum, 
    updateAlbum, 
    addPhotoToAlbum, 
    removePhotoFromAlbum, 
    getAlbumPhotos 
  }
}