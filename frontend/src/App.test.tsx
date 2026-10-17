import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('CivicTrace application shell', () => {
  it('shows the public civic reporting call to action', () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: /make every report count/i })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /explore reports/i })).toBeInTheDocument()
  })
})
