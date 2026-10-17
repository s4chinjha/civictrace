import { useQuery } from '@tanstack/react-query'
import { ArrowRight, FilePlus2, Inbox, TrendingUp } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiRequest } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { IssueCard } from '../components/IssueCard'
import { PageHeader } from '../components/Layout'
import type { AnalyticsSummary, IssuePage } from '../types'

export function ResidentDashboard() {
  const { user } = useAuth()
  const reports = useQuery({ queryKey: ['my-issues'], queryFn: () => apiRequest<IssuePage>('/api/my/issues?size=50') })
  const stats = useQuery({ queryKey: ['analytics', 'summary'], queryFn: () => apiRequest<AnalyticsSummary>('/api/analytics/summary') })
  const issues = reports.data?.content ?? []
  return <div className="section-shell page-shell"><PageHeader eyebrow="YOUR CIVIC TRAIL" title={`Good to see you, ${user?.name.split(' ')[0]}.`} description="Keep an eye on the issues you have raised and the progress your community is building." action={<Link className="button button-primary" to="/report"><FilePlus2 size={17} /> New report</Link>} /><div className="dashboard-metrics"><Metric icon={<Inbox />} label="My reports" value={reports.data?.totalElements ?? '—'} /><Metric icon={<TrendingUp />} label="Community reports" value={stats.data?.totalReports ?? '—'} /><Metric icon={<ArrowRight />} label="Open reports" value={stats.data?.openReports ?? '—'} /></div><div className="section-title-row dashboard-section-title"><div><p className="eyebrow">YOUR SUBMISSIONS</p><h2>Reports you started</h2></div><Link className="text-link" to="/issues">Explore all reports <ArrowRight size={16} /></Link></div>{reports.isLoading && <div className="page-state">Loading your reports…</div>}{reports.isError && <div className="error-panel">Unable to load your reports right now.</div>}{!reports.isLoading && !issues.length && <div className="empty-panel large-empty"><FilePlus2 size={24} /><strong>Your civic trail starts here.</strong><span>Report the first issue you notice in your neighbourhood.</span><Link className="button button-primary" to="/report">Create a report</Link></div>}{issues.length > 0 && <div className="issue-grid">{issues.map((issue) => <IssueCard issue={issue} key={issue.id} />)}</div>}</div>
}

function Metric({ icon, label, value }: { icon: React.ReactNode; label: string; value: number | string }) { return <div className="metric-card"><span className="metric-icon">{icon}</span><span>{label}</span><strong>{value}</strong></div> }
