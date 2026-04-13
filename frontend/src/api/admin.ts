import { request } from '@/utils/request'
import type { Result, PageResult } from '@/types'
import type { UserManage, UserQuery, SystemConfig, Announcement, StorageStatistics } from '@/types/admin'

export const adminApi = {
  getUserList(params: UserQuery): Promise<Result<PageResult<UserManage>>> {
    return request.get('/v1/admin/users', { params })
  },

  updateUserStatus(id: number, status: number): Promise<Result<void>> {
    return request.put(`/v1/admin/users/${id}/status`, null, {
      params: { status },
    })
  },

  deleteUser(id: number): Promise<Result<void>> {
    return request.delete(`/v1/admin/users/${id}`)
  },

  getSystemConfig(): Promise<Result<SystemConfig>> {
    return request.get('/v1/admin/config')
  },

  updateSystemConfig(data: SystemConfig): Promise<Result<void>> {
    return request.put('/v1/admin/config', data)
  },

  getStorageStatistics(): Promise<Result<StorageStatistics>> {
    return request.get('/v1/admin/storage')
  },

  getAnnouncements(): Promise<Result<Announcement[]>> {
    return request.get('/v1/admin/announcements')
  },

  createAnnouncement(data: Partial<Announcement>): Promise<Result<Announcement>> {
    return request.post('/v1/admin/announcements', data)
  },

  updateAnnouncement(id: number, data: Partial<Announcement>): Promise<Result<Announcement>> {
    return request.put(`/v1/admin/announcements/${id}`, data)
  },

  deleteAnnouncement(id: number): Promise<Result<void>> {
    return request.delete(`/v1/admin/announcements/${id}`)
  },
}
