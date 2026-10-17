import { useState } from 'react'
import { AlertTriangle, ArrowRight, CheckCircle2, MapPin } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import { apiRequest, ApiError } from '../api/client'
import { PageHeader } from '../components/Layout'
import type { IssueCategory, IssueDetail, Suggestion } from '../types'

const categories: { value: IssueCategory; label: string; hint: string }[] = [
  { value: 'ROADS', label: 'Roads & potholes', hint: 'Potholes, damaged surfaces, unsafe crossings' },
  { value: 'STREETLIGHTS', label: 'Streetlights', hint: 'Broken, flickering, or missing lights' },
  { value: 'WASTE', label: 'Waste & cleanliness', hint: 'Overflowing bins or missed collection' },
  { value: 'WATER', label: 'Water supply', hint: 'Leaks, flooding, or supply concerns' },
  { value: 'DRAINAGE', label: 'Drainage', hint: 'Blocked drains or waterlogging' },
  { value: 'OTHER', label: 'Other civic issue', hint: 'Something else your neighbourhood needs' },
]

export function ReportPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', description: '', category: 'ROADS' as IssueCategory, latitude: '28.613900', longitude: '77.209000', locationLabel: '' })
  const [suggestions, setSuggestions] = useState<Suggestion[]>([])
  const [pending, setPending] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  function payload() { return { ...form, latitude: Number(form.latitude), longitude: Number(form.longitude) } }
  async function submit(event: React.FormEvent) {
    event.preventDefault(); setError(''); setBusy(true)
    try {
      const nearby = await apiRequest<Suggestion[]>('/api/issues/suggestions', { method: 'POST', body: JSON.stringify({ category: form.category, latitude: Number(form.latitude), longitude: Number(form.longitude) }) })
      if (nearby.length) { setSuggestions(nearby); setPending(true); setBusy(false); return }
      await createReport()
    } catch (err) { setError(err instanceof ApiError ? err.message : err instanceof Error ? err.message : 'Unable to submit this report.') } finally { setBusy(false) }
  }
  async function createReport() {
    const created = await apiRequest<IssueDetail>('/api/issues', { method: 'POST', body: JSON.stringify(payload()) })
    navigate(`/issues/${created.issue.id}`)
  }
  return <div className="section-shell page-shell report-shell"><PageHeader eyebrow="ADD YOUR VOICE" title="Report a civic issue." description="Give your neighbourhood team the context they need to act with confidence." />
    <div className="report-layout"><form className="report-form" onSubmit={submit}><div className="form-section"><div className="form-section-heading"><span>01</span><div><h2>What needs attention?</h2><p>Start with a clear, specific title.</p></div></div><label>Issue title<input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} placeholder="e.g. Deep pothole near the community gate" required maxLength={160} /></label><label>Describe the issue<textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} placeholder="What is happening, and how is it affecting people?" required rows={5} maxLength={5000} /></label><label>Category<select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value as IssueCategory })}>{categories.map((category) => <option value={category.value} key={category.value}>{category.label}</option>)}</select><small>{categories.find((category) => category.value === form.category)?.hint}</small></label></div><div className="form-section"><div className="form-section-heading"><span>02</span><div><h2>Where is it?</h2><p>A precise location helps teams respond faster.</p></div></div><label>Location label<input value={form.locationLabel} onChange={(event) => setForm({ ...form, locationLabel: event.target.value })} placeholder="e.g. Sector 12 community gate" maxLength={240} /></label><div className="coordinate-grid"><label>Latitude<input type="number" step="any" min="-90" max="90" value={form.latitude} onChange={(event) => setForm({ ...form, latitude: event.target.value })} required /></label><label>Longitude<input type="number" step="any" min="-180" max="180" value={form.longitude} onChange={(event) => setForm({ ...form, longitude: event.target.value })} required /></label></div><div className="location-tip"><MapPin size={17} /><span>For this MVP, use a map pin’s latitude and longitude. A live location picker can be added later.</span></div></div>{error && <div className="form-error">{error}</div>}<button className="button button-primary" disabled={busy}>{busy ? 'Checking nearby reports…' : 'Check & submit report'} <ArrowRight size={17} /></button></form><aside className="report-aside"><div className="aside-card"><CheckCircle2 size={21} /><h3>Good reports lead to good action.</h3><p>Include landmarks, timing, and impact where possible. Your report will be visible to the community and civic staff.</p></div><div className="aside-card muted"><strong>Community-first promise</strong><p>Potential matches are suggestions only. You always decide whether to support an existing report or submit a new one.</p></div></aside></div>
    {pending && <div className="modal-backdrop"><div className="suggestion-modal"><div className="modal-icon"><AlertTriangle size={22} /></div><p className="eyebrow">POSSIBLE MATCHES NEARBY</p><h2>Someone may have already reported this.</h2><p>Review these open reports before adding another voice. You can still continue if this is a different problem.</p><div className="suggestion-list">{suggestions.map((suggestion) => <Link className="suggestion-item" to={`/issues/${suggestion.id}`} key={suggestion.id}><span><strong>{suggestion.title}</strong><small>{suggestion.locationLabel || 'Nearby'} · {suggestion.distanceKm.toFixed(1)} km away</small></span><ArrowRight size={17} /></Link>)}</div><div className="modal-actions"><button className="button button-outline" onClick={() => { setPending(false); setSuggestions([]) }}>Go back</button><button className="button button-primary" onClick={() => { setPending(false); setBusy(true); void createReport().catch((err) => setError(err instanceof Error ? err.message : 'Unable to submit this report.')).finally(() => setBusy(false)) }}>Continue with new report</button></div>{error && <div className="form-error">{error}</div>}</div></div>}
  </div>
}
