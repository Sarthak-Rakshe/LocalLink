import { useNavigate } from "react-router-dom";
import Button from "../ui/Button.jsx";
import { StarIcon } from "@heroicons/react/24/solid";
import Skeleton from "../ui/Skeleton.jsx";

export default function ServiceCard({ service }) {
  const navigate = useNavigate();
  if (!service) return null;
  const {
    serviceName,
    serviceDescription,
    serviceCategory,
    servicePricePerHour,
    reviewAggregate,
    serviceId,
  } = service;

  const rating = reviewAggregate?.averageRating ?? null;
  const reviewsCount = reviewAggregate?.totalReviews ?? 0;

  // Deterministic identicon pattern for the service
  const seed = String(serviceId || serviceName || "service");
  function hashCode(str) {
    let h = 0;
    for (let i = 0; i < str.length; i++) h = (h << 5) - h + str.charCodeAt(i);
    return Math.abs(h);
  }
  const hue = hashCode(seed) % 360;
  // Lighter palette for softer look
  const fg = `hsl(${hue} 70% 50%)`;
  const fg2 = `hsl(${(hue + 30) % 360} 70% 55%)`;
  const bg = `hsl(${hue} 40% 96%)`;

  const nameFallback = serviceName || "Untitled service";
  const descriptionFallback =
    serviceDescription || "No description provided yet.";
  const priceText =
    servicePricePerHour != null && servicePricePerHour !== ""
      ? `₹${servicePricePerHour}`
      : "—";

  return (
    <div className="group relative overflow-hidden rounded-xl border border-zinc-200 bg-white shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md dark:border-zinc-800 dark:bg-zinc-900">
      {/* Identicon banner (8x8 symmetric, denser + lighter) */}
      <div
        className="relative aspect-video w-full overflow-hidden"
        style={{ background: bg }}
      >
        <svg viewBox="0 0 8 8" className="h-full w-full">
          {Array.from({ length: 8 }).map((_, y) =>
            Array.from({ length: 4 }).map((__, x) => {
              const h = hashCode(`${seed}:${x},${y}`);
              const bit = h % 3 !== 0; // ~66% density
              if (!bit) return null;
              const color = h % 2 === 0 ? fg : fg2;
              return (
                <g key={`${x}-${y}`}>
                  <rect
                    x={x}
                    y={y}
                    width="1"
                    height="1"
                    fill={color}
                    fillOpacity="0.6"
                  />
                  <rect
                    x={7 - x}
                    y={y}
                    width="1"
                    height="1"
                    fill={color}
                    fillOpacity="0.6"
                  />
                </g>
              );
            })
          )}
        </svg>
        {serviceCategory && (
          <span className="absolute left-2 top-2 rounded bg-black/60 px-2 py-0.5 text-[11px] font-medium text-white backdrop-blur">
            {serviceCategory}
          </span>
        )}
      </div>
      <div className="p-5">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h3 className="truncate text-base font-semibold text-zinc-900 dark:text-zinc-100">
              {nameFallback}
            </h3>
            <div className="mt-1 text-xs text-zinc-500 line-clamp-1 dark:text-zinc-400">
              {descriptionFallback}
            </div>
          </div>
          <div className="text-right">
            <div className="text-xs text-zinc-500 dark:text-zinc-400">From</div>
            <div className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
              {priceText}
              <span className="ml-0.5 text-xs font-normal text-zinc-500 dark:text-zinc-400">
                /hr
              </span>
            </div>
          </div>
        </div>

        <div className="mt-3 flex items-center justify-between text-sm text-zinc-600 dark:text-zinc-400">
          {rating ? (
            <div className="inline-flex items-center gap-1 text-zinc-700 dark:text-zinc-300">
              <StarIcon className="size-4 text-amber-400" aria-hidden />
              <span className="font-medium">{rating.toFixed(1)}</span>
              <span className="text-zinc-400">({reviewsCount})</span>
            </div>
          ) : (
            <div className="text-zinc-400">No ratings yet</div>
          )}

          {/* View details navigates to dedicated page */}
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate(`/services/${serviceId ?? service?.id}`)}
            className="group-hover:border-zinc-400"
          >
            View details
          </Button>
        </div>
      </div>
    </div>
  );
}

// A skeleton placeholder that mirrors the ServiceCard layout
export function ServiceCardSkeleton() {
  return (
    <div className="relative overflow-hidden rounded-xl border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <div className="aspect-video w-full bg-zinc-100 dark:bg-white/5">
        <Skeleton className="h-full w-full rounded-none" />
      </div>
      <div className="p-5">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <Skeleton className="h-4 w-3/4" />
            <div className="mt-2">
              <Skeleton className="h-3 w-full" />
            </div>
          </div>
          <div className="text-right">
            <Skeleton className="h-3 w-10 ml-auto" />
            <div className="mt-1">
              <Skeleton className="h-5 w-16 ml-auto" />
            </div>
          </div>
        </div>
        <div className="mt-3 flex items-center justify-between">
          <Skeleton className="h-4 w-24" />
          <Skeleton className="h-8 w-24" />
        </div>
      </div>
    </div>
  );
}
