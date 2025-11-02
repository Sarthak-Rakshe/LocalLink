import { useNavigate } from "react-router-dom";
import Button from "../ui/Button.jsx";
import { StarIcon } from "@heroicons/react/24/solid";

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

  return (
    <div className="group relative overflow-hidden rounded-xl border border-zinc-200 bg-white p-5 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="truncate text-base font-semibold text-zinc-900">
            {serviceName}
          </h3>
          <div className="mt-1 text-xs text-zinc-500">
            <span className="inline-flex items-center rounded bg-zinc-100 px-2 py-0.5 text-[11px] font-medium text-zinc-700">
              {serviceCategory}
            </span>
          </div>
        </div>
        <div className="text-right">
          <div className="text-xs text-zinc-500">From</div>
          <div className="text-lg font-semibold text-zinc-900">
            ₹{servicePricePerHour}
            <span className="ml-0.5 text-xs font-normal text-zinc-500">
              /hr
            </span>
          </div>
        </div>
      </div>
      {serviceDescription && (
        <p className="mt-2 line-clamp-3 text-sm text-zinc-700">
          {serviceDescription}
        </p>
      )}

      <div className="mt-3 flex items-center justify-between text-sm text-zinc-600">
        {rating ? (
          <div className="inline-flex items-center gap-1 text-zinc-700">
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
  );
}
