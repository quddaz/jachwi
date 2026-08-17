import { tokenStore } from './auth'
import type { ApiEnvelope, TokenPair } from './types'

export type ApiLog = { at: string; method: string; path: string; status: number }
let logger: (log: ApiLog) => void = () => undefined
export const setApiLogger = (next: typeof logger) => { logger = next }

async function rotate() {
  const refreshToken = tokenStore.refresh()
  if (!refreshToken) return false
  const response = await fetch('/api/auth/tokens', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ refreshToken }) })
  const body = await response.json().catch(() => null) as ApiEnvelope<TokenPair> | null
  logger({ at: new Date().toLocaleTimeString(), method: 'POST', path: '/api/auth/tokens', status: response.status })
  if (!response.ok || !body) { tokenStore.clear(); return false }
  tokenStore.save(body.data)
  return true
}

export async function api<T>(path: string, init: RequestInit = {}, retry = true): Promise<T> {
  const headers = new Headers(init.headers)
  if (!(init.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  const accessToken = tokenStore.access()
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)
  const method = init.method ?? 'GET'
  const response = await fetch(path, { ...init, headers })
  if (response.status === 401 && retry && await rotate()) return api<T>(path, init, false)
  const body = response.status === 204 ? null : await response.json().catch(() => null)
  logger({ at: new Date().toLocaleTimeString(), method, path, status: response.status })
  if (!response.ok) throw new Error(`${body?.code ?? response.status}: ${body?.message ?? '요청 실패'}`)
  return body?.data as T
}
