import { useEffect, useMemo, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Availability, Bookings, Reviews } from "../../services/api.js";
import { useAvailableSlots } from "../../hooks/useAvailability.js";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import toast from "react-hot-toast";
import { useAuth } from "../../context/AuthContext.jsx";

function formatDateTime(d) {
  try {
    return new Date(d).toLocaleString();
  } catch {
    return String(d);
  }
}

export default function BookingDetails() {
  const { user } = useAuth();
  const { id } = useParams();
  const [search] = useSearchParams();
  const q = useQuery({
    queryKey: ["booking", id],
    queryFn: () => Bookings.getById(id),
    enabled: !!id,
  });

  const b = q.data;

  // If the current booking is RESCHEDULED and has a rescheduledToId, fetch the new booking details
  const isRescheduled = useMemo(
    () =>
      String(b?.bookingStatus ?? b?.status ?? "").toUpperCase() ===
      "RESCHEDULED",
    [b]
  );
  const rescheduledToId = useMemo(() => {
    const v = b?.rescheduledToId;
    if (!v || v === "N/A") return null;
    return String(v);
  }, [b]);

  const qRescheduled = useQuery({
    queryKey: ["booking", "rescheduled", rescheduledToId],
    queryFn: () => Bookings.getById(rescheduledToId),
    enabled: !!rescheduledToId && isRescheduled,
  });

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
  const isCompleted = useMemo(
    () =>
      String(b?.bookingStatus ?? b?.status ?? "")
        .toUpperCase()
        .includes("COMPLETED"),
    [b]
  );
  const isCancelled = useMemo(
    () =>
      String(b?.bookingStatus ?? b?.status ?? "")
        .toUpperCase()
        .includes("CANCELLED"),
    [b]
  );
  const isTerminal = isCompleted || isCancelled;

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
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [timeWithinSlot, setTimeWithinSlot] = useState("");
  const [endTimeWithinSlot, setEndTimeWithinSlot] = useState("");

  useEffect(() => {
    setSelectedSlot(null);
    setTimeWithinSlot("");
    setEndTimeWithinSlot("");
  }, [newDate]);

  const slotsQ = useAvailableSlots(
    Number(providerId),
    Number(serviceId),
    newDate
  );
  const slots = slotsQ.data ?? [];

  // --- Add Review state ---
  const [reviewOpen, setReviewOpen] = useState(
    search.get("review") === "1" || false
  );
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");

  const addReviewMutation = useMutation({
    mutationFn: async () => {
      if (!user?.userType || user.userType !== "CUSTOMER") {
        throw new Error("Only customers can add reviews");
      }
      const payload = {
        serviceProviderId: Number(providerId),
        serviceId: Number(serviceId),
        rating: Number(rating),
        comment: comment?.trim() || null,
      };
      return Reviews.add(payload);
    },
    onSuccess: () => {
      toast.success("Review submitted");
      setReviewOpen(false);
      setComment("");
    },
    onError: (e) =>
      toast.error(
        e?.response?.data?.message || e?.message || "Failed to submit review"
      ),
  });

  function computeEndTime(start, slotEnd) {
    if (!start) return slotEnd || "";
    try {
      const [h, m] = String(start)
        .split(":")
        .map((v) => parseInt(v, 10));
      const d = new Date();
      d.setHours(h || 0, m || 0, 0, 0);
      d.setMinutes(d.getMinutes() + 60);
      const hh = String(d.getHours()).padStart(2, "0");
      const mm = String(d.getMinutes()).padStart(2, "0");
      const plus1h = `${hh}:${mm}`;
      if (slotEnd && plus1h > String(slotEnd).slice(0, 5))
        return String(slotEnd).slice(0, 5);
      return plus1h;
    } catch {
      return slotEnd || "";
    }
  }

  function timeToMinutes(t) {
    if (!t) return null;
    const [hh, mm] = String(t).slice(0, 5).split(":");
    const h = parseInt(hh, 10);
    const m = parseInt(mm, 10);
    if (Number.isNaN(h) || Number.isNaN(m)) return null;
    return h * 60 + m;
  }

  const rescheduleMutation = useMutation({
    mutationFn: async () => {
      const start = (timeWithinSlot || selectedSlot?.startTime || "")
        .toString()
        .slice(0, 5);
      const computedEnd = computeEndTime(start, selectedSlot?.endTime);
      const end = (endTimeWithinSlot || computedEnd || "")
        .toString()
        .slice(0, 5);
      /**
       * Backend expects:
       * {
       *   newBookingDate: YYYY-MM-DD,
       *   newBookingStartTime: HH:mm,
       *   newBookingEndTime: HH:mm
       * }
       * Any null fields fall back to existing values on server.
       */
      const payload = {
        newBookingDate: newDate || null,
        newBookingStartTime: start || null,
        newBookingEndTime: end || null,
      };
      return Bookings.reschedule(id, payload);
    },
    onSuccess: () => {
      toast.success("Booking rescheduled");
      setRescheduleOpen(false);
      q.refetch();
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to reschedule booking"),
  });

  // Availability check for selected date/time
  const [availability, setAvailability] = useState(null); // AVAILABLE | BLOCKED | OUTSIDE_WORKING_HOURS
  const [checkingAvail, setCheckingAvail] = useState(false);
  async function handleCheckAvailability() {
    try {
      setCheckingAvail(true);
      const start = (timeWithinSlot || selectedSlot?.startTime || "")
        .toString()
        .slice(0, 5);
      const computedEnd = computeEndTime(start, selectedSlot?.endTime);
      const end = (endTimeWithinSlot || computedEnd || "")
        .toString()
        .slice(0, 5);
      const req = {
        serviceProviderId: Number(providerId),
        serviceId: Number(serviceId),
        date: newDate,
        startTime: start,
        endTime: end,
      };
      const res = await Availability.checkAvailability(req);
      const status = res?.status ?? res; // support plain string fallback
      setAvailability(status);
      if (String(status).toUpperCase() === "AVAILABLE") {
        toast.success("Selected time is available");
      } else if (status) {
        toast.error(`Not available (${status})`);
      } else {
        toast("Could not determine availability");
      }
    } catch (e) {
      toast.error(e?.response?.data?.message || "Failed to check availability");
    } finally {
      setCheckingAvail(false);
    }
  }

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
                <div className="font-medium">{b.bookingId ?? b.id}</div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Status</div>
                <div className="font-medium">
                  {b.bookingStatus ?? b.status ?? ""}
                </div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">When</div>
                <div className="font-medium">
                  {b.bookingDate ? (
                    <>
                      <span>{b.bookingDate}</span>{" "}
                      <span>
                        {(b.bookingStartTime || "").toString().slice(0, 5)} -{" "}
                        {(b.bookingEndTime || "").toString().slice(0, 5)}
                      </span>
                    </>
                  ) : (
                    formatDateTime(b.createdAt)
                  )}
                </div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Service ID</div>
                <div className="font-medium">{b.serviceId ?? "-"}</div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Service category</div>
                <div className="font-medium">{b.serviceCategory ?? "-"}</div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Provider ID</div>
                <div className="font-medium">{b.serviceProviderId ?? "-"}</div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Customer ID</div>
                <div className="font-medium">{b.customerId ?? "-"}</div>
              </div>
              <div>
                <div className="text-sm text-zinc-500">Created at</div>
                <div className="font-medium">{formatDateTime(b.createdAt)}</div>
              </div>
              {b.rescheduledToId && b.rescheduledToId !== "N/A" && (
                <div>
                  <div className="text-sm text-zinc-500">Rescheduled to</div>
                  <div className="font-medium">{b.rescheduledToId}</div>
                </div>
              )}
            </div>

            <div className="flex flex-wrap gap-2">
              {!isTerminal && (
                <>
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
                    {cancelMutation.isPending
                      ? "Cancelling…"
                      : "Cancel booking"}
                  </Button>
                </>
              )}
              {isCompleted && user?.userType === "CUSTOMER" && (
                <Button
                  variant="outline"
                  onClick={() => setReviewOpen((s) => !s)}
                  disabled={!providerId || !serviceId}
                >
                  {reviewOpen ? "Close review" : "Add review"}
                </Button>
              )}
            </div>

            {isRescheduled && rescheduledToId && (
              <div className="rounded-md border p-3">
                <div className="mb-2 flex items-center justify-between gap-2">
                  <h2 className="text-lg font-medium">Rescheduled to</h2>
                  <Link
                    to={`/bookings/${rescheduledToId}`}
                    className="text-sm text-blue-600 hover:underline"
                  >
                    Open rescheduled booking
                  </Link>
                </div>
                {qRescheduled.isLoading && (
                  <div className="text-sm text-zinc-500">Loading…</div>
                )}
                {qRescheduled.isError && (
                  <div className="text-sm text-red-600">
                    {qRescheduled.error?.response?.data?.message ||
                      "Failed to load rescheduled booking."}
                  </div>
                )}
                {qRescheduled.data && (
                  <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                    <div>
                      <div className="text-sm text-zinc-500">Booking ID</div>
                      <div className="font-medium">
                        {qRescheduled.data.bookingId ?? qRescheduled.data.id}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-zinc-500">Status</div>
                      <div className="font-medium">
                        {qRescheduled.data.bookingStatus ??
                          qRescheduled.data.status ??
                          ""}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-zinc-500">When</div>
                      <div className="font-medium">
                        {qRescheduled.data.bookingDate ? (
                          <>
                            <span>{qRescheduled.data.bookingDate}</span>{" "}
                            <span>
                              {(qRescheduled.data.bookingStartTime || "")
                                .toString()
                                .slice(0, 5)}{" "}
                              -{" "}
                              {(qRescheduled.data.bookingEndTime || "")
                                .toString()
                                .slice(0, 5)}
                            </span>
                          </>
                        ) : (
                          formatDateTime(qRescheduled.data.createdAt)
                        )}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-zinc-500">Service ID</div>
                      <div className="font-medium">
                        {qRescheduled.data.serviceId ?? "-"}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-zinc-500">
                        Service category
                      </div>
                      <div className="font-medium">
                        {qRescheduled.data.serviceCategory ?? "-"}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-zinc-500">Provider ID</div>
                      <div className="font-medium">
                        {qRescheduled.data.serviceProviderId ?? "-"}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-zinc-500">Customer ID</div>
                      <div className="font-medium">
                        {qRescheduled.data.customerId ?? "-"}
                      </div>
                    </div>
                    <div>
                      <div className="text-sm text-zinc-500">Created at</div>
                      <div className="font-medium">
                        {formatDateTime(qRescheduled.data.createdAt)}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}

            {rescheduleOpen && !isTerminal && (
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
                      value={selectedSlot?.startTime || ""}
                      onChange={(e) => {
                        const start = e.target.value;
                        const found = (slots || []).find(
                          (s) => String(s.startTime) === String(start)
                        );
                        setSelectedSlot(found || null);
                        const startVal = found?.startTime
                          ? String(found.startTime).slice(0, 5)
                          : "";
                        setTimeWithinSlot(startVal);
                        const defaultEnd = computeEndTime(
                          startVal,
                          found?.endTime
                        );
                        setEndTimeWithinSlot(defaultEnd || "");
                      }}
                      disabled={!newDate || slotsQ.isLoading}
                    >
                      <option value="">
                        {slotsQ.isLoading
                          ? "Loading slots…"
                          : "Select a slot (optional)"}
                      </option>
                      {slots.map((s, idx) => (
                        <option key={idx} value={s.startTime}>
                          {s.label ?? s.startTime}
                        </option>
                      ))}
                    </select>
                  </div>
                  {selectedSlot?.startTime && (
                    <div className="md:col-span-3 grid grid-cols-1 gap-3 md:grid-cols-2">
                      <div>
                        <Label>Start time</Label>
                        <Input
                          type="time"
                          value={timeWithinSlot}
                          min={String(selectedSlot?.startTime || "").slice(
                            0,
                            5
                          )}
                          max={String(selectedSlot?.endTime || "").slice(0, 5)}
                          onChange={(e) => {
                            const newStart = e.target.value;
                            setTimeWithinSlot(newStart);
                            const em = timeToMinutes(endTimeWithinSlot);
                            const sm = timeToMinutes(newStart);
                            if (em != null && sm != null && em <= sm) {
                              setEndTimeWithinSlot(
                                computeEndTime(
                                  newStart,
                                  selectedSlot?.endTime
                                ) || ""
                              );
                            }
                          }}
                        />
                      </div>
                      <div>
                        <Label>End time</Label>
                        <Input
                          type="time"
                          value={endTimeWithinSlot}
                          min={(timeWithinSlot || "").slice(0, 5)}
                          max={String(selectedSlot?.endTime || "").slice(0, 5)}
                          onChange={(e) => setEndTimeWithinSlot(e.target.value)}
                        />
                        {(() => {
                          const em = timeToMinutes(endTimeWithinSlot);
                          const sm = timeToMinutes(timeWithinSlot);
                          const maxm = timeToMinutes(selectedSlot?.endTime);
                          return (
                            <>
                              {em != null && sm != null && em <= sm && (
                                <p className="mt-1 text-xs text-red-600">
                                  End time must be after start time.
                                </p>
                              )}
                              {em != null && maxm != null && em > maxm && (
                                <p className="mt-1 text-xs text-red-600">
                                  End time must be within the selected slot.
                                </p>
                              )}
                            </>
                          );
                        })()}
                      </div>
                    </div>
                  )}
                  <div className="flex items-end gap-2">
                    <Button
                      variant="outline"
                      onClick={handleCheckAvailability}
                      disabled={
                        !newDate ||
                        !timeWithinSlot ||
                        checkingAvail ||
                        slotsQ.isLoading
                      }
                    >
                      {checkingAvail ? "Checking…" : "Check availability"}
                    </Button>
                    {availability && (
                      <span
                        className={
                          "self-center rounded px-2 py-1 text-xs font-medium " +
                          (String(availability).toUpperCase() === "AVAILABLE"
                            ? "bg-green-100 text-green-700"
                            : "bg-red-100 text-red-700")
                        }
                      >
                        {String(availability)}
                      </span>
                    )}
                    <Button
                      onClick={() => rescheduleMutation.mutate()}
                      disabled={!newDate || rescheduleMutation.isPending}
                      className="ml-auto"
                    >
                      {rescheduleMutation.isPending
                        ? "Rescheduling…"
                        : "Confirm reschedule"}
                    </Button>
                  </div>
                </div>
              </div>
            )}

            {reviewOpen && isCompleted && user?.userType === "CUSTOMER" && (
              <div className="rounded-md border p-3">
                <h2 className="mb-2 text-lg font-medium">Add your review</h2>
                <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                  <div>
                    <Label>Rating</Label>
                    <select
                      className="w-full rounded-md border px-3 py-2"
                      value={rating}
                      onChange={(e) => setRating(parseInt(e.target.value, 10))}
                    >
                      {[5, 4, 3, 2, 1].map((r) => (
                        <option key={r} value={r}>
                          {r} star{r > 1 ? "s" : ""}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="md:col-span-2">
                    <Label>Comment (optional)</Label>
                    <textarea
                      className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm shadow-sm placeholder:text-zinc-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/50"
                      rows={3}
                      placeholder="Share your experience"
                      value={comment}
                      onChange={(e) => setComment(e.target.value)}
                    />
                  </div>
                  <div className="md:col-span-2 flex items-center gap-2">
                    <Button
                      onClick={() => addReviewMutation.mutate()}
                      disabled={
                        addReviewMutation.isPending ||
                        !providerId ||
                        !serviceId ||
                        (!user?.id && !user?.userId) ||
                        !(rating >= 1 && rating <= 5)
                      }
                    >
                      {addReviewMutation.isPending
                        ? "Submitting…"
                        : "Submit review"}
                    </Button>
                    <span className="text-xs text-zinc-500">
                      Reviews are allowed only after a booking is completed.
                    </span>
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
