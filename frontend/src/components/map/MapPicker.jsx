import React, { useEffect, useMemo, useState } from "react";
import "leaflet/dist/leaflet.css";
import { MapContainer, TileLayer, Marker, useMapEvents } from "react-leaflet";
import L from "leaflet";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

// Fix default icon paths for Leaflet in bundlers
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
});

const defaultIcon = new L.Icon({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41],
});

function ClickHandler({ onSelect }) {
  useMapEvents({
    click(e) {
      const { lat, lng } = e.latlng;
      onSelect({ lat: Number(lat.toFixed(6)), lng: Number(lng.toFixed(6)) });
    },
  });
  return null;
}

const MapPicker = ({ value, onChange, height = 320, autoLocate = false }) => {
  const [position, setPosition] = useState(() => {
    if (
      value &&
      typeof value.lat === "number" &&
      typeof value.lng === "number"
    ) {
      return { lat: value.lat, lng: value.lng };
    }
    // Default center (India) if no value is provided
    return { lat: 20.5937, lng: 78.9629 };
  });

  const center = useMemo(() => {
    if (
      value &&
      typeof value.lat === "number" &&
      typeof value.lng === "number"
    ) {
      return [value.lat, value.lng];
    }
    return [position.lat, position.lng];
  }, [value, position]);

  useEffect(() => {
    if (
      value &&
      typeof value.lat === "number" &&
      typeof value.lng === "number"
    ) {
      setPosition({ lat: value.lat, lng: value.lng });
    }
  }, [value?.lat, value?.lng]);

  // If enabled, try to auto-center on user's current location once on mount
  useEffect(() => {
    const shouldAutoLocate =
      autoLocate &&
      (!value ||
        typeof value.lat !== "number" ||
        typeof value.lng !== "number");
    if (!shouldAutoLocate) return;

    if (!("geolocation" in navigator)) return;

    const geoSuccess = (pos) => {
      const lat = Number(pos.coords.latitude.toFixed(6));
      const lng = Number(pos.coords.longitude.toFixed(6));
      const next = { lat, lng };
      setPosition(next);
      onChange?.(next);
    };
    const geoError = () => {
      // Silently ignore: we'll keep default center
    };

    navigator.geolocation.getCurrentPosition(geoSuccess, geoError, {
      enableHighAccuracy: true,
      timeout: 8000,
      maximumAge: 0,
    });
    // run only once on mount
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSelect = (pos) => {
    setPosition(pos);
    onChange?.(pos);
  };

  return (
    <div
      style={{ height }}
      className="w-full overflow-hidden rounded-md border"
    >
      <MapContainer
        center={center}
        zoom={13}
        style={{ height: "100%", width: "100%" }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ClickHandler onSelect={handleSelect} />
        {position && (
          <Marker
            position={[position.lat, position.lng]}
            icon={defaultIcon}
            draggable={true}
            eventHandlers={{
              dragend: (e) => {
                const m = e.target;
                const { lat, lng } = m.getLatLng();
                handleSelect({
                  lat: Number(lat.toFixed(6)),
                  lng: Number(lng.toFixed(6)),
                });
              },
            }}
          />
        )}
      </MapContainer>
    </div>
  );
};

export default MapPicker;
