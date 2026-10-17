import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { IssueCard } from './IssueCard'

describe('IssueCard', () => {
  it('shows the report category, status, location, and support count', () => {
    render(
      <MemoryRouter>
        <IssueCard issue={{
          id: 'issue-1',
          title: 'Deep pothole near the gate',
          description: 'Unsafe after rain.',
          category: 'ROADS',
          status: 'IN_PROGRESS',
          latitude: 28.6139,
          longitude: 77.209,
          locationLabel: 'Community gate',
          reporterName: 'Resident',
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-02T00:00:00Z',
          supportCount: 7,
          supportedByCurrentUser: false,
        }} />
      </MemoryRouter>,
    )

    expect(screen.getByRole('link', { name: 'Deep pothole near the gate' })).toBeInTheDocument()
    expect(screen.getByText('In progress')).toBeInTheDocument()
    expect(screen.getByText(/Community gate/)).toBeInTheDocument()
    expect(screen.getByText(/7 supports/)).toBeInTheDocument()
  })
})
