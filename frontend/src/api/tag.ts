import { request } from '@/utils/request'
import type { Result } from '@/types'
import type { Tag } from '@/types/tag'

export const tagApi = {
  getList(): Promise<Result<Tag[]>> {
    return request.get('/v1/tags')
  },

  create(data: { name: string; color?: string }): Promise<Result<Tag>> {
    return request.post('/v1/tags', data)
  },

  update(id: number, data: { name?: string; color?: string }): Promise<Result<Tag>> {
    return request.put(`/v1/tags/${id}`, data)
  },

  delete(id: number): Promise<Result<void>> {
    return request.delete(`/v1/tags/${id}`)
  },

  addTagToPhoto(photoId: number, tagId: number): Promise<Result<void>> {
    return request.post(`/v1/photos/${photoId}/tags`, { tagId })
  },

  removeTagFromPhoto(photoId: number, tagId: number): Promise<Result<void>> {
    return request.delete(`/v1/photos/${photoId}/tags/${tagId}`)
  },
}
