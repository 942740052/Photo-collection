export interface Share {
  id: number
  userId: number
  photoId: number
  albumId: number
  shareCode: string
  hasPassword: boolean
  expireAt: string
  viewCount: number
  isActive: number
  createdAt: string
  photoTitle: string
  albumName: string
}

export interface ShareCreate {
  photoId?: number
  albumId?: number
  password?: string
  expireHours?: number
}

export interface ShareContent {
  type: string
  content: any
  photos: any[]
}
