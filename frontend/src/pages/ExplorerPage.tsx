import { useQuery } from '@tanstack/react-query'
import { ChevronLeft, ChevronRight, List, Map as MapIcon, Search, SlidersHorizontal } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiRequest } from '../api/client'
import { IssueCard } from '../components/IssueCard'
import { IssueMap } from '../components/IssueMap'
import { PageHeader } from '../components/Layout'
import type { IssueCategory, IssuePage, IssueStatus } from '../types'

const categories: { value: IssueCategory | ''; label: string }[] = [
  { value: '', label: 'All categories' }, { value: 'ROADS', label: 'Roads' }, { value: 'STREETLIGHTS', label: 'Streetlights' },
  { value: 'WASTE', label: 'Waste' }, { value: 'WATER', label: 'Water' }, { value: 'DRAINAGE', label: 'Drainage' }, { value: 'OTHER', label: 'Other' },
]
const statuses: { value: IssueStatus | ''; label: string }[] = [
  { value: '', label: 'All statuses' }, { value: 'REPORTED', label: 'Reported' }, { value: 'UNDER_REVIEW', label: 'Under review' },
  { value: 'IN_PROGRESS', label: 'In progress' }, { value: 'RESOLVED', label: 'Resolved' }, { value: 'REJECTED', label: 'Rejected' }, { value: 'DUPLICATE', label: 'Duplicate' },
]
const PAGE_SIZE = 20

export function ExplorerPage() {
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState<IssueCategory | ''>('')
  const [status, setStatus] = useState<IssueStatus | ''>('')
  const [page, setPage] = useState(0)
  const [view, setView] = useState<'list' | 'map'>('list')
  const params = useMemo(() => {
    const values = new URLSearchParams({ page: String(page), size: String(PAGE_SIZE) })
    if (search) values.set('query', search)
    if (category) values.set('category', category)
    if (status) values.set('status', status)
    return values.toString()
  }, [category, page, search, status])
  const issues = useQuery({ queryKey: ['issues', params], queryFn: () => apiRequest<IssuePage>(`/api/issues?${params}`) })
  const records = issues.data?.content ?? []

  return <div className="section-shell page-shell">
    <PageHeader eyebrow="PUBLIC EXPLORER" title="See what needs attention." description="Browse reports from across the community, understand the pattern, and add your support." action={<Link className="button button-primary" to="/report">Report an issue</Link>} />
    <div className="filter-panel">
      <form className="search-box" onSubmit={(event) => { event.preventDefault(); setSearch(query.trim()); setPage(0) }}><Search size={18} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search reports, places, or keywords" aria-label="Search reports" /></form>
      <div className="filter-selects"><label><span className="sr-only">Category</span><select value={category} onChange={(event) => { setCategory(event.target.value as IssueCategory | ''); setPage(0) }}>{categories.map((option) => <option value={option.value} key={option.label}>{option.label}</option>)}</select></label><label><span className="sr-only">Status</span><select value={status} onChange={(event) => { setStatus(event.target.value as IssueStatus | ''); setPage(0) }}>{statuses.map((option) => <option value={option.value} key={option.label}>{option.label}</option>)}</select></label></div>
      <div className="view-switcher"><button className={view === 'list' ? 'active' : ''} onClick={() => setView('list')} aria-label="List view"><List size={17} /> List</button><button className={view === 'map' ? 'active' : ''} onClick={() => setView('map')} aria-label="Map view"><MapIcon size={17} /> Map</button></div>
    </div>
    {issues.isLoading && <div className="page-state">Loading community reports…</div>}
    {issues.isError && <div className="error-panel"><strong>Reports are temporarily unavailable.</strong><span>Check that the backend is running, then try again.</span></div>}
    {!issues.isLoading && !issues.isError && !records.length && <div className="empty-panel large-empty"><SlidersHorizontal size={22} /><strong>No reports match these filters.</strong><span>Try a different category or be the first to report an issue in this area.</span></div>}
    {!issues.isLoading && !issues.isError && records.length > 0 && (view === 'map' ? <IssueMap issues={records} /> : <div className="issue-grid">{records.map((issue) => <IssueCard issue={issue} key={issue.id} />)}</div>)}
    {issues.data && <div className="explorer-pagination"><span className="result-count">Showing {records.length} of {issues.data.totalElements} community reports</span><div className="pagination-controls"><button className="button button-outline button-small" onClick={() => setPage((current) => current - 1)} disabled={page <= 0} aria-label="Previous page"><ChevronLeft size={15} /> Previous</button><span aria-live="polite">Page {page + 1} of {Math.max(issues.data.totalPages, 1)}</span><button className="button button-outline button-small" onClick={() => setPage((current) => current + 1)} disabled={page >= issues.data.totalPages - 1} aria-label="Next page">Next <ChevronRight size={15} /></button></div></div>}
  </div>
}
