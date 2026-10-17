import { describe, expect, it, vi } from 'vitest'
import { apiRequest } from './client'

describe('apiRequest CSRF handling', () => {
  it('uses the JSON CSRF token when the backend cookie is not readable by the frontend origin', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify({ token: 'csrf-from-backend-json' }), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ ok: true }), { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    await apiRequest('/api/issues', { method: 'POST', body: '{}' })

    const secondRequest = fetchMock.mock.calls[1]?.[1] as RequestInit
    expect(secondRequest.credentials).toBe('include')
    expect((secondRequest.headers as Headers).get('X-XSRF-TOKEN')).toBe('csrf-from-backend-json')
    vi.unstubAllGlobals()
  })
})
