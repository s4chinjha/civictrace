import { useQuery } from '@tanstack/react-query'
import { ArrowRight, BarChart3, CheckCircle2, MapPinned, Megaphone, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiRequest } from '../api/client'
import type { AnalyticsSummary } from '../types'

export function LandingPage() {
  const summary = useQuery({
    queryKey: ['analytics', 'summary'],
    queryFn: () => apiRequest<AnalyticsSummary>('/api/analytics/summary'),
  })
  const stats = summary.data

  return (
    <div>
      <section className="hero section-shell">
        <div className="hero-copy">
          <div className="hero-kicker"><span className="pulse-dot" /> A clearer way to care for where we live</div>
          <h1>Make every report <em>count.</em></h1>
          <p className="hero-lede">CivicTrace brings residents and civic teams together to spot problems, build support, and follow every fix from first report to resolution.</p>
          <div className="hero-actions">
            <Link className="button button-primary" to="/issues">Explore reports <ArrowRight size={17} /></Link>
            <Link className="button button-outline" to="/register">Report an issue</Link>
          </div>
          <div className="hero-note"><CheckCircle2 size={16} /> Open data. Visible progress. Stronger neighbourhoods.</div>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="hero-grid" />
          <div className="hero-pin hero-pin-main"><MapPinned size={22} /><span>Streetlight fixed</span></div>
          <div className="hero-pin hero-pin-small"><Megaphone size={17} /><span>12 neighbours supporting</span></div>
          <div className="hero-orbit orbit-one" /><div className="hero-orbit orbit-two" />
          <div className="hero-art-label"><span>LIVE CIVIC PULSE</span><strong>03</strong><small>active areas</small></div>
        </div>
      </section>

      <section className="stats-strip section-shell">
        <div><strong>{stats?.totalReports ?? '—'}</strong><span>Reports tracked</span></div>
        <div><strong>{stats?.openReports ?? '—'}</strong><span>Open right now</span></div>
        <div><strong>{stats?.resolvedReports ?? '—'}</strong><span>Resolved by teams</span></div>
        <div><strong>{stats ? `${Math.round(stats.averageResolutionHours)}h` : '—'}</strong><span>Average resolution</span></div>
      </section>

      <section className="section-shell story-section">
        <div className="section-intro"><p className="eyebrow">HOW IT WORKS</p><h2>Small signals. Shared action.</h2><p>One report is a start. A transparent trail of support and progress helps the right people act faster.</p></div>
        <div className="feature-grid">
          <Feature icon={<Megaphone />} number="01" title="Spot it" text="Report potholes, waste, water leaks, and more with a precise location." />
          <Feature icon={<Users />} number="02" title="Support it" text="Add your voice to existing reports so recurring problems rise to the top." />
          <Feature icon={<BarChart3 />} number="03" title="Track it" text="See what is happening next through a public timeline and live dashboard." />
        </div>
      </section>

      <section className="cta-band section-shell"><div><p className="eyebrow">YOUR NEIGHBOURHOOD, IN VIEW</p><h2>Ready to make a difference?</h2></div><Link className="button button-light" to="/issues">See the civic pulse <ArrowRight size={17} /></Link></section>
    </div>
  )
}

function Feature({ icon, number, title, text }: { icon: React.ReactNode; number: string; title: string; text: string }) {
  return <article className="feature-card"><div className="feature-top"><span className="feature-icon">{icon}</span><span className="feature-number">{number}</span></div><h3>{title}</h3><p>{text}</p></article>
}
