import { CheckCircle2, Circle, MessageSquare } from 'lucide-react'
import type { IssueUpdate } from '../types'
import { formatStatus, StatusBadge } from './StatusBadge'

export function Timeline({ updates }: { updates: IssueUpdate[] }) {
  if (!updates.length) return <div className="empty-panel">No updates have been recorded yet.</div>
  return (
    <div className="timeline">
      {updates.map((update, index) => (
        <div className="timeline-item" key={update.id}>
          <div className="timeline-marker">{index === updates.length - 1 ? <CheckCircle2 size={18} /> : <Circle size={17} />}</div>
          <div className="timeline-content">
            <div className="timeline-heading">
              <strong>{update.oldStatus && update.oldStatus !== update.newStatus ? `${formatStatus(update.oldStatus)} → ` : ''}{formatStatus(update.newStatus)}</strong>
              <time>{new Date(update.createdAt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}</time>
            </div>
            <div className="timeline-actor">{update.actorName} · {update.actorRole === 'STAFF' ? 'CivicTrace staff' : 'Resident'}</div>
            {update.comment && <p><MessageSquare size={14} /> {update.comment}</p>}
            {index === updates.length - 1 && <StatusBadge status={update.newStatus} />}
          </div>
        </div>
      ))}
    </div>
  )
}
