import { useEffect, useRef } from "react";
import L from "leaflet";
import Button from "./Button.jsx";

// Simple Leaflet-based map picker for lat/lng
// Props: value={{ latitude, longitude }}, onChange({ latitude, longitude }), height
export default function MapPicker({ value, onChange, height = 320 }) {
  const mapRef = useRef(null);
  const markerRef = useRef(null);

  useEffect(() => {
    if (mapRef.current) return; // already init
    const container = document.getElementById("map-picker");
    if (!container) return;

    const map = L.map(container).setView(
      [value?.latitude || 18.5204, value?.longitude || 73.8567],
      12
    );
    mapRef.current = map;

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>',
    }).addTo(map);

    const onMapClick = (e) => {
      const { lat, lng } = e.latlng;
      if (markerRef.current) {
        markerRef.current.setLatLng([lat, lng]);
      } else {
        markerRef.current = L.circleMarker([lat, lng], {
          radius: 8,
          color: "#2563eb",
          weight: 2,
          fillColor: "#60a5fa",
          fillOpacity: 0.7,
        }).addTo(map);
      }
      onChange?.({ latitude: lat, longitude: lng });
    };

    map.on("click", onMapClick);

    return () => {
      map.off("click", onMapClick);
      map.remove();
      mapRef.current = null;
      markerRef.current = null;
    };
  }, [onChange, value?.latitude, value?.longitude]);

  // Update marker when value changes externally
  useEffect(() => {
    const map = mapRef.current;
    if (!map || value?.latitude == null || value?.longitude == null) return;
    if (markerRef.current) {
      markerRef.current.setLatLng([value.latitude, value.longitude]);
    } else {
      markerRef.current = L.circleMarker([value.latitude, value.longitude], {
        radius: 8,
        color: "#2563eb",
        weight: 2,
        fillColor: "#60a5fa",
        fillOpacity: 0.7,
      }).addTo(map);
    }
    map.setView([value.latitude, value.longitude], 14);
  }, [value?.latitude, value?.longitude]);

  const useCurrentLocation = () => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition((pos) => {
      const { latitude, longitude } = pos.coords;
      onChange?.({ latitude, longitude });
    });
  };

  return (
    <div>
      <div
        id="map-picker"
        style={{ height: typeof height === "number" ? `${height}px` : height }}
        className="w-full rounded-md border border-gray-200 overflow-hidden"
      />
      <div className="mt-2 flex justify-end">
        <Button variant="ghost" type="button" onClick={useCurrentLocation}>
          Use current location
        </Button>
      </div>
    </div>
  );
}
