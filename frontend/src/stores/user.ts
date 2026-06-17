import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface User {
  id: number
  email: string
  nickname: string
  avatar: string
  bio?: string
  streakDays?: number
  totalDays?: number
}

const TOKEN_KEY = 'smartvocab_token'
const USER_KEY = 'smartvocab_user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<User | null>(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  function setLogin(t: string, u: User) {
    token.value = t
    user.value = u
    localStorage.setItem(TOKEN_KEY, t)
    localStorage.setItem(USER_KEY, JSON.stringify(u))
  }

  function setUser(u: User) {
    user.value = u
    localStorage.setItem(USER_KEY, JSON.stringify(u))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function restore() {
    if (!token.value) token.value = localStorage.getItem(TOKEN_KEY) || ''
    if (!user.value) {
      const raw = localStorage.getItem(USER_KEY)
      if (raw) user.value = JSON.parse(raw)
    }
  }

  return { token, user, setLogin, setUser, logout, restore }
})
