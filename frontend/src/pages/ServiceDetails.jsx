import { useEffect, useMemo, useState } from "react";
import { useParams, Link, useLocation } from "react-router-dom";
import servicesApi from "../api/services.js";
import { Card, CardBody } from "../components/ui/Card.jsx";
import Rating from "../components/ui/Rating.jsx";
import Button from "../components/ui/Button.jsx";
import { placeholderImageFor } from "../components/ServiceCard.jsx";

export default function ServiceDetails() {
  const { serviceId } = useParams();
  const location = useLocation();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [service, setService] = useState(null);
  const [averageRating, setAverageRating] = useState(0);

  useEffect(() => {
    let canceled = false;
    async function load() {
      try {
        setLoading(true);
        setError("");
        const svc = await servicesApi.getById(serviceId);
        if (!canceled) setService(svc);
        // Derive average rating directly from service details DTO
        const avg = svc?.reviewAggregate?.averageRating ?? 0;
        if (!canceled) setAverageRating(Number(avg) || 0);
      } catch (e) {
        if (!canceled)
          setError(
            e?.response?.data?.message || "Failed to load service details"
          );
      } finally {
        if (!canceled) setLoading(false);
      }
    }
    load();
    return () => {
      canceled = true;
    };
  }, [serviceId]);

  const imageSrc = useMemo(() => placeholderImageFor(service || {}), [service]);

  if (loading) return <div className="text-gray-600">Loading service…</div>;
  if (error)
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-red-700">
        {error}
      </div>
    );
  if (!service) return null;

  const from = location.state?.from;
  const backPath =
    from === "home" ? "/home" : from === "services" ? "/services" : "/services";
  const backLabel =
    from === "home"
      ? "Back to home"
      : from === "services"
      ? "Back to services"
      : "Back";

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="m-0">{service.serviceName}</h1>
        <Button as={Link} to={backPath} state={{ from }} variant="ghost">
          {backLabel}
        </Button>
      </div>

      <Card>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-0 md:gap-6">
          <div className="md:col-span-1">
            <img
              src={imageSrc}
              alt={service.serviceName}
              className="w-full h-56 md:h-full object-cover"
            />
          </div>
          <CardBody className="md:col-span-2">
            <div className="flex items-start justify-between gap-4">
              <span className="rounded-full bg-gray-100 px-3 py-1 text-xs text-gray-700">
                {service.serviceCategory}
              </span>
              <div className="text-right">
                <div className="text-sm text-gray-500">Price</div>
                <div className="text-lg font-semibold">
                  {new Intl.NumberFormat(undefined, {
                    style: "currency",
                    currency: "INR",
                    maximumFractionDigits: 0,
                  }).format(service.servicePricePerHour)}
                  <span className="text-sm text-gray-600">/hr</span>
                </div>
              </div>
            </div>

            <div className="mt-3">
              <Rating
                value={service._averageRating ?? averageRating ?? 0}
                count={service?.reviewAggregate?.totalReviews}
              />
            </div>

            <p className="mt-4 text-gray-700 whitespace-pre-line">
              {service.serviceDescription}
            </p>

            {/* Future: availability, booking, provider info, reviews, etc. */}
          </CardBody>
        </div>
      </Card>
    </div>
  );
}
