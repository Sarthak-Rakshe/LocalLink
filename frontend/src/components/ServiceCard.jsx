import { Card, CardBody } from "./ui/Card.jsx";
import Rating from "./ui/Rating.jsx";
import { Link } from "react-router-dom";

function formatPricePerHour(price) {
  if (price == null) return "-";
  return (
    new Intl.NumberFormat(undefined, {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0,
    }).format(price) + "/hr"
  );
}

function haversineKm(lat1, lon1, lat2, lon2) {
  const toRad = (deg) => (deg * Math.PI) / 180;
  const R = 6371; // km
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) *
      Math.cos(toRad(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

// Simple, deterministic placeholder image based on category/name.
// Exported for reuse in pages like ServiceDetails.
export function placeholderImageFor(svc) {
  const emojis = ["🧹", "🔧", "🌿", "🐶", "👨‍🏫", "🚗", "🍽️", "🏠", "🧰", "🧴"];
  const colors = [
    "#eef2ff",
    "#ecfeff",
    "#dcfce7",
    "#fff7ed",
    "#ffe4e6",
    "#fef9c3",
  ];
  const key = (svc?.serviceCategory || svc?.serviceName || "").length || 0;
  const e = emojis[key % emojis.length];
  const bg = colors[key % colors.length];
  const svg = encodeURIComponent(
    `<svg xmlns='http://www.w3.org/2000/svg' width='600' height='360'>
      <rect width='100%' height='100%' fill='${bg}'/>
      <text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='96'>${e}</text>
    </svg>`
  );
  return `data:image/svg+xml,${svg}`;
}

export default function ServiceCard({
  service,
  userLocation,
  variant = "default",
  linkState,
}) {
  const distanceKm = userLocation
    ? haversineKm(
        userLocation.latitude,
        userLocation.longitude,
        service.latitude,
        service.longitude
      )
    : null;

  const imageSrc = placeholderImageFor(service);
  const isCompact = variant === "compact";
  const imageHeightClass = isCompact ? "h-28" : "h-36";

  return (
    <Link
      to={`/serviceDetails/${service.serviceId}`}
      state={linkState}
      className="block h-full focus:outline-none focus:ring-2 focus:ring-indigo-500 rounded-lg"
    >
      <Card className="overflow-hidden h-full flex flex-col hover:shadow-md transition-shadow">
        {/* Image area with built-in placeholder */}
        <div
          className={`${imageHeightClass} w-full bg-gray-100 overflow-hidden`}
          aria-hidden
        >
          <img
            src={imageSrc}
            alt={service.serviceName}
            className="h-full w-full object-cover select-none pointer-events-none"
          />
        </div>
        <CardBody className="flex-1 flex flex-col">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h3
                className={
                  isCompact
                    ? "text-base font-semibold"
                    : "text-lg font-semibold"
                }
              >
                {service.serviceName}
              </h3>
              <p
                className={
                  isCompact
                    ? "mt-1 line-clamp-1 text-xs text-gray-600"
                    : "mt-1 line-clamp-2 text-sm text-gray-600"
                }
              >
                {service.serviceDescription}
              </p>
            </div>
            {!isCompact && (
              <span className="shrink-0 rounded-full bg-gray-100 px-2 py-1 text-xs text-gray-700">
                {service.serviceCategory}
              </span>
            )}
          </div>

          <div className="flex-1" />

          <div className="mt-3 flex items-center justify-between text-sm">
            <Rating
              value={service._averageRating ?? 0}
              count={service._ratingCount}
              size={isCompact ? "xs" : "sm"}
            />
            <span
              className={
                isCompact
                  ? "text-gray-900 font-medium text-sm"
                  : "text-gray-900 font-medium"
              }
            >
              {formatPricePerHour(service.servicePricePerHour)}
            </span>
          </div>

          {distanceKm != null && (
            <div className="mt-1 text-xs text-gray-500">
              {distanceKm.toFixed(1)} km away
            </div>
          )}
        </CardBody>
      </Card>
    </Link>
  );
}
