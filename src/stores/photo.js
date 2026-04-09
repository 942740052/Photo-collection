import { defineStore } from 'pinia'
import { mockPhotos, mockAlbums, mockTags, mockPeople } from '../mock/data'
import dayjs from 'dayjs'

export const usePhotoStore = defineStore('photo', {
  state: () => ({
    photos: [...mockPhotos],
    albums: [...mockAlbums],
    tags: [...mockTags],
    people: [...mockPeople],
    selectedPhotos: [],
    viewMode: 'grid',
    gridSize: 'medium',
    sortBy: 'date_taken',
    sortOrder: 'desc',
    filterTags: [],
    filterDateRange: null,
    filterCamera: '',
    searchQuery: ''
  }),

  getters: {
    activePhotos: state => state.photos.filter(p => !p.is_deleted && !p.is_archived),
    deletedPhotos: state => state.photos.filter(p => p.is_deleted),
    favoritePhotos: state => state.photos.filter(p => p.is_favorite && !p.is_deleted),
    archivedPhotos: state => state.photos.filter(p => p.is_archived && !p.is_deleted),

    filteredPhotos(state) {
      let result = state.photos.filter(p => !p.is_deleted && !p.is_archived)

      if (state.searchQuery) {
        const q = state.searchQuery.toLowerCase()
        result = result.filter(p =>
          p.file_name.toLowerCase().includes(q) ||
          p.location_name?.toLowerCase().includes(q) ||
          p.tags?.some(t => t.toLowerCase().includes(q)) ||
          p.camera_model?.toLowerCase().includes(q) ||
          p.description?.toLowerCase().includes(q)
        )
      }

      if (state.filterTags.length > 0) {
        result = result.filter(p =>
          state.filterTags.some(t => p.tags?.includes(t))
        )
      }

      if (state.filterDateRange && state.filterDateRange.length === 2) {
        const [start, end] = state.filterDateRange
        result = result.filter(p => {
          const d = dayjs(p.date_taken)
          return d.isAfter(dayjs(start).startOf('day')) && d.isBefore(dayjs(end).endOf('day'))
        })
      }

      if (state.filterCamera) {
        result = result.filter(p => p.camera_model === state.filterCamera)
      }

      result.sort((a, b) => {
        let cmp = 0
        switch (state.sortBy) {
          case 'date_taken':
            cmp = dayjs(a.date_taken).unix() - dayjs(b.date_taken).unix()
            break
          case 'date_uploaded':
            cmp = dayjs(a.date_uploaded).unix() - dayjs(b.date_uploaded).unix()
            break
          case 'file_name':
            cmp = a.file_name.localeCompare(b.file_name)
            break
          case 'file_size':
            cmp = a.file_size - b.file_size
            break
          case 'rating':
            cmp = a.rating - b.rating
            break
        }
        return state.sortOrder === 'desc' ? -cmp : cmp
      })

      return result
    },

    timelinePhotos(state) {
      const groups = {}
      const photos = state.photos.filter(p => !p.is_deleted && !p.is_archived)
      photos.forEach(p => {
        const dateKey = dayjs(p.date_taken).format('YYYY-MM-DD')
        if (!groups[dateKey]) {
          groups[dateKey] = {
            date: dateKey,
            displayDate: dayjs(p.date_taken).format('YYYY年MM月DD日 dddd'),
            photos: []
          }
        }
        groups[dateKey].photos.push(p)
      })
      return Object.values(groups).sort((a, b) => dayjs(b.date).unix() - dayjs(a.date).unix())
    },

    cameraList(state) {
      const cameras = new Set(state.photos.map(p => p.camera_model).filter(Boolean))
      return [...cameras]
    },

    storageStats(state) {
      const total = state.photos.reduce((sum, p) => sum + p.file_size, 0)
      const active = state.photos.filter(p => !p.is_deleted).reduce((sum, p) => sum + p.file_size, 0)
      const deleted = state.photos.filter(p => p.is_deleted).reduce((sum, p) => sum + p.file_size, 0)
      return { total, active, deleted }
    },

    photoStats(state) {
      return {
        total: state.photos.length,
        active: state.photos.filter(p => !p.is_deleted).length,
        favorites: state.photos.filter(p => p.is_favorite && !p.is_deleted).length,
        deleted: state.photos.filter(p => p.is_deleted).length,
        albums: state.albums.length,
        tags: state.tags.length,
        people: state.people.length
      }
    }
  },

  actions: {
    getPhoto(id) {
      return this.photos.find(p => p.id === id)
    },

    getAlbum(id) {
      return this.albums.find(a => a.id === id)
    },

    getAlbumPhotos(albumId) {
      const album = this.albums.find(a => a.id === albumId)
      if (!album) return []
      if (album.type === 'smart' && album.smart_rules) {
        return this.filteredPhotosByRules(album.smart_rules)
      }
      return this.activePhotos.slice(0, album.photo_count)
    },

    filteredPhotosByRules(rules) {
      let result = this.activePhotos
      if (rules.tags) {
        result = result.filter(p => rules.tags.some(t => p.tags?.includes(t)))
      }
      if (rules.people) {
        result = result.filter(p => rules.people.some(name => p.tags?.includes(name)))
      }
      return result
    },

    toggleFavorite(photoId) {
      const photo = this.photos.find(p => p.id === photoId)
      if (photo) {
        photo.is_favorite = !photo.is_favorite
      }
    },

    toggleSelect(photoId) {
      const idx = this.selectedPhotos.indexOf(photoId)
      if (idx === -1) {
        this.selectedPhotos.push(photoId)
      } else {
        this.selectedPhotos.splice(idx, 1)
      }
    },

    selectAll() {
      this.selectedPhotos = this.filteredPhotos.map(p => p.id)
    },

    clearSelection() {
      this.selectedPhotos = []
    },

    deletePhotos(photoIds) {
      photoIds.forEach(id => {
        const photo = this.photos.find(p => p.id === id)
        if (photo) {
          photo.is_deleted = true
          photo.deleted_at = dayjs().format('YYYY-MM-DD HH:mm:ss')
        }
      })
      this.selectedPhotos = []
    },

    restorePhotos(photoIds) {
      photoIds.forEach(id => {
        const photo = this.photos.find(p => p.id === id)
        if (photo) {
          photo.is_deleted = false
          photo.deleted_at = null
        }
      })
    },

    permanentDeletePhotos(photoIds) {
      this.photos = this.photos.filter(p => !photoIds.includes(p.id))
    },

    emptyTrash() {
      this.photos = this.photos.filter(p => !p.is_deleted)
    },

    addPhoto(photo) {
      this.photos.unshift(photo)
    },

    addPhotos(photos) {
      this.photos = [...photos, ...this.photos]
    },

    createAlbum(data) {
      const album = {
        id: `album-${Date.now()}`,
        user_id: 'user-1',
        parent_id: null,
        name: data.name,
        description: data.description || '',
        cover_photo_id: null,
        cover_url: '',
        type: data.type || 'normal',
        sort_order: this.albums.length,
        is_public: false,
        smart_rules: data.smart_rules || null,
        photo_count: 0,
        created_at: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        updated_at: dayjs().format('YYYY-MM-DD HH:mm:ss')
      }
      this.albums.push(album)
      return album
    },

    updateAlbum(albumId, data) {
      const album = this.albums.find(a => a.id === albumId)
      if (album) {
        Object.assign(album, data, { updated_at: dayjs().format('YYYY-MM-DD HH:mm:ss') })
      }
    },

    deleteAlbum(albumId) {
      this.albums = this.albums.filter(a => a.id !== albumId)
    },

    createTag(name) {
      const exists = this.tags.find(t => t.name === name)
      if (exists) return exists
      const tag = {
        id: `tag-${Date.now()}`,
        name,
        parent_id: null,
        color: `#${Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0')}`,
        icon: '',
        usage_count: 0,
        is_ai_generated: false,
        created_at: dayjs().format('YYYY-MM-DD HH:mm:ss')
      }
      this.tags.push(tag)
      return tag
    },

    deleteTag(tagId) {
      this.tags = this.tags.filter(t => t.id !== tagId)
    },

    addTagToPhoto(photoId, tagName) {
      const photo = this.photos.find(p => p.id === photoId)
      if (photo && !photo.tags.includes(tagName)) {
        photo.tags.push(tagName)
        const tag = this.tags.find(t => t.name === tagName)
        if (tag) tag.usage_count++
      }
    },

    removeTagFromPhoto(photoId, tagName) {
      const photo = this.photos.find(p => p.id === photoId)
      if (photo) {
        photo.tags = photo.tags.filter(t => t !== tagName)
      }
    },

    updatePhotoRating(photoId, rating) {
      const photo = this.photos.find(p => p.id === photoId)
      if (photo) {
        photo.rating = rating
      }
    },

    setViewMode(mode) {
      this.viewMode = mode
    },

    setGridSize(size) {
      this.gridSize = size
    },

    setSortBy(field) {
      this.sortBy = field
    },

    setSortOrder(order) {
      this.sortOrder = order
    },

    setSearchQuery(query) {
      this.searchQuery = query
    },

    setFilterTags(tags) {
      this.filterTags = tags
    },

    setFilterDateRange(range) {
      this.filterDateRange = range
    },

    setFilterCamera(camera) {
      this.filterCamera = camera
    },

    clearFilters() {
      this.searchQuery = ''
      this.filterTags = []
      this.filterDateRange = null
      this.filterCamera = ''
    }
  }
})
