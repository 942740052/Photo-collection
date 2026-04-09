import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { usePhotos } from '../hooks/usePhotos'
import Navbar from '../components/Navbar'
import { Upload as UploadIcon, Image, X } from 'lucide-react'

const UploadPage = () => {
  const { user, loading: authLoading } = useAuth()
  const { uploadPhoto, loading: uploadLoading } = usePhotos(user?.id)
  const navigate = useNavigate()
  const [files, setFiles] = useState<File[]>([])
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [tags, setTags] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const fileInputRef = useRef<HTMLInputElement>(null)

  if (authLoading) {
    return <div className="min-h-screen flex items-center justify-center">加载中...</div>
  }

  if (!user) {
    navigate('/login')
    return null
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    if (e.dataTransfer.files.length > 0) {
      setFiles([...files, ...Array.from(e.dataTransfer.files)])
    }
  }

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFiles([...files, ...Array.from(e.target.files)])
    }
  }

  const removeFile = (index: number) => {
    setFiles(files.filter((_, i) => i !== index))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (files.length === 0) {
      setError('请选择要上传的照片')
      return
    }

    // 处理标签输入，将逗号分隔的标签转换为数组
    const tagsArray = tags
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag !== '')

    // 上传每个文件
    try {
      for (const file of files) {
        await uploadPhoto(file, title || file.name, description, tagsArray)
      }
      setSuccess('照片上传成功！')
      setFiles([])
      setTitle('')
      setDescription('')
      setTags('')
    } catch (err) {
      setError('上传失败，请重试')
      console.error(err)
    }
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />
      <div className="container mx-auto px-4 pt-24 pb-12">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-800 mb-2">上传照片</h1>
          <p className="text-gray-600">支持批量上传，拖拽文件到下方区域</p>
        </div>

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

        <form onSubmit={handleSubmit}>
          {/* 拖拽上传区域 */}
          <div
            className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center mb-6"
            onDragOver={handleDragOver}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
          >
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileSelect}
              multiple
              accept="image/*"
              className="hidden"
            />
            <UploadIcon className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-600 mb-2">拖拽照片到这里，或点击选择文件</p>
            <p className="text-sm text-gray-500">支持 JPG、PNG、WebP 等图片格式</p>
          </div>

          {/* 已选择的文件 */}
          {files.length > 0 && (
            <div className="mb-6">
              <h3 className="text-sm font-medium text-gray-700 mb-2">已选择 {files.length} 个文件</h3>
              <div className="flex flex-wrap gap-2">
                {files.map((file, index) => (
                  <div key={index} className="flex items-center bg-gray-100 rounded-full px-3 py-1">
                    <Image className="h-4 w-4 text-gray-500 mr-2" />
                    <span className="text-sm text-gray-700 truncate max-w-[150px]">
                      {file.name}
                    </span>
                    <button
                      type="button"
                      onClick={() => removeFile(index)}
                      className="ml-2 text-gray-500 hover:text-gray-700"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 照片信息 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div>
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
            <div>
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
            <div className="md:col-span-2">
              <label htmlFor="description" className="block text-gray-700 mb-2">描述</label>
              <textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows={3}
                placeholder="照片描述"
              ></textarea>
            </div>
          </div>

          <button
            type="submit"
            disabled={uploadLoading || files.length === 0}
            className="bg-blue-500 text-white py-2 px-6 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
          >
            {uploadLoading ? '上传中...' : `上传 ${files.length} 张照片`}
          </button>
        </form>
      </div>
    </div>
  )
}

export default UploadPage