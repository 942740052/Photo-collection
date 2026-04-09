import { useState, useEffect } from 'react'
import { supabase } from '../supabase/client'
import { User } from '@supabase/supabase-js'

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // 检查用户是否已登录
    const checkUser = async () => {
      const { data: { user } } = await supabase.auth.getUser()
      setUser(user)
      setLoading(false)
    }

    checkUser()

    // 监听认证状态变化
    const { data: { subscription } } = supabase.auth.onAuthStateChange((event, session) => {
      setUser(session?.user || null)
      setLoading(false)
    })

    return () => subscription.unsubscribe()
  }, [])

  // 登录函数
  const login = async (email: string, password: string) => {
    const { error } = await supabase.auth.signInWithPassword({ email, password })
    return error
  }

  // 注册函数
  const register = async (email: string, password: string) => {
    const { error } = await supabase.auth.signUp({ email, password })
    return error
  }

  // 登出函数
  const logout = async () => {
    const { error } = await supabase.auth.signOut()
    return error
  }

  return { user, loading, login, register, logout }
}