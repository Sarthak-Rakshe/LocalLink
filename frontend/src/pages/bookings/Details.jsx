import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Availability, Bookings } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import toast from "react-hot-toast";

function formatDateTime(d) {
  try {
    return new Date(d).toLocaleString();
  } catch {
    return String(d);
  }
}

export default function BookingDetails() {
  const { id } = useParams();
  const q = useQuery({
    queryKey: ["booking", id],
    queryFn: () => Bookings.getById(id),
    enabled: !!id,
  });

  const b = q.data;

  // Derive provider/service ids with fallbacks
  const providerId = useMemo(
    () =>
      b?.serviceProviderId ??
      b?.providerId ??
      b?.serviceProvider?.id ??
      b?.provider?.id,
    [b]
  );
  const serviceId = useMemo(() => b?.serviceId ?? b?.service?.id, [b]);

  // Cancel booking
  const cancelMutation = useMutation({
    mutationFn: async () => Bookings.updateStatus(id, "CANCELLED"),
    onSuccess: () => {
      toast.success("Booking cancelled");
      q.refetch();
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to cancel booking"),
  });

  // Reschedule state
  const [rescheduleOpen, setRescheduleOpen] = useState(false);
  const [newDate, setNewDate] = useState("");
  const [newSlot, setNewSlot] = useState("");

  useEffect(() => {
    setNewSlot("");
  }, [newDate]);

  const slotsQ = useQuery({
    queryKey: ["reschedule-slots", providerId, serviceId, newDate],
    queryFn: async () =>
      Availability.getAvailableSlots(
        Number(providerId),
        Number(serviceId),
        newDate
      ),
    enabled: !!providerId && !!serviceId && !!newDate,
  });

  const slots = useMemo(() => {
    const raw = slotsQ.data ?? [];
    return Array.isArray(raw) ? raw : raw?.slots ?? [];
  }, [slotsQ.data]);

  const rescheduleMutation = useMutation({
    mutationFn: async () =>
      Bookings.reschedule(id, { startTime: newSlot || newDate }),
    onSuccess: () => {
      toast.success("Booking rescheduled");
      setRescheduleOpen(false);
      q.refetch();
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to reschedule booking"),
  });

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Booking details</h1>
      <Card>
        {q.isLoading && <div className="text-sm text-zinc-500">Loading…</div>}
        {q.isError && (
          <div className="text-sm text-red-600">
            {q.error?.response?.data?.message || "Failed to load booking."}
          </div>
        )}

        {b && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <div className="text-sm text-zinc-500">Booking ID</div>
                <div className="font-medium">{b.id ?? b.bookingId}</div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Status</div>
                <div className="font-medium">{b.status ?? ""}</div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">When</div>
                <div className="font-medium">
                  {formatDateTime(b.date ?? b.startTime ?? b.createdAt)}
                </div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Service</div>
                <div className="font-medium">
                  {b.serviceName ?? b.service?.name ?? "Service"}
                </div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Provider</div>
                <div className="font-medium">
                  {b.providerName ??
                    b.serviceProviderName ??
                    b.provider?.name ??
                    "Provider"}
                </div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Customer</div>
                <div className="font-medium">
                  {b.customerName ?? b.customer?.name ?? "Customer"}
                </div>
              </div>
            </div>

            <div className="flex flex-wrap gap-2">
              <Button
                variant="outline"
                onClick={() => setRescheduleOpen((s) => !s)}
                disabled={!providerId || !serviceId}
              >
                {rescheduleOpen ? "Close reschedule" : "Reschedule"}
              </Button>
              <Button
                variant="danger"
                onClick={() => cancelMutation.mutate()}
                disabled={cancelMutation.isPending}
              >
                {cancelMutation.isPending ? "Cancelling…" : "Cancel booking"}
              </Button>
            </div>

            {rescheduleOpen && (
              <div className="rounded-md border p-3">
                <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                  <div>
                    <Label>New date</Label>
                    <Input
                      type="date"
                      value={newDate}
                      onChange={(e) => setNewDate(e.target.value)}
                    />
                  </div>
                  <div>
                    <Label>Available slots</Label>
                    <select
                      className="w-full rounded-md border px-3 py-2"
                      value={newSlot}
                      onChange={(e) => setNewSlot(e.target.value)}
                      disabled={!newDate || slotsQ.isLoading}
                    >
                      <option value="">
                        {slotsQ.isLoading
                          ? "Loading slots…"
                          : "Select a slot (optional)"}
                      </option>
                      {slots.map((s, idx) => (
                        <option key={idx} value={s?.startTime ?? s?.time ?? s}>
                          {s?.label ?? s?.startTime ?? s}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="flex items-end">
                    <Button
                      onClick={() => rescheduleMutation.mutate()}
                      disabled={!newDate || rescheduleMutation.isPending}
                      className="w-full"
                    >
                      {rescheduleMutation.isPending
                        ? "Rescheduling…"
                        : "Confirm reschedule"}
                    </Button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </Card>
    </div>
  );
}
