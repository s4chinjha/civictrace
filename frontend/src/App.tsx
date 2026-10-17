import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { Layout } from './components/Layout'
import { RealtimeSync } from './components/RealtimeSync'
import { AnalyticsPage } from './pages/AnalyticsPage'
import { ExplorerPage } from './pages/ExplorerPage'
import { IssueDetailPage } from './pages/IssueDetailPage'
import { LandingPage } from './pages/LandingPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ReportPage } from './pages/ReportPage'
import { ResidentDashboard } from './pages/ResidentDashboard'
import { StaffDashboard } from './pages/StaffDashboard'
import type { Role } from './types'

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 15_000, retry: 1 } },
})

function ProtectedRoute({ role }: { role?: Role }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="page-state">Checking your session…</div>
  if (!user) return <Navigate to="/login" replace />
  if (role && user.role !== role) return <Navigate to={user.role === 'STAFF' ? '/staff' : '/dashboard'} replace />
  return <Outlet />
}

function AppRoutes() {
  return (
    <>
      <RealtimeSync />
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<LandingPage />} />
          <Route path="/issues" element={<ExplorerPage />} />
          <Route path="/issues/:issueId" element={<IssueDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route element={<ProtectedRoute role="RESIDENT" />}>
            <Route path="/report" element={<ReportPage />} />
            <Route path="/dashboard" element={<ResidentDashboard />} />
          </Route>
          <Route element={<ProtectedRoute role="STAFF" />}>
            <Route path="/staff" element={<StaffDashboard />} />
          </Route>
          <Route path="/analytics" element={<AnalyticsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <AppRoutes />
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  )
}
