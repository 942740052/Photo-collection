import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types/user'
import { authApi } from '@/api/auth'
import { getToken, setToken, removeToken, setUser, removeUser, getUser } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<User | null>(getUser())

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    setToken(res.data.token)
    setUser(res.data.user)
    return res
  }

  const register = async (username: string, password: string, email: string, nickname?: string) => {
    const res = await authApi.register({ username, password, email, nickname })
    token.value = res.data.token
    user.value = res.data.user
    setToken(res.data.token)
    setUser(res.data.user)
    return res
  }

  const logout = () => {
    token.value = null
    user.value = null
    removeToken()
    removeUser()
  }

  const getProfile = async () => {
    const res = await authApi.getProfile()
    user.value = res.data
    setUser(res.data)
    return res
  }

  const updateProfile = async (data: Partial<User>) => {
    const res = await authApi.updateProfile(data)
    user.value = res.data
    setUser(res.data)
    return res
  }

  return {
    token,
    user,
    login,
    register,
    logout,
    getProfile,
    updateProfile,
  }
})
