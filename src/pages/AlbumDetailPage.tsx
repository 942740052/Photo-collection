import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useAlbums } from '../hooks/useAlbums'
import Navbar from '../components/Navbar'
import { supabase } from '../supabase/client'
import { ArrowLeft, Trash2, Edit2, Image as ImageIcon, Check } from 'lucide-react'

const AlbumDetailPage = () => {
  const { albumId } = useParams<{ albumId: string }>()
  const { user, loading: authLoading } = useAuth()
  const { getAlbumPhotos, updateAlbum, removePhotoFromAlbum, loading, error } = useAlbums(user?.id)
  const navigate = useNavigate()
  
  const [photos, setPhotos] = useState<any[]>([])
  const [album, setAlbum] = useState<any>(null)
  const [loadingPhotos, setLoadingPhotos] = useState(true)
  const [showCoverModal, setShowCoverModal] = useState(false)
  const [selectedCover, setSelectedCover] = useState<string | null>(null)

  useEffect(() => {
    if (!authLoading && !user) {
      navigate('/login')
    } else if (user && albumId) {
      fetchAlbumData()
    }
  }, [authLoading, user, navigate, albumId])

  const fetchAlbumData = async () => {
    if (!albumId || !user) return

    setLoadingPhotos(true)
    try {
      // 获取相册详情
      const { data: albumData, error: albumError } = await supabase
        .from('albums')
        .select('*')
        .eq('id', albumId)
        .eq('user_id', user.id)
        .single()
      
      if (albumError) throw albumError
      setAlbum(albumData)

      // 获取相册照片
      const { data: albumPhotos, error: photosError } = await getAlbumPhotos(albumId)
      if (photosError) throw photosError
      setPhotos(albumPhotos || [])
    } catch (err) {
      console.error(err)
    } finally {
      setLoadingPhotos(false)
    }
  }

  const handleSetCover = async () => {
    if (!albumId || !selectedCover) return

    await updateAlbum(albumId, { cover_photo_id: selectedCover })
    setShowCoverModal(false)
    setSelectedCover(null)
    // 刷新相册数据
    fetchAlbumData()
  }

  const handleRemovePhoto = async (photoId: string) => {
    if (window.confirm('确定要从相册中移除这张照片吗？')) {
      await removePhotoFromAlbum(albumId!, photoId)
      // 刷新相册数据
      fetchAlbumData()
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
        <div className="flex items-center mb-6">
          <Link 
            to="/albums" 
            className="flex items-center text-blue-500 hover:text-blue-600 mr-4"
          >
            <ArrowLeft className="h-5 w-5 mr-1" />
            返回相册列表
          </Link>
          <div>
            <h1 className="text-2xl font-bold text-gray-800">{album?.name || '相册'}</h1>
            <p className="text-gray-600">{album?.description || '无描述'}</p>
          </div>
        </div>

        <div className="flex justify-between items-center mb-6">
          <p className="text-gray-600">共 {photos.length} 张照片</p>
          <button
            onClick={() => setShowCoverModal(true)}
            className="bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 flex items-center"
          >
            <ImageIcon className="h-4 w-4 mr-2" />
            设置封面
          </button>
        </div>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {loadingPhotos ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {Array.from({ length: 10 }).map((_, index) => (
              <div key={index} className="bg-gray-200 rounded-lg aspect-square animate-pulse"></div>
            ))}
          </div>
        ) : photos.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-600 mb-4">相册中还没有照片</p>
            <Link
              to="/upload"
              className="bg-blue-500 text-white py-2 px-6 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              上传照片
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {photos.map((photo) => (
              <div key={photo.id} className="relative group">
                <div className="aspect-square bg-gray-200 rounded-lg overflow-hidden">
                  <img
                    src={photo.url}
                    alt={photo.caption || '照片'}
                    className="w-full h-full object-cover"
                  />
                </div>
                <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-30 transition-all flex items-center justify-center opacity-0 group-hover:opacity-100">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleRemovePhoto(photo.id)}
                      className="bg-white p-2 rounded-full hover:bg-gray-100 transition-colors"
                      title="从相册移除"
                    >
                      <Trash2 className="h-4 w-4 text-gray-800" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 设置封面模态框 */}
        {showCoverModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[80vh] overflow-y-auto">
              <h2 className="text-xl font-bold text-gray-800 mb-4">选择相册封面</h2>
              
              <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-4 mb-6">
                {photos.map((photo) => (
                  <div 
                    key={photo.id} 
                    className={`relative cursor-pointer rounded-lg overflow-hidden ${selectedCover === photo.id ? 'ring-2 ring-blue-500 ring-offset-2' : ''}`}
                    onClick={() => setSelectedCover(photo.id)}
                  >
                    <div className="aspect-square">
                      <img
                        src={photo.url}
                        alt={photo.caption || '照片'}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    {selectedCover === photo.id && (
                      <div className="absolute top-2 right-2 bg-blue-500 rounded-full p-1">
                        <Check className="h-4 w-4 text-white" />
                      </div>
                    )}
                  </div>
                ))}
              </div>
              
              <div className="flex space-x-4">
                <button
                  onClick={handleSetCover}
                  disabled={!selectedCover}
                  className="bg-blue-500 text-white py-2 px-6 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  确定
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowCoverModal(false)
                    setSelectedCover(null)
                  }}
                  className="bg-gray-200 text-gray-700 py-2 px-6 rounded-lg hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:ring-offset-2"
                >
                  取消
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default AlbumDetailPage