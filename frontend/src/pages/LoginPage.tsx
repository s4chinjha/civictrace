import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  async function submit(event: React.FormEvent) {
    event.preventDefault(); setError(''); setBusy(true)
    try { const user = await login({ email, password }); navigate(user.role === 'STAFF' ? '/staff' : (location.state as { from?: string } | null)?.from ?? '/dashboard') } catch (err) { setError(err instanceof ApiError && err.status === 401 ? 'Email or password is incorrect.' : err instanceof Error ? err.message : 'Unable to sign in.') } finally { setBusy(false) }
  }
  return <AuthLayout eyebrow="WELCOME BACK" title="Sign in to CivicTrace" subtitle="Follow your reports, support your neighbours, and keep the civic pulse moving."><form className="auth-form" onSubmit={submit}><label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required autoComplete="email" /></label><label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" /></label>{error && <div className="form-error">{error}</div>}<button className="button button-primary full-width" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button><p className="auth-switch">New to CivicTrace? <Link to="/register">Create an account</Link></p></form></AuthLayout>
}

export function AuthLayout({ eyebrow, title, subtitle, children }: { eyebrow: string; title: string; subtitle: string; children: React.ReactNode }) {
  return <div className="auth-page"><div className="auth-panel"><Link className="brand auth-brand" to="/"><span className="brand-mark">CT</span><span>Civic<span>Trace</span></span></Link><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p className="auth-subtitle">{subtitle}</p>{children}</div><div className="auth-aside"><div className="auth-quote">“The best civic systems make it easy to care, and impossible for progress to disappear.”</div><span>— CivicTrace principle</span></div></div>
}
