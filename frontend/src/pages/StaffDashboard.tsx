import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ClipboardCheck, Filter, RefreshCw, UserRound } from 'lucide-react'
import { useMemo, useState } from 'react'
import { apiRequest } from '../api/client'
import { IssueCard } from '../components/IssueCard'
import { PageHeader } from '../components/Layout'
import { formatStatus, StatusBadge } from '../components/StatusBadge'
import type { IssuePage, IssueStatus, User } from '../types'

const transitions: Record<IssueStatus, IssueStatus[]> = { REPORTED: ['UNDER_REVIEW', 'REJECTED', 'DUPLICATE'], UNDER_REVIEW: ['IN_PROGRESS', 'REJECTED', 'DUPLICATE'], IN_PROGRESS: ['RESOLVED', 'REJECTED', 'DUPLICATE'], RESOLVED: [], REJECTED: [], DUPLICATE: [] }

export function StaffDashboard() {
  const queryClient = useQueryClient()
  const [status, setStatus] = useState<IssueStatus | ''>('')
  const [selectedId, setSelectedId] = useState<string>()
  const [comment, setComment] = useState('')
  const params = status ? `?size=100&status=${status}` : '?size=100'
  const reports = useQuery({ queryKey: ['issues', 'staff', params], queryFn: () => apiRequest<IssuePage>(`/api/issues${params}`) })
  const staff = useQuery({ queryKey: ['staff-members'], queryFn: () => apiRequest<User[]>('/api/staff/members') })
  const selected = reports.data?.content.find((issue) => issue.id === selectedId) ?? reports.data?.content[0]
  const change = useMutation({ mutationFn: (nextStatus: IssueStatus) => apiRequest(`/api/issues/${selected?.id}/status`, { method: 'PATCH', body: JSON.stringify({ status: nextStatus, comment }) }), onSuccess: () => { setComment(''); void queryClient.invalidateQueries({ queryKey: ['issues'] }); void queryClient.invalidateQueries({ queryKey: ['issue'] }) } })
  const assign = useMutation({ mutationFn: (staffId: string) => apiRequest(`/api/issues/${selected?.id}/assignments`, { method: 'POST', body: JSON.stringify({ staffId }) }), onSuccess: () => { void queryClient.invalidateQueries({ queryKey: ['issues'] }) } })
  const transitionOptions = useMemo(() => selected ? transitions[selected.status] : [], [selected])
 return <div className="section-shell page-shell"><PageHeader eyebrow="OPERATIONS DESK" title="Move the work forward." description="Review community reports, assign ownership, and keep every status change visible." action={<button className="button button-outline" onClick={() => void reports.refetch()}><RefreshCw size={16} /> Refresh</button>} /><div className="staff-layout"><section><div className="staff-filter"><Filter size={16} /><select value={status} onChange={(event) => setStatus(event.target.value as IssueStatus | '')}><option value="">All active work</option><option value="REPORTED">Reported</option><option value="UNDER_REVIEW">Under review</option><option value="IN_PROGRESS">In progress</option><option value="RESOLVED">Resolved</option></select><span>{reports.data?.totalElements ?? 0} reports</span></div>{reports.isLoading && <div className="page-state">Loading operations queue…</div>}<div className="staff-list">{reports.data?.content.map((issue) => <button className={`staff-list-item ${selected?.id === issue.id ? 'selected' : ''}`} key={issue.id} onClick={() => setSelectedId(issue.id)}><div><strong>{issue.title}</strong><small>{issue.locationLabel || 'Location not labelled'}</small></div><StatusBadge status={issue.status} /></button>)}</div></section><aside className="staff-detail">{selected ? <><div className="staff-detail-heading"><div><p className="eyebrow">SELECTED REPORT</p><h2>{selected.title}</h2></div><StatusBadge status={selected.status} /></div><IssueCard issue={selected} compact /><label className="staff-label"><span><UserRound size={15} /> Assign to</span><select value={selected.assignedStaffId ?? ''} onChange={(event) => event.target.value && assign.mutate(event.target.value)}><option value="">Unassigned</option>{staff.data?.map((member) => <option value={member.id} key={member.id}>{member.name}</option>)}</select></label><label className="staff-label"><span>Public progress note</span><textarea value={comment} onChange={(event) => setComment(event.target.value)} placeholder="Explain what happens next…" rows={3} /></label><div className="transition-actions"><span>Update status</span><div>{transitionOptions.map((nextStatus) => <button className="button button-small button-dark" key={nextStatus} disabled={change.isPending} onClick={() => change.mutate(nextStatus)}>{formatStatus(nextStatus)}</button>)}{!transitionOptions.length && <small>This report has no further workflow transitions.</small>}</div></div>{(change.isError || assign.isError) && <div className="form-error">Unable to save this update. Please try again.</div>}</> : <div className="empty-panel">Select a report to manage it.</div>}</aside></div><div className="staff-note"><ClipboardCheck size={18} /><span>Status changes are validated and recorded in the public timeline before any live update is broadcast.</span></div></div>
}
