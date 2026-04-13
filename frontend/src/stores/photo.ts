import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Photo, PhotoQuery } from '@/types/photo'
import type { PageResult } from '@/types'
import { photoApi } from '@/api/photo'

export const usePhotoStore = defineStore('photo', () => {
  const photos = ref<Photo[]>([])
  const currentPhoto = ref<Photo | null>(null)
  const total = ref(0)
  const loading = ref(false)

  const fetchPhotos = async (params: PhotoQuery) => {
    loading.value = true
    try {
      const res = await photoApi.getList(params)
      photos.value = res.data.records
      total.value = res.data.total
      return res.data
    } finally {
      loading.value = false
    }
  }

  const fetchPhotoById = async (id: number) => {
    loading.value = true
    try {
      const res = await photoApi.getById(id)
      currentPhoto.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  const updatePhoto = async (id: number, data: any) => {
    const res = await photoApi.update(id, data)
    return res.data
  }

  const deletePhoto = async (id: number) => {
    await photoApi.delete(id)
  }

  const toggleFavorite = async (id: number, isFavorite: boolean) => {
    if (isFavorite) {
      await photoApi.unfavorite(id)
    } else {
      await photoApi.favorite(id)
    }
  }

  return {
    photos,
    currentPhoto,
    total,
    loading,
    fetchPhotos,
    fetchPhotoById,
    updatePhoto,
    deletePhoto,
    toggleFavorite,
  }
})
