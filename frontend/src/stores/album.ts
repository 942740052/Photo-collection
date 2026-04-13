import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Album, AlbumDTO } from '@/types/album'
import { albumApi } from '@/api/album'

export const useAlbumStore = defineStore('album', () => {
  const albums = ref<Album[]>([])
  const currentAlbum = ref<Album | null>(null)
  const loading = ref(false)

  const fetchAlbums = async () => {
    loading.value = true
    try {
      const res = await albumApi.getList()
      albums.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  const fetchAlbumById = async (id: number) => {
    loading.value = true
    try {
      const res = await albumApi.getById(id)
      currentAlbum.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  const createAlbum = async (data: AlbumDTO) => {
    const res = await albumApi.create(data)
    await fetchAlbums()
    return res.data
  }

  const updateAlbum = async (id: number, data: AlbumDTO) => {
    const res = await albumApi.update(id, data)
    await fetchAlbums()
    return res.data
  }

  const deleteAlbum = async (id: number) => {
    await albumApi.delete(id)
    await fetchAlbums()
  }

  return {
    albums,
    currentAlbum,
    loading,
    fetchAlbums,
    fetchAlbumById,
    createAlbum,
    updateAlbum,
    deleteAlbum,
  }
})
