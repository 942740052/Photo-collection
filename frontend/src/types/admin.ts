export interface UserManage {
  id: number
  username: string
  email: string
  nickname: string
  avatar: string
  role: string
  status: number
  createdAt: string
  lastLoginAt: string
  photoCount: number
  storageUsed: number
}

export interface UserQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  role?: string
  status?: number
}

export interface SystemConfig {
  [key: string]: string
}

export interface Announcement {
  id: number
  title: string
  content: string
  type: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface StorageStatistics {
  totalStorage: number
  usedStorage: number
  availableStorage: number
  photoCount: number
  albumCount: number
  userCount: number
}
