import { request } from '@/utils/request'
import type { Result } from '@/types'
import type { User, LoginDTO, RegisterDTO } from '@/types/user'

export const authApi = {
  login(data: LoginDTO): Promise<Result<{ token: string; refreshToken: string; user: User }>> {
    return request.post('/v1/auth/login', data)
  },

  register(data: RegisterDTO): Promise<Result<{ token: string; refreshToken: string; user: User }>> {
    return request.post('/v1/auth/register', data)
  },

  getProfile(): Promise<Result<User>> {
    return request.get('/v1/auth/profile')
  },

  updateProfile(data: Partial<User>): Promise<Result<User>> {
    return request.put('/v1/auth/profile', data)
  },

  updatePassword(oldPassword: string, newPassword: string): Promise<Result<void>> {
    return request.put('/v1/auth/password', null, {
      params: { oldPassword, newPassword },
    })
  },

  getUserStats(): Promise<Result<{ photoCount: number; albumCount: number; shareCount: number }>> {
    return request.get('/v1/auth/stats')
  },
}
