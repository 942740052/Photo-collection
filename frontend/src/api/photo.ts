import { request } from '@/utils/request'
import type { Result, PageResult } from '@/types'
import type { Photo, PhotoQuery, PhotoUpdate } from '@/types/photo'

export const photoApi = {
  getList(params: PhotoQuery): Promise<Result<PageResult<Photo>>> {
    return request.get('/v1/photos', { params })
  },

  getById(id: number): Promise<Result<Photo>> {
    return request.get(`/v1/photos/${id}`)
  },

  uploadPhoto(file: File, albumId?: number, title?: string, description?: string): Promise<Result<Photo>> {
    const formData = new FormData()
    formData.append('file', file)
    if (albumId) formData.append('albumId', String(albumId))
    if (title) formData.append('title', title)
    if (description) formData.append('description', description)
    return request.post('/v1/photos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  uploadPhotos(files: File[]): Promise<Result<Photo[]>> {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return request.post('/v1/photos/batch', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  update(id: number, data: PhotoUpdate): Promise<Result<Photo>> {
    return request.put(`/v1/photos/${id}`, data)
  },

  batchUpdate(data: { photoIds: number[]; title?: string; description?: string; albumId?: number }): Promise<Result<void>> {
    return request.put('/v1/photos/batch', data)
  },

  delete(id: number): Promise<Result<void>> {
    return request.delete(`/v1/photos/${id}`)
  },

  permanentDelete(id: number): Promise<Result<void>> {
    return request.delete(`/v1/photos/${id}/permanent`)
  },

  restore(id: number): Promise<Result<void>> {
    return request.post(`/v1/photos/${id}/restore`)
  },

  batchRestore(ids: number[]): Promise<Result<void>> {
    return request.post('/v1/photos/batch-operation', { ids, operation: 'restore' })
  },

  emptyTrash(): Promise<Result<void>> {
    return request.delete('/v1/photos/trash')
  },

  favorite(id: number): Promise<Result<void>> {
    return request.post(`/v1/photos/${id}/favorite`)
  },

  unfavorite(id: number): Promise<Result<void>> {
    return request.delete(`/v1/photos/${id}/favorite`)
  },

  batchOperation(data: { ids: number[]; operation: string }): Promise<Result<void>> {
    return request.post('/v1/photos/batch-operation', data)
  },

  batchMoveToAlbum(photoIds: number[], albumId: number): Promise<Result<void>> {
    return request.post('/v1/photos/batch-move', { ids: photoIds, albumId })
  },

  getCameraModels(): Promise<Result<string[]>> {
    return request.get('/v1/photos/cameras')
  },

  getTags(): Promise<Result<{ id: number; name: string }[]>> {
    return request.get('/v1/tags')
  },
}
