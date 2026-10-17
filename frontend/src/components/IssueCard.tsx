import { ArrowUpRight, MapPin, ThumbsUp } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { IssueSummary } from '../types'
import { StatusBadge } from './StatusBadge'

const categoryLabels: Record<IssueSummary['category'], string> = {
  ROADS: 'Roads', STREETLIGHTS: 'Streetlights', WASTE: 'Waste', WATER: 'Water', DRAINAGE: 'Drainage', OTHER: 'Other',
}

export function IssueCard({ issue, compact = false }: { issue: IssueSummary; compact?: boolean }) {
  return (
    <article className={`issue-card ${compact ? 'issue-card-compact' : ''}`}>
      <div className="issue-card-top">
        <span className="category-label">{categoryLabels[issue.category]}</span>
        <StatusBadge status={issue.status} />
      </div>
      <Link to={`/issues/${issue.id}`} className="issue-card-title">{issue.title}</Link>
      {!compact && <p className="issue-card-description">{issue.description}</p>}
      <div className="issue-card-meta">
        <span><MapPin size={14} /> {issue.locationLabel || `${Number(issue.latitude).toFixed(3)}, ${Number(issue.longitude).toFixed(3)}`}</span>
        <span><ThumbsUp size={14} /> {issue.supportCount} support{issue.supportCount === 1 ? '' : 's'}</span>
      </div>
      <div className="issue-card-footer">
        <span>Reported by {issue.reporterName}</span>
        <Link to={`/issues/${issue.id}`} aria-label={`View ${issue.title}`}><ArrowUpRight size={17} /></Link>
      </div>
    </article>
  )
}
