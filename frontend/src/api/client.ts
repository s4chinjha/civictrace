import type { ApiErrorShape } from '../types'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080'
let csrfToken: string | undefined

export class ApiError extends Error {
  status: number
  fieldErrors: Record<string, string>

  constructor(status: number, body: ApiErrorShape) {
    super(body.message ?? 'Something went wrong. Please try again.')
    this.status = status
    this.fieldErrors = body.fieldErrors ?? {}
  }
}

function readCookie(name: string): string | undefined {
  const prefix = `${name}=`
  return document.cookie.split('; ').find((cookie) => cookie.startsWith(prefix))?.slice(prefix.length)
}

async function ensureCsrfToken(): Promise<string | undefined> {
  const response = await fetch(`${API_BASE_URL}/api/auth/csrf`, { credentials: 'include' })
  let responseToken: string | undefined
  try {
    responseToken = (await response.json() as { token?: string }).token
  } catch {
    responseToken = undefined
  }
  csrfToken = responseToken ?? (readCookie('XSRF-TOKEN') ? decodeURIComponent(readCookie('XSRF-TOKEN')!) : undefined)
  return csrfToken
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const method = (init.method ?? 'GET').toUpperCase()
  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const csrf = await ensureCsrfToken()
    if (csrf) headers.set('X-XSRF-TOKEN', csrf)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...init, method, headers, credentials: 'include' })
  if (!response.ok) {
    let body: ApiErrorShape = {}
    try { body = await response.json() as ApiErrorShape } catch { /* empty response */ }
    throw new ApiError(response.status, body)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export function websocketUrl(): string {
  const configured = (import.meta.env.VITE_WS_URL as string | undefined)
  if (configured) return configured
  return API_BASE_URL.replace(/^http/, 'ws') + '/ws'
}
