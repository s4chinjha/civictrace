export type Role = 'RESIDENT' | 'STAFF'
export type IssueCategory = 'ROADS' | 'STREETLIGHTS' | 'WASTE' | 'WATER' | 'DRAINAGE' | 'OTHER'
export type IssueStatus = 'REPORTED' | 'UNDER_REVIEW' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED' | 'DUPLICATE'

export interface User {
  id: string
  name: string
  email: string
  role: Role
  createdAt: string
}

export interface IssueSummary {
  id: string
  title: string
  description: string
  category: IssueCategory
  status: IssueStatus
  latitude: number
  longitude: number
  locationLabel?: string | null
  reporterName: string
  createdAt: string
  updatedAt: string
  resolvedAt?: string | null
  supportCount: number
  supportedByCurrentUser: boolean
  assignedStaffId?: string | null
  assignedStaffName?: string | null
}

export interface IssueUpdate {
  id: string
  actorId: string
  actorName: string
  actorRole: Role
  oldStatus?: IssueStatus | null
  newStatus: IssueStatus
  comment?: string | null
  createdAt: string
}

export interface IssueDetail {
  issue: IssueSummary
  updates: IssueUpdate[]
}

export interface IssuePage {
  content: IssueSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Suggestion {
  id: string
  title: string
  category: IssueCategory
  status: IssueStatus
  latitude: number
  longitude: number
  locationLabel?: string | null
  distanceKm: number
}

export interface AnalyticsSummary {
  totalReports: number
  openReports: number
  resolvedReports: number
  averageResolutionHours: number
  byCategory: { category: string; count: number }[]
  agingOpenReports: { bucket: string; count: number }[]
}

export interface TrendPoint {
  date: string
  count: number
}

export interface Hotspot {
  cell: string
  latitude: number
  longitude: number
  count: number
}

export interface ApiErrorShape {
  message?: string
  fieldErrors?: Record<string, string>
}
