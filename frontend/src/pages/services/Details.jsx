import { useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Availability, Bookings, Services, Users } from "../../services/api.js";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import toast from "react-hot-toast";
import { useAuth } from "../../context/AuthContext.jsx";
import {
  useAvailableDays,
  useAvailableSlots,
} from "../../hooks/useAvailability.js";

function toISODate(d) {
  const dt = new Date(d);
  return dt.toISOString().slice(0, 10);
}

export default function ServiceDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const serviceId = Number(id);

  const [date, setDate] = useState(toISODate(new Date()));
  const [selectedSlot, setSelectedSlot] = useState(null);

  // Fetch service core details
  const serviceQ = useQuery({
    queryKey: ["service", serviceId],
    queryFn: async () => Services.getById(serviceId),
    enabled: Number.isFinite(serviceId) && serviceId > 0,
  });

  // Fetch provider details (after service)
  const providerQ = useQuery({
    queryKey: ["provider", serviceQ.data?.serviceProviderId],
    queryFn: async () =>
      Users.getProviderById(Number(serviceQ.data?.serviceProviderId)),
    enabled: !!serviceQ.data?.serviceProviderId,
  });

  // Fetch available days for this service (optional, to guide date picking)
  const availableDaysQ = useAvailableDays(serviceId);

  // Fetch available slots for selected date
  const slotsQ = useAvailableSlots(
    Number(providerQ.data?.providerId),
    serviceId,
    date
  );

  // Note: booking creation is handled by redirecting to /bookings/create with prefilled params.
  // If you want to create directly from this page later, wire a mutation using selectedSlot.

  useEffect(() => {
    setSelectedSlot(null);
  }, [date]);

  const service = serviceQ.data;
  const provider = providerQ.data;
  const slots = slotsQ.data ?? [];

  const dayName = (d) =>
    ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"][d] || String(d);

  const availableDayNames = useMemo(() => {
    const arr = availableDaysQ.data || [];
    if (!Array.isArray(arr)) return [];
    return arr.map((s) => (typeof s === "string" ? s : dayName(Number(s))));
  }, [availableDaysQ.data]);

  // simple guard: only CUSTOMERS can book
  const canBook = user?.userType === "CUSTOMER";

  return (
    <div className="space-y-4">
      {/* Header / Summary */}
      <div className="rounded-xl border bg-white p-5 shadow-sm">
        {serviceQ.isLoading ? (
          <div className="animate-pulse text-sm text-zinc-500">Loading…</div>
        ) : service ? (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <div className="md:col-span-2">
              <h1 className="text-2xl font-semibold text-zinc-900">
                {service.serviceName}
              </h1>
              <div className="mt-1 text-sm text-zinc-600">
                <span className="inline-flex items-center rounded bg-zinc-100 px-2 py-0.5 text-[12px] font-medium text-zinc-700">
                  {service.serviceCategory}
                </span>
              </div>
              {service.serviceDescription && (
                <p className="mt-3 text-zinc-700">
                  {service.serviceDescription}
                </p>
              )}
            </div>
            <div className="md:text-right">
              <div className="text-sm text-zinc-500">Price</div>
              <div className="text-2xl font-semibold text-zinc-900">
                ₹{service.servicePricePerHour}
                <span className="text-sm font-normal text-zinc-500">/hr</span>
              </div>
              {service.reviewAggregate ? (
                <div className="mt-2 inline-flex items-center gap-1 text-sm text-zinc-700">
                  <span aria-hidden>★</span>
                  <span className="font-medium">
                    {Number(service.reviewAggregate.averageRating || 0).toFixed(
                      1
                    )}
                  </span>
                  <span className="text-zinc-500">
                    ({service.reviewAggregate.totalReviews} reviews)
                  </span>
                </div>
              ) : (
                <div className="mt-2 text-sm text-zinc-500">No ratings yet</div>
              )}
            </div>
          </div>
        ) : (
          <div className="text-sm text-rose-600">
            {serviceQ.error?.message || "Service not found"}
          </div>
        )}
      </div>

      {/* Provider info */}
      <Card title="Provider">
        {providerQ.isLoading ? (
          <div className="animate-pulse text-sm text-zinc-500">Loading…</div>
        ) : provider ? (
          <div className="grid grid-cols-1 gap-2 md:grid-cols-3">
            <div>
              <div className="text-sm text-zinc-500">Name</div>
              <div className="font-medium">{provider.providerName}</div>
            </div>
            <div>
              <div className="text-sm text-zinc-500">Email</div>
              <div className="font-medium">{provider.providerEmail}</div>
            </div>
            <div>
              <div className="text-sm text-zinc-500">Contact</div>
              <div className="font-medium">{provider.providerContact}</div>
            </div>
            {provider.providerAddress && (
              <div className="md:col-span-3">
                <div className="text-sm text-zinc-500">Address</div>
                <div className="font-medium">{provider.providerAddress}</div>
              </div>
            )}
            {provider.providerReviewAggregateResponse && (
              <div className="md:col-span-3 text-sm text-zinc-600">
                <span className="inline-flex items-center gap-1">
                  <span aria-hidden>★</span>
                  <span className="font-medium">
                    {Number(
                      provider.providerReviewAggregateResponse.averageRating ||
                        0
                    ).toFixed(1)}
                  </span>
                  <span className="text-zinc-500">
                    ({provider.providerReviewAggregateResponse.totalReviews}{" "}
                    reviews)
                  </span>
                </span>
              </div>
            )}
          </div>
        ) : (
          <div className="text-sm text-rose-600">
            {providerQ.error?.message || "Provider not found"}
          </div>
        )}
      </Card>

      {/* Availability & Booking */}
      <Card title="Check availability and book">
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div>
            <Label>Date</Label>
            <Input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
            />
            {availableDayNames.length > 0 && (
              <div className="mt-1 text-xs text-zinc-500">
                Typically available on: {availableDayNames.join(", ")}
              </div>
            )}
          </div>
          <div className="md:col-span-2">
            <Label>Available slots</Label>
            <div className="flex items-center gap-2 overflow-x-auto py-1">
              {slotsQ.isLoading ? (
                <div className="text-sm text-zinc-500">Loading slots…</div>
              ) : slots.length > 0 ? (
                slots.map((s, idx) => {
                  const active =
                    String(selectedSlot?.startTime) === String(s.startTime);
                  return (
                    <Button
                      key={idx}
                      variant={active ? "primary" : "outline"}
                      size="sm"
                      onClick={() => setSelectedSlot(s)}
                    >
                      {s.label ?? s.startTime}
                    </Button>
                  );
                })
              ) : (
                <div className="text-sm text-zinc-500">
                  No slots returned for this date. You may still request a
                  booking for the date.
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="mt-4 flex items-center gap-2">
          <Button
            disabled={
              !canBook || !selectedSlot || !provider?.providerId || !serviceId
            }
            onClick={() => {
              if (!canBook) {
                toast.error("Only customers can create bookings");
                return;
              }
              if (!selectedSlot) {
                toast.error("Please select a slot first");
                return;
              }
              if (!provider?.providerId || !serviceId) return;
              const params = new URLSearchParams({
                providerId: String(provider.providerId),
                serviceId: String(serviceId),
                date: String(date),
              });
              if (selectedSlot?.startTime)
                params.set("slotStart", String(selectedSlot.startTime));
              if (selectedSlot?.endTime)
                params.set("slotEnd", String(selectedSlot.endTime));
              navigate(`/bookings/create?${params.toString()}`);
            }}
          >
            Book now
          </Button>
          <Button variant="outline" onClick={() => navigate(-1)}>
            Back
          </Button>
        </div>

        {canBook && !selectedSlot && (
          <div className="mt-2 text-xs text-zinc-500">
            Select a slot to enable the Book now button.
          </div>
        )}

        {!canBook && (
          <div className="mt-2 text-xs text-zinc-500">
            Log in as a customer to book this service.
          </div>
        )}
      </Card>
    </div>
  );
}
