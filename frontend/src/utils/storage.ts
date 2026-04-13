const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

export const getRefreshToken = (): string | null => {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export const setRefreshToken = (token: string): void => {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

export const removeRefreshToken = (): void => {
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export const getUser = (): any => {
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export const setUser = (user: any): void => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const removeUser = (): void => {
  localStorage.removeItem(USER_KEY)
}

export const clearAll = (): void => {
  removeToken()
  removeRefreshToken()
  removeUser()
}
