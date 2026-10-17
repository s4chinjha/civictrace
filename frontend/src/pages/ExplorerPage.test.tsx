import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ExplorerPage } from './ExplorerPage'
import type { IssuePage } from '../types'

const request = vi.fn<(path: string) => Promise<IssuePage>>()

vi.mock('../api/client', () => ({
  apiRequest: (path: string) => request(path),
}))

function page(pageNumber = 0, totalPages = 2): IssuePage {
  return { content: [], page: pageNumber, size: 20, totalElements: 21, totalPages }
}

function renderExplorer() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(<QueryClientProvider client={queryClient}><MemoryRouter><ExplorerPage /></MemoryRouter></QueryClientProvider>)
}

describe('Explorer pagination and filters', () => {
  beforeEach(() => {
    request.mockReset()
    request.mockResolvedValue(page())
  })

  it('requests the first page and exposes every issue status', async () => {
    renderExplorer()

    await waitFor(() => expect(request).toHaveBeenCalledWith('/api/issues?page=0&size=20'))
    expect(screen.getByRole('option', { name: 'Rejected' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Duplicate' })).toBeInTheDocument()
  })

  it('moves between pages and disables navigation at the boundaries', async () => {
    const user = userEvent.setup()
    renderExplorer()

    const next = await screen.findByRole('button', { name: /next page/i })
    expect(screen.getByRole('button', { name: /previous page/i })).toBeDisabled()
    await user.click(next)

    await waitFor(() => expect(request).toHaveBeenLastCalledWith('/api/issues?page=1&size=20'))
    expect(screen.getByRole('button', { name: /previous page/i })).not.toBeDisabled()
    expect(screen.getByRole('button', { name: /next page/i })).toBeDisabled()
  })

  it('resets to the first page when a filter changes', async () => {
    const user = userEvent.setup()
    renderExplorer()

    await user.click(await screen.findByRole('button', { name: /next page/i }))
    await waitFor(() => expect(request).toHaveBeenCalledWith('/api/issues?page=1&size=20'))
    await user.selectOptions(screen.getByRole('combobox', { name: 'Status' }), 'REJECTED')

    await waitFor(() => expect(request).toHaveBeenCalledWith('/api/issues?page=0&size=20&status=REJECTED'))
  })
})
