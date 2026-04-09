import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useAlbums } from '../hooks/useAlbums'
import Navbar from '../components/Navbar'
import { Plus, Trash2, Edit2 } from 'lucide-react'

const AlbumsPage = () => {
  const { user, loading: authLoading } = useAuth()
  const { albums, loading, error, fetchAlbums, createAlbum, deleteAlbum } = useAlbums(user?.id)
  const navigate = useNavigate()
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [albumName, setAlbumName] = useState('')
  const [albumDescription, setAlbumDescription] = useState('')
  const [createError, setCreateError] = useState('')

  useEffect(() => {
    if (!authLoading && !user) {
      navigate('/login')
    } else if (user) {
      fetchAlbums()
    }
  }, [authLoading, user, navigate, fetchAlbums])

  const handleCreateAlbum = async (e: React.FormEvent) => {
    e.preventDefault()
    setCreateError('')

    if (!albumName.trim()) {
      setCreateError('请输入相册名称')
      return
    }

    await createAlbum(albumName, albumDescription, null)
    setShowCreateModal(false)
    setAlbumName('')
    setAlbumDescription('')
  }

  const handleDeleteAlbum = async (albumId: string) => {
    if (window.confirm('确定要删除这个相册吗？')) {
      await deleteAlbum(albumId)
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
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-800 mb-2">我的相册</h1>
            <p className="text-gray-600">共 {albums.length} 个相册</p>
          </div>
          <button
            onClick={() => setShowCreateModal(true)}
            className="bg-blue-500 text-white py-2 px-6 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            <Plus className="inline-block h-4 w-4 mr-2" />
            创建相册
          </button>
        </div>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {Array.from({ length: 4 }).map((_, index) => (
              <div key={index} className="bg-gray-200 rounded-lg aspect-[4/3] animate-pulse"></div>
            ))}
          </div>
        ) : albums.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-600 mb-4">还没有相册，快去创建吧！</p>
            <button
              onClick={() => setShowCreateModal(true)}
              className="bg-blue-500 text-white py-2 px-6 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              创建相册
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {albums.map((album) => (
              <div key={album.id} className="bg-white rounded-lg shadow-md overflow-hidden group">
                <div className="aspect-[4/3] bg-gray-200 flex items-center justify-center">
                  {album.cover_photo_id ? (
                    <img
                      src="https://via.placeholder.com/400x300?text=Album+Cover"
                      alt={album.name}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="text-gray-400">
                      <span className="text-xl">📷</span>
                    </div>
                  )}
                </div>
                <div className="p-4 relative">
                  <div className="absolute top-2 right-2 flex space-x-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      className="bg-white bg-opacity-80 p-1 rounded-full hover:bg-opacity-100 transition-colors"
                      title="编辑"
                    >
                      <Edit2 className="h-4 w-4 text-gray-800" />
                    </button>
                    <button
                      onClick={() => handleDeleteAlbum(album.id)}
                      className="bg-white bg-opacity-80 p-1 rounded-full hover:bg-opacity-100 transition-colors"
                      title="删除"
                    >
                      <Trash2 className="h-4 w-4 text-gray-800" />
                    </button>
                  </div>
                  <h3 className="text-lg font-medium text-gray-800 mb-1">{album.name}</h3>
                  <p className="text-sm text-gray-500 mb-2">
                    {album.description || '无描述'}
                  </p>
                  <p className="text-xs text-gray-400">
                    创建于 {new Date(album.created_at).toLocaleDateString()}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 创建相册模态框 */}
        {showCreateModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <h2 className="text-xl font-bold text-gray-800 mb-4">创建新相册</h2>
              
              {createError && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-2 rounded mb-4">
                  {createError}
                </div>
              )}
              
              <form onSubmit={handleCreateAlbum}>
                <div className="mb-4">
                  <label htmlFor="albumName" className="block text-gray-700 mb-2">相册名称</label>
                  <input
                    type="text"
                    id="albumName"
                    value={albumName}
                    onChange={(e) => setAlbumName(e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="输入相册名称"
                  />
                </div>
                
                <div className="mb-6">
                  <label htmlFor="albumDescription" className="block text-gray-700 mb-2">描述</label>
                  <textarea
                    id="albumDescription"
                    value={albumDescription}
                    onChange={(e) => setAlbumDescription(e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    rows={3}
                    placeholder="输入相册描述"
                  ></textarea>
                </div>
                
                <div className="flex space-x-4">
                  <button
                    type="submit"
                    className="bg-blue-500 text-white py-2 px-6 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                  >
                    创建
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowCreateModal(false)
                      setAlbumName('')
                      setAlbumDescription('')
                      setCreateError('')
                    }}
                    className="bg-gray-200 text-gray-700 py-2 px-6 rounded-lg hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:ring-offset-2"
                  >
                    取消
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default AlbumsPage