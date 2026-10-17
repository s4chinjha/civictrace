import type { IssueStatus } from '../types'

const labels: Record<IssueStatus, string> = {
  REPORTED: 'Reported',
  UNDER_REVIEW: 'Under review',
  IN_PROGRESS: 'In progress',
  RESOLVED: 'Resolved',
  REJECTED: 'Rejected',
  DUPLICATE: 'Duplicate',
}

export function StatusBadge({ status }: { status: IssueStatus }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{labels[status]}</span>
}

export function formatStatus(status: IssueStatus) {
  return labels[status]
}
