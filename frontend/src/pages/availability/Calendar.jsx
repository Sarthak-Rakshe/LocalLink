import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../../context/AuthContext.jsx";
import { Availability } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import Skeleton from "../../components/ui/Skeleton.jsx";

function toISODate(d) {
  const dt = new Date(d);
  return dt.toISOString().slice(0, 10);
}

export default function AvailabilityCalendar() {
  const { user } = useAuth();
  const providerId = user?.id ?? user?.userId;
  const [serviceId, setServiceId] = useState("");
  const [date, setDate] = useState(toISODate(new Date()));

  const slotsQ = useQuery({
    queryKey: ["available-slots", providerId, serviceId, date],
    queryFn: async () =>
      Availability.getAvailableSlots(
        Number(providerId),
        Number(serviceId),
        date
      ),
    enabled: !!providerId && !!serviceId && !!date,
  });

  const { slots, isDayAvailable } = useMemo(() => {
    const res = slotsQ.data ?? null;
    const availableSlots = Array.isArray(res?.availableSlots)
      ? res.availableSlots
      : Array.isArray(res)
      ? res
      : [];
    return {
      slots: availableSlots,
      isDayAvailable: Boolean(res?.isDayAvailable),
    };
  }, [slotsQ.data]);

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Availability calendar</h1>
      <Card title="Filters">
        <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
          <div>
            <Label>Service ID</Label>
            <Input
              type="number"
              placeholder="Enter service id"
              value={serviceId}
              onChange={(e) => setServiceId(e.target.value)}
            />
          </div>
          <div>
            <Label>Date</Label>
            <Input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
            />
          </div>
        </div>
      </Card>

      <Card title="Available slots">
        {slotsQ.isIdle && (
          <div className="text-sm text-zinc-600">
            Enter a service id to see available slots.
          </div>
        )}
        {slotsQ.isLoading && (
          <div className="space-y-2">
            <Skeleton className="h-4 w-36" />
            <Skeleton className="h-6 w-full" />
            <Skeleton className="h-6 w-full" />
          </div>
        )}
        {slotsQ.isError && (
          <div className="text-sm text-red-600">
            {slotsQ.error?.response?.data?.message || "Failed to load slots."}
          </div>
        )}
        {!slotsQ.isLoading && !slotsQ.isError && slots.length === 0 && (
          <EmptyState
            title="No available slots"
            message="Try a different date or adjust your rules."
          />
        )}
        <div className="mb-2">
          {slotsQ.isSuccess && (
            <span
              className={`inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-medium ${
                isDayAvailable
                  ? "bg-emerald-100 text-emerald-800"
                  : "bg-zinc-100 text-zinc-700"
              }`}
            >
              {isDayAvailable ? "Day available" : "Day unavailable"}
            </span>
          )}
        </div>
        <div className="flex flex-wrap gap-2">
          {slots.map((s, idx) => {
            const start = (s?.startTime || "").toString().slice(0, 5);
            const end = (s?.endTime || "").toString().slice(0, 5);
            return (
              <span
                key={idx}
                className="rounded-md border px-2 py-1 text-sm text-zinc-700"
              >
                {start && end ? `${start} - ${end}` : s?.label || String(s)}
              </span>
            );
          })}
        </div>
      </Card>
    </div>
  );
}
