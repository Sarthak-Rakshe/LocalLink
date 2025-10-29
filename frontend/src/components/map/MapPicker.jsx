import { useEffect, useMemo, useState } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  useMap,
  useMapEvents,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// Fix default marker icons (Vite bundling)
import marker2x from "leaflet/dist/images/marker-icon-2x.png";
import marker1x from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

// Ensure default icon assets resolve under Vite bundling; also create an explicit icon
L.Icon.Default.mergeOptions({
  iconRetinaUrl: marker2x,
  iconUrl: marker1x,
  shadowUrl: markerShadow,
});
const defaultIcon = L.icon({
  iconRetinaUrl: marker2x,
  iconUrl: marker1x,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41],
});

function ClickHandler({ onClick }) {
  useMapEvents({
    click(e) {
      if (!e?.latlng) return;
      onClick?.({ lat: e.latlng.lat, lng: e.latlng.lng });
    },
  });
  return null;
}

function InvalidateSizeOnLoad({ deps = [] }) {
  const map = useMap();
  useEffect(() => {
    // Defer to next tick so the container has its final size
    const id = setTimeout(() => {
      try {
        map.invalidateSize();
      } catch {}
    }, 0);
    return () => clearTimeout(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
  return null;
}

/**
 * MapPicker component
 * Props:
 * - value?: { lat: number, lng: number }
 * - onChange?: ({ lat, lng }) => void
 * - height?: string | number (default 320)
 */
export default function MapPicker({ value, onChange, height = 320 }) {
  const fallbackCenter = useMemo(() => ({ lat: 20.5937, lng: 78.9629 }), []); // India approx center
  const [center, setCenter] = useState(value ?? fallbackCenter);
  const [position, setPosition] = useState(value ?? null);

  // Keep internal state in sync when parent value changes
  useEffect(() => {
    if (value && Number.isFinite(value.lat) && Number.isFinite(value.lng)) {
      setCenter(value);
      setPosition(value);
    }
  }, [value]);

  const handlePick = ({ lat, lng }) => {
    const next = { lat, lng };
    setPosition(next);
    setCenter(next);
    onChange?.(next);
  };

  const style = {
    height: typeof height === "number" ? `${height}px` : height,
    width: "100%",
    borderRadius: "0.5rem",
    overflow: "hidden",
  };

  return (
    <div className="mt-2">
      <div style={style} className="ring-1 ring-zinc-200">
        <MapContainer
          center={[center.lat, center.lng]}
          zoom={position ? 14 : 5}
          scrollWheelZoom={true}
          style={{ height: "100%", width: "100%" }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <InvalidateSizeOnLoad deps={[center.lat, center.lng]} />
          <ClickHandler onClick={handlePick} />
          {position && (
            <Marker
              position={[position.lat, position.lng]}
              icon={defaultIcon}
              draggable
              eventHandlers={{
                dragend: (e) => {
                  const m = e.target;
                  const p = m.getLatLng();
                  handlePick({ lat: p.lat, lng: p.lng });
                },
              }}
            />
          )}
        </MapContainer>
      </div>
      <p className="mt-2 text-xs text-zinc-600">
        Click the map to set a marker, or drag the marker to fine-tune location.
      </p>
    </div>
  );
}
