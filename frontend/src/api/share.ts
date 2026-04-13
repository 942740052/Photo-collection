import { request } from '@/utils/request'
import type { Result } from '@/types'
import type { Share, ShareCreate, ShareContent } from '@/types/share'

export const shareApi = {
  sharePhoto(id: number, data?: ShareCreate): Promise<Result<Share>> {
    return request.post(`/v1/share/photo/${id}`, data)
  },

  shareAlbum(id: number, data?: ShareCreate): Promise<Result<Share>> {
    return request.post(`/v1/share/album/${id}`, data)
  },

  getShare(code: string): Promise<Result<ShareContent>> {
    return request.get(`/v1/share/${code}`)
  },

  verifyShare(code: string, password?: string): Promise<Result<ShareContent>> {
    return request.post(`/v1/share/${code}/verify`, password, {
      headers: { 'Content-Type': 'text/plain' }
    })
  },

  getShareList(): Promise<Result<Share[]>> {
    return request.get('/v1/share')
  },

  cancelShare(code: string): Promise<Result<void>> {
    return request.delete(`/v1/share/${code}`)
  },

  getShareStatistics(id: number): Promise<Result<Share>> {
    return request.get(`/v1/share/${id}/statistics`)
  },
}
