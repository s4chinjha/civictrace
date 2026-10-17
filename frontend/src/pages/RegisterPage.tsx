import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AuthLayout } from './LoginPage'

export function RegisterPage() {
  const { register, login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  async function submit(event: React.FormEvent) {
    event.preventDefault(); setError(''); setBusy(true)
    try { await register(form); await login({ email: form.email, password: form.password }); navigate('/dashboard') } catch (err) { setError(err instanceof ApiError ? err.message : err instanceof Error ? err.message : 'Unable to create your account.') } finally { setBusy(false) }
  }
  return <AuthLayout eyebrow="JOIN THE PULSE" title="Create your resident account" subtitle="Report what you see and help your community turn attention into action."><form className="auth-form" onSubmit={submit}><label>Your name<input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required maxLength={120} /></label><label>Email<input type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} required autoComplete="email" /></label><label>Password<input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required minLength={8} autoComplete="new-password" /><small>Use at least 8 characters.</small></label>{error && <div className="form-error">{error}</div>}<button className="button button-primary full-width" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button><p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p></form></AuthLayout>
}
