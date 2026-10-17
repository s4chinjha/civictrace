import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, CalendarDays, CheckCircle2, MapPin, ThumbsUp } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { apiRequest } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { IssueMap } from '../components/IssueMap'
import { StatusBadge } from '../components/StatusBadge'
import { Timeline } from '../components/Timeline'
import type { IssueDetail } from '../types'

export function IssueDetailPage() {
  const { issueId } = useParams<{ issueId: string }>()
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const issue = useQuery({ queryKey: ['issue', issueId], queryFn: () => apiRequest<IssueDetail>(`/api/issues/${issueId}`), enabled: Boolean(issueId) })
  const support = useMutation({ mutationFn: () => apiRequest<IssueDetail>(`/api/issues/${issueId}/support`, { method: 'POST' }), onSuccess: (data) => { queryClient.setQueryData(['issue', issueId], data); void queryClient.invalidateQueries({ queryKey: ['issues'] }) } })

  if (issue.isLoading) return <div className="page-state">Loading report…</div>
  if (issue.isError || !issue.data) return <div className="section-shell page-shell"><div className="error-panel"><strong>This report could not be found.</strong><Link to="/issues">Return to explorer</Link></div></div>
  const { issue: record, updates } = issue.data
  return <div className="section-shell page-shell detail-shell">
    <Link className="back-link" to="/issues"><ArrowLeft size={16} /> Back to explorer</Link>
    <div className="detail-layout">
      <article className="detail-main">
        <div className="detail-kicker"><span className="category-label">{record.category}</span><StatusBadge status={record.status} /></div>
        <h1>{record.title}</h1>
        <p className="detail-description">{record.description}</p>
        <div className="detail-meta-grid"><div><MapPin size={17} /><span><small>Location</small>{record.locationLabel || `${record.latitude}, ${record.longitude}`}</span></div><div><CalendarDays size={17} /><span><small>Reported</small>{new Date(record.createdAt).toLocaleDateString()}</span></div><div><ThumbsUp size={17} /><span><small>Community support</small>{record.supportCount} people</span></div></div>
        <div className="detail-map"><IssueMap issues={[record]} /></div>
        <section className="timeline-section"><div className="section-title-row"><div><p className="eyebrow">TRANSPARENT PROGRESS</p><h2>Status timeline</h2></div><span className="timeline-count">{updates.length} updates</span></div><Timeline updates={updates} /></section>
      </article>
      <aside className="detail-aside"><div className="support-card"><div className="support-icon"><ThumbsUp size={22} /></div><strong>{record.supportCount}</strong><span>neighbours support this report</span>{user?.role === 'RESIDENT' ? <button className="button button-primary full-width" disabled={record.supportedByCurrentUser || support.isPending} onClick={() => support.mutate()}>{record.supportedByCurrentUser ? <><CheckCircle2 size={16} /> Supported</> : 'Support this report'}</button> : !user ? <Link className="button button-primary full-width" to="/login">Sign in to support</Link> : null}{support.isError && <small className="form-error">{support.error.message}</small>}</div><div className="detail-note"><strong>Why support matters</strong><p>Community support helps civic teams identify recurring issues and prioritize work where it matters most.</p></div></aside>
    </div>
  </div>
}
