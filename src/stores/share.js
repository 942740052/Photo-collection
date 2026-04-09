import { defineStore } from 'pinia'
import { mockShareLinks } from '../mock/data'
import dayjs from 'dayjs'

export const useShareStore = defineStore('share', {
  state: () => ({
    shareLinks: [...mockShareLinks]
  }),

  getters: {
    activeShares: state => state.shareLinks.filter(s => s.is_active),
    expiredShares: state => state.shareLinks.filter(s => s.expires_at && dayjs(s.expires_at).isBefore(dayjs()))
  },

  actions: {
    createShare(data) {
      const share = {
        id: `share-${Date.now()}`,
        user_id: 'user-1',
        token: Math.random().toString(36).substring(2, 14),
        resource_type: data.resource_type,
        resource_id: data.resource_id,
        resource_name: data.resource_name,
        password: data.password || null,
        expires_at: data.expires_at || null,
        max_views: data.max_views || null,
        view_count: 0,
        allow_download: data.allow_download ?? true,
        is_active: true,
        created_at: dayjs().format('YYYY-MM-DD HH:mm:ss')
      }
      this.shareLinks.push(share)
      return share
    },

    revokeShare(shareId) {
      const share = this.shareLinks.find(s => s.id === shareId)
      if (share) {
        share.is_active = false
      }
    },

    deleteShare(shareId) {
      this.shareLinks = this.shareLinks.filter(s => s.id !== shareId)
    },

    getShareByToken(token) {
      return this.shareLinks.find(s => s.token === token && s.is_active)
    }
  }
})
