import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Share, ShareCreate, ShareContent } from '@/types/share'
import { shareApi } from '@/api/share'

export const useShareStore = defineStore('share', () => {
  const shares = ref<Share[]>([])
  const currentShare = ref<ShareContent | null>(null)
  const loading = ref(false)

  const sharePhoto = async (photoId: number, data?: ShareCreate) => {
    const res = await shareApi.sharePhoto(photoId, data)
    return res.data
  }

  const shareAlbum = async (albumId: number, data?: ShareCreate) => {
    const res = await shareApi.shareAlbum(albumId, data)
    return res.data
  }

  const fetchShares = async () => {
    loading.value = true
    try {
      const res = await shareApi.getShareList()
      shares.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  const getShareContent = async (code: string) => {
    loading.value = true
    try {
      const res = await shareApi.getShare(code)
      currentShare.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  const verifyShare = async (code: string, password?: string) => {
    loading.value = true
    try {
      const res = await shareApi.verifyShare(code, password)
      currentShare.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  const cancelShare = async (code: string) => {
    await shareApi.cancelShare(code)
    await fetchShares()
  }

  return {
    shares,
    currentShare,
    loading,
    sharePhoto,
    shareAlbum,
    fetchShares,
    getShareContent,
    verifyShare,
    cancelShare,
  }
})
