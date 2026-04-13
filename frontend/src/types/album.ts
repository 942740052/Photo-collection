export interface Album {
  id: number
  userId: number
  name: string
  description: string
  coverPhotoId: number
  coverPhotoUrl: string
  photoCount: number
  isPublic: number
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface AlbumDTO {
  id?: number
  name: string
  description?: string
  coverPhotoId?: number
  isPublic?: number
}
