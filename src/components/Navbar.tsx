import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { Search, Upload, Album, User, LogOut, Menu, X } from 'lucide-react'

const Navbar = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <nav className="bg-white shadow-md fixed w-full z-10">
      <div className="container mx-auto px-4 py-3">
        <div className="flex justify-between items-center">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <div className="h-10 w-10 bg-blue-500 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-xl">P</span>
            </div>
            <span className="text-xl font-bold text-gray-800">照片收藏</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center space-x-8">
            <div className="relative">
              <input
                type="text"
                placeholder="搜索照片..."
                className="pl-10 pr-4 py-2 rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
            </div>
            {user ? (
              <>
                <Link to="/upload" className="flex items-center space-x-1 text-gray-700 hover:text-blue-500">
                  <Upload className="h-5 w-5" />
                  <span>上传</span>
                </Link>
                <Link to="/albums" className="flex items-center space-x-1 text-gray-700 hover:text-blue-500">
                  <Album className="h-5 w-5" />
                  <span>相册</span>
                </Link>
                <div className="relative group">
                  <button className="flex items-center space-x-1 text-gray-700 hover:text-blue-500">
                    <User className="h-5 w-5" />
                    <span>{user.email?.split('@')[0]}</span>
                  </button>
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 hidden group-hover:block">
                    <button
                      onClick={handleLogout}
                      className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 w-full text-left"
                    >
                      <LogOut className="inline-block h-4 w-4 mr-2" />
                      退出登录
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <>
                <Link to="/login" className="text-gray-700 hover:text-blue-500">登录</Link>
                <Link to="/register" className="text-gray-700 hover:text-blue-500">注册</Link>
              </>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button
            className="md:hidden text-gray-700"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>

        {/* Mobile Navigation */}
        {mobileMenuOpen && (
          <div className="md:hidden mt-4 py-2">
            <div className="relative mb-4">
              <input
                type="text"
                placeholder="搜索照片..."
                className="w-full pl-10 pr-4 py-2 rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
            </div>
            {user ? (
              <div className="space-y-2">
                <Link
                  to="/upload"
                  className="flex items-center space-x-2 text-gray-700 hover:text-blue-500 py-2"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Upload className="h-5 w-5" />
                  <span>上传</span>
                </Link>
                <Link
                  to="/albums"
                  className="flex items-center space-x-2 text-gray-700 hover:text-blue-500 py-2"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Album className="h-5 w-5" />
                  <span>相册</span>
                </Link>
                <button
                  onClick={async () => {
                    await handleLogout()
                    setMobileMenuOpen(false)
                  }}
                  className="flex items-center space-x-2 text-gray-700 hover:text-blue-500 py-2 w-full text-left"
                >
                  <LogOut className="h-5 w-5" />
                  <span>退出登录</span>
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                <Link
                  to="/login"
                  className="block text-gray-700 hover:text-blue-500 py-2"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  登录
                </Link>
                <Link
                  to="/register"
                  className="block text-gray-700 hover:text-blue-500 py-2"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  注册
                </Link>
              </div>
            )}
          </div>
        )}
      </div>
    </nav>
  )
}

export default Navbar