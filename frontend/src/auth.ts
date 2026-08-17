import type { LoginResult } from './types'

const ACCESS_KEY = 'jachwi.accessToken'
const REFRESH_KEY = 'jachwi.refreshToken'
const PKCE_KEY = 'jachwi.pkce'
const NONCE_KEY = 'jachwi.nonce'
const STATE_KEY = 'jachwi.state'
const redirectUri = () => import.meta.env.VITE_GOOGLE_REDIRECT_URI || `${location.origin}/oauth/google/callback`

function randomValue(bytes = 32) {
  const value = crypto.getRandomValues(new Uint8Array(bytes))
  return btoa(String.fromCharCode(...value)).replaceAll('+', '-').replaceAll('/', '_').replaceAll('=', '')
}

async function sha256(value: string) {
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(value))
  return btoa(String.fromCharCode(...new Uint8Array(digest))).replaceAll('+', '-').replaceAll('/', '_').replaceAll('=', '')
}

export const tokenStore = {
  access: () => sessionStorage.getItem(ACCESS_KEY),
  refresh: () => sessionStorage.getItem(REFRESH_KEY),
  save: (tokens: { accessToken: string; refreshToken: string }) => {
    sessionStorage.setItem(ACCESS_KEY, tokens.accessToken)
    sessionStorage.setItem(REFRESH_KEY, tokens.refreshToken)
  },
  clear: () => { sessionStorage.removeItem(ACCESS_KEY); sessionStorage.removeItem(REFRESH_KEY) },
}

export async function beginGoogleLogin() {
  const verifier = randomValue(64)
  const nonce = randomValue()
  const state = randomValue()
  sessionStorage.setItem(PKCE_KEY, verifier); sessionStorage.setItem(NONCE_KEY, nonce); sessionStorage.setItem(STATE_KEY, state)
  const query = new URLSearchParams({
    client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID,
    redirect_uri: redirectUri(), response_type: 'code',
    scope: 'openid email profile', code_challenge: await sha256(verifier), code_challenge_method: 'S256',
    nonce, state, prompt: 'select_account',
  })
  location.assign(`https://accounts.google.com/o/oauth2/v2/auth?${query}`)
}

export async function completeGoogleLogin(): Promise<LoginResult> {
  const query = new URLSearchParams(location.search)
  const code = query.get('code'); const state = query.get('state')
  const expectedState = sessionStorage.getItem(STATE_KEY)
  const verifier = sessionStorage.getItem(PKCE_KEY); const nonce = sessionStorage.getItem(NONCE_KEY)
  if (!code || !state || state !== expectedState || !verifier || !nonce) throw new Error('Google 로그인 콜백 정보가 올바르지 않습니다.')
  const response = await fetch('/api/auth/google', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ authorizationCode: code, codeVerifier: verifier, nonce, redirectUri: redirectUri() }),
  })
  const body = await response.json()
  if (!response.ok) throw new Error(`${body.code ?? response.status}: ${body.message ?? '로그인 실패'}`)
  tokenStore.save(body.data)
  sessionStorage.removeItem(PKCE_KEY); sessionStorage.removeItem(NONCE_KEY); sessionStorage.removeItem(STATE_KEY)
  return body.data
}
