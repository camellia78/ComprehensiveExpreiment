import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const doLogin = async (username, password) => {
    const data = await login({ username, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data))
    localStorage.setItem('role', data.role)
    user.value = data
    return data
  }

  const doLogout = async () => {
    try { await logout() } catch (e) { /* ignore */ }
    localStorage.clear()
    user.value = null
  }

  return { user, doLogin, doLogout }
})
