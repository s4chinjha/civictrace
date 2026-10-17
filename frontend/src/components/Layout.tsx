import { LogIn, LogOut, Map, Menu, Plus, ShieldCheck, X } from 'lucide-react'
import { useState } from 'react'
import { NavLink, Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function Layout() {
  const { user, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    setOpen(false)
    navigate('/')
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link className="brand" to="/" onClick={() => setOpen(false)}>
          <span className="brand-mark"><Map size={19} /></span>
          <span>Civic<span>Trace</span></span>
        </Link>
        <button className="mobile-menu" aria-label="Toggle navigation" onClick={() => setOpen((value) => !value)}>
          {open ? <X size={21} /> : <Menu size={21} />}
        </button>
        <nav className={`nav-links ${open ? 'nav-open' : ''}`}>
          <NavLink to="/issues" onClick={() => setOpen(false)}>Explore</NavLink>
          <NavLink to="/analytics" onClick={() => setOpen(false)}>Insights</NavLink>
          {user?.role === 'RESIDENT' && <NavLink to="/dashboard" onClick={() => setOpen(false)}>My reports</NavLink>}
          {user?.role === 'STAFF' && <NavLink to="/staff" onClick={() => setOpen(false)}>Operations</NavLink>}
          {user ? (
            <div className="nav-account">
              <span className="avatar">{user.name.slice(0, 1).toUpperCase()}</span>
              <span className="account-name">{user.name}</span>
              <button className="button button-ghost button-small" onClick={handleLogout}><LogOut size={15} /> Log out</button>
            </div>
          ) : (
            <Link className="button button-dark button-small" to="/login" onClick={() => setOpen(false)}><LogIn size={15} /> Sign in</Link>
          )}
        </nav>
      </header>
      <main><Outlet /></main>
      <footer className="footer">
        <span>© {new Date().getFullYear()} CivicTrace</span>
        <span>Built for clearer, kinder neighbourhoods.</span>
      </footer>
    </div>
  )
}

export function PageHeader({ eyebrow, title, description, action }: { eyebrow?: string; title: string; description?: string; action?: React.ReactNode }) {
  return (
    <div className="page-header">
      <div>
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h1>{title}</h1>
        {description && <p className="page-description">{description}</p>}
      </div>
      {action}
    </div>
  )
}
