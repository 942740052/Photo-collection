import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { usePhotos } from '../hooks/usePhotos'
import Navbar from '../components/Navbar'
import { Trash2, ArrowLeft, Share2, Edit2, Check, X } from 'lucide-react'

const PhotoDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const { user, loading: authLoading } = useAuth()
  const { photos, fetchPhotos, deletePhoto, updatePhoto, loading } = usePhotos(user?.id)
  const navigate = useNavigate()
  const [photo, setPhoto] = useState<any>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [tags, setTags] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (!authLoading && !user) {
      navigate('/login')
    } else if (user) {
      fetchPhotos()
    }
  }, [authLoading, user, navigate, fetchPhotos])

  useEffect(() => {
    if (id && photos.length > 0) {
      const foundPhoto = photos.find((p: any) => p.id === id)
      if (foundPhoto) {
        setPhoto(foundPhoto)
        setTitle(foundPhoto.title || '')
        setDescription(foundPhoto.description || '')
        setTags(foundPhoto.tags?.join(', ') || '')
      }
    }
  }, [id, photos])

  const handleDelete = async () => {
    if (window.confirm('确定要删除这张照片吗？')) {
      await deletePhoto(id!)
      navigate('/')
    }
  }

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    // 处理标签输入，将逗号分隔的标签转换为数组
    const tagsArray = tags
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag !== '')

    try {
      await updatePhoto(id!, {
        title,
        description,
        tags: tagsArray
      })
      setSuccess('照片信息更新成功！')
      setIsEditing(false)
    } catch (err) {
      setError('更新失败，请重试')
      console.error(err)
    }
  }

  if (authLoading || loading) {
    return <div className="min-h-screen flex items-center justify-center">加载中...</div>
  }

  if (!user || !photo) {
    return <div className="min-h-screen flex items-center justify-center">照片不存在</div>
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-12">
        <div className="mb-6">
          <Link
            to="/"
            className="flex items-center text-gray-700 hover:text-blue-500 mb-4"
          >
            <ArrowLeft className="h-5 w-5 mr-2" />
            返回照片列表
          </Link>

          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          {success && (
            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
              {success}
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* 照片预览 */}
          <div className="bg-white rounded-lg shadow-md overflow-hidden">
            <img
              src={photo.file_path}
              alt={photo.title || photo.filename}
              className="w-full h-auto object-contain"
            />
          </div>

          {/* 照片信息 */}
          <div className="bg-white rounded-lg shadow-md p-6">
            {isEditing ? (
              <form onSubmit={handleUpdate}>
                <div className="mb-4">
                  <label htmlFor="title" className="block text-gray-700 mb-2">标题</label>
                  <input
                    type="text"
                    id="title"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="照片标题"
                  />
                </div>

                <div className="mb-4">
                  <label htmlFor="tags" className="block text-gray-700 mb-2">标签</label>
                  <input
                    type="text"
                    id="tags"
                    value={tags}
                    onChange={(e) => setTags(e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="用逗号分隔，如：风景, 旅行, 朋友"
                  />
                </div>

                <div className="mb-6">
                  <label htmlFor="description" className="block text-gray-700 mb-2">描述</label>
                  <textarea
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    rows={4}
                    placeholder="照片描述"
                  ></textarea>
                </div>

                <div className="flex space-x-4">
                  <button
                    type="submit"
                    className="bg-blue-500 text-white py-2 px-6 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                  >
                    <Check className="inline-block h-4 w-4 mr-2" />
                    保存
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setIsEditing(false)
                      setTitle(photo.title || '')
                      setDescription(photo.description || '')
                      setTags(photo.tags?.join(', ') || '')
                    }}
                    className="bg-gray-200 text-gray-700 py-2 px-6 rounded-lg hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:ring-offset-2"
                  >
                    <X className="inline-block h-4 w-4 mr-2" />
                    取消
                  </button>
                </div>
              </form>
            ) : (
              <>
                <h1 className="text-2xl font-bold text-gray-800 mb-2">
                  {photo.title || photo.filename}
                </h1>
                <p className="text-gray-500 mb-4">
                  上传于 {new Date(photo.uploaded_at).toLocaleString()}
                </p>

                {photo.tags && photo.tags.length > 0 && (
                  <div className="mb-4">
                    <h3 className="text-sm font-medium text-gray-700 mb-2">标签</h3>
                    <div className="flex flex-wrap gap-2">
                      {photo.tags.map((tag: string, index: number) => (
                        <span
                          key={index}
                          className="bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded"
                        >
                          {tag}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {photo.description && (
                  <div className="mb-6">
                    <h3 className="text-sm font-medium text-gray-700 mb-2">描述</h3>
                    <p className="text-gray-600">{photo.description}</p>
                  </div>
                )}

                <div className="flex space-x-4">
                  <button
                    onClick={() => setIsEditing(true)}
                    className="bg-blue-500 text-white py-2 px-6 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                  >
                    <Edit2 className="inline-block h-4 w-4 mr-2" />
                    编辑
                  </button>
                  <button
                    onClick={handleDelete}
                    className="bg-red-500 text-white py-2 px-6 rounded-lg hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
                  >
                    <Trash2 className="inline-block h-4 w-4 mr-2" />
                    删除
                  </button>
                  <button
                    className="bg-green-500 text-white py-2 px-6 rounded-lg hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
                  >
                    <Share2 className="inline-block h-4 w-4 mr-2" />
                    分享
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default PhotoDetailPage