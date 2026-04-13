import { request } from '@/utils/request'
import type { Result, PageResult } from '@/types'
import type { Album, AlbumDTO } from '@/types/album'
import type { Photo } from '@/types/photo'

export const albumApi = {
  getList(): Promise<Result<Album[]>> {
    return request.get('/v1/albums')
  },

  getById(id: number): Promise<Result<Album>> {
    return request.get(`/v1/albums/${id}`)
  },

  create(data: AlbumDTO): Promise<Result<Album>> {
    return request.post('/v1/albums', data)
  },

  update(id: number, data: AlbumDTO): Promise<Result<Album>> {
    return request.put(`/v1/albums/${id}`, data)
  },

  delete(id: number): Promise<Result<void>> {
    return request.delete(`/v1/albums/${id}`)
  },

  addPhotos(id: number, photoIds: number[]): Promise<Result<void>> {
    return request.post(`/v1/albums/${id}/photos`, { photoIds })
  },

  removePhotos(id: number, photoIds: number[]): Promise<Result<void>> {
    return request.delete(`/v1/albums/${id}/photos`, { data: { photoIds } })
  },

  getPhotos(id: number, params: any): Promise<Result<PageResult<Photo>>> {
    return request.get(`/v1/albums/${id}/photos`, { params })
  },
}
