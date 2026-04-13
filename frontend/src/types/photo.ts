export interface Photo {
  id: number
  userId: number
  albumId: number
  albumName: string
  title: string
  description: string
  filePath: string
  thumbnailPath: string
  fileSize: number
  fileType: string
  width: number
  height: number
  shootTime: string
  cameraModel: string
  gpsLatitude: number
  gpsLongitude: number
  isFavorite: number
  viewCount: number
  createdAt: string
  tags: Tag[]
}

export interface PhotoQuery {
  pageNum: number
  pageSize: number
  albumId?: number
  keyword?: string
  startTime?: string
  endTime?: string
  tagId?: number
  isFavorite?: number
}

export interface PhotoUpdate {
  id: number
  title: string
  description?: string
  albumId?: number
  tagIds?: number[]
}
