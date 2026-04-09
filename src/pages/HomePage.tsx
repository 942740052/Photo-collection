import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { usePhotos } from '../hooks/usePhotos'
import Navbar from '../components/Navbar'
import { Trash2, Edit } from 'lucide-react'

const HomePage = () => {
  const { user, loading: authLoading } = useAuth()
  const { photos, loading, error, fetchPhotos, deletePhoto } = usePhotos(user?.id)
  const navigate = useNavigate()
  const [selectedPhoto, setSelectedPhoto] = useState<string | null>(null)

  useEffect(() => {
    if (!authLoading && !user) {
      navigate('/login')
    } else if (user) {
      fetchPhotos()
    }
  }, [authLoading, user, navigate, fetchPhotos])

  const handleDelete = async (photoId: string) => {
    if (window.confirm('确定要删除这张照片吗？')) {
      await deletePhoto(photoId)
    }
  }

  if (authLoading) {
    return <div className="min-h-screen flex items-center justify-center">加载中...</div>
  }

  if (!user) {
    return null
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-12">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-800 mb-2">我的照片</h1>
          <p className="text-gray-600">共 {photos.length} 张照片</p>
        </div>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {Array.from({ length: 8 }).map((_, index) => (
              <div key={index} className="bg-gray-200 rounded-lg aspect-square animate-pulse"></div>
            ))}
          </div>
        ) : photos.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-600 mb-4">还没有照片，快去上传吧！</p>
            <Link
              to="/upload"
              className="bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              上传照片
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {photos.map((photo) => (
              <div key={photo.id} className="relative group">
                <Link to={`/photo/${photo.id}`}>
                  <div className="aspect-square overflow-hidden rounded-lg shadow-md">
                    <img
                      src={photo.file_path}
                      alt={photo.title || photo.filename}
                      className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                    />
                  </div>
                </Link>
                <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-30 transition-all duration-300 flex items-center justify-center opacity-0 group-hover:opacity-100">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleDelete(photo.id)}
                      className="bg-white bg-opacity-80 p-2 rounded-full hover:bg-opacity-100 transition-colors"
                      title="删除"
                    >
                      <Trash2 className="h-5 w-5 text-gray-800" />
                    </button>
                    <Link
                      to={`/photo/${photo.id}`}
                      className="bg-white bg-opacity-80 p-2 rounded-full hover:bg-opacity-100 transition-colors"
                      title="编辑"
                    >
                      <Edit className="h-5 w-5 text-gray-800" />
                    </Link>
                  </div>
                </div>
                <div className="mt-2">
                  <h3 className="text-sm font-medium text-gray-800 truncate">
                    {photo.title || photo.filename}
                  </h3>
                  <p className="text-xs text-gray-500">
                    {new Date(photo.uploaded_at).toLocaleDateString()}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default HomePage