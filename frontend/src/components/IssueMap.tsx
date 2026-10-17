import L from 'leaflet'
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet'
import { Link } from 'react-router-dom'
import type { IssueSummary } from '../types'
import { StatusBadge } from './StatusBadge'
import 'leaflet/dist/leaflet.css'

L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
})

export function IssueMap({ issues }: { issues: IssueSummary[] }) {
  const center: [number, number] = issues.length ? [Number(issues[0].latitude), Number(issues[0].longitude)] : [28.6139, 77.209]
  if (!issues.length) return <div className="empty-panel map-empty">No mapped reports match these filters.</div>
  return (
    <div className="map-wrap">
      <MapContainer center={center} zoom={13} scrollWheelZoom className="issue-map">
        <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {issues.map((issue) => (
          <Marker key={issue.id} position={[Number(issue.latitude), Number(issue.longitude)]}>
            <Popup>
              <div className="map-popup">
                <strong>{issue.title}</strong>
                <StatusBadge status={issue.status} />
                <Link to={`/issues/${issue.id}`}>Open report →</Link>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
      <div className="map-attribution-note">Map data © OpenStreetMap contributors</div>
    </div>
  )
}
