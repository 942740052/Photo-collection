import { defineStore } from 'pinia'
import { mockUsers } from '../mock/data'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: localStorage.getItem('token') || null,
    users: []
  }),

  getters: {
    isLoggedIn: state => !!state.token,
    isAdmin: state => state.user?.role === 'admin',
    currentUser: state => state.user
  },

  actions: {
    async login(credentials) {
      if (credentials.username === 'admin' && credentials.password === 'admin123') {
        const user = mockUsers[0]
        const token = 'mock_token_' + Date.now()
        this.user = user
        this.token = token
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        return { success: true, user }
      } else if (credentials.username && credentials.password) {
        const user = mockUsers.find(u => u.username === credentials.username)
        if (user && user.status === 'active') {
          const token = 'mock_token_' + Date.now()
          this.user = user
          this.token = token
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify(user))
          return { success: true, user }
        }
        return { success: false, message: '用户名或密码错误' }
      }
      return { success: false, message: '请输入用户名和密码' }
    },

    async register(data) {
      const exists = mockUsers.find(u => u.username === data.username)
      if (exists) {
        return { success: false, message: '用户名已存在' }
      }
      const newUser = {
        id: `user-${Date.now()}`,
        username: data.username,
        email: data.email || '',
        display_name: data.displayName || data.username,
        avatar_url: '',
        role: 'user',
        status: 'active',
        storage_quota: 53687091200,
        storage_used: 0,
        two_factor_enabled: false,
        created_at: new Date().toISOString(),
        last_login_at: null
      }
      mockUsers.push(newUser)
      return { success: true, user: newUser }
    },

    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    async fetchUsers() {
      this.users = [...mockUsers]
      return this.users
    },

    async updateUser(userId, data) {
      const idx = mockUsers.findIndex(u => u.id === userId)
      if (idx !== -1) {
        Object.assign(mockUsers[idx], data)
        if (this.user?.id === userId) {
          this.user = mockUsers[idx]
          localStorage.setItem('user', JSON.stringify(this.user))
        }
        return { success: true }
      }
      return { success: false, message: '用户不存在' }
    },

    async deleteUser(userId) {
      const idx = mockUsers.findIndex(u => u.id === userId)
      if (idx !== -1) {
        mockUsers.splice(idx, 1)
        return { success: true }
      }
      return { success: false, message: '用户不存在' }
    },

    updateProfile(data) {
      if (this.user) {
        Object.assign(this.user, data)
        localStorage.setItem('user', JSON.stringify(this.user))
      }
    }
  }
})
