import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { apiRequest } from '../api/client'
import type { LoginRequest, RegisterRequest } from './authTypes'
import type { User } from '../types'

interface AuthContextValue {
  user: User | null
  loading: boolean
  login: (request: LoginRequest) => Promise<User>
  register: (request: RegisterRequest) => Promise<User>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    apiRequest<User>('/api/me').then(setUser).catch(() => setUser(null)).finally(() => setLoading(false))
  }, [])

  const value = useMemo<AuthContextValue>(() => ({
    user,
    loading,
    login: async (request) => {
      const loggedIn = await apiRequest<User>('/api/auth/login', { method: 'POST', body: JSON.stringify(request) })
      setUser(loggedIn)
      return loggedIn
    },
    register: (request) => apiRequest<User>('/api/auth/register', { method: 'POST', body: JSON.stringify(request) }),
    logout: async () => {
      await apiRequest<void>('/api/auth/logout', { method: 'POST' })
      setUser(null)
    },
  }), [loading, user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
