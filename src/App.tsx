import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import UploadPage from './pages/UploadPage'
import PhotoDetailPage from './pages/PhotoDetailPage'
import AlbumsPage from './pages/AlbumsPage'
import AlbumDetailPage from './pages/AlbumDetailPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/upload" element={<UploadPage />} />
        <Route path="/photo/:id" element={<PhotoDetailPage />} />
        <Route path="/albums" element={<AlbumsPage />} />
        <Route path="/album/:albumId" element={<AlbumDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Routes>
    </Router>
  )
}

export default App