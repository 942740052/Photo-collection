export interface User {
  id: number
  username: string
  email: string
  nickname: string
  avatar: string
  role: string
  status: number
  createdAt: string
  lastLoginAt: string
}

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  email: string
  nickname?: string
}
