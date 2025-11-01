import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Bookings, Users, Services, Payments } from "../../services/api.js";
import { useAvailableSlots } from "../../hooks/useAvailability.js";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import toast from "react-hot-toast";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import { PayPalButtons } from "@paypal/react-paypal-js";

function toISODate(d) {
  const dt = new Date(d);
  return dt.toISOString().slice(0, 10);
}

export default function BookingCreate() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const [query, setQuery] = useState("");
  const [providerId, setProviderId] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [date, setDate] = useState(toISODate(new Date()));
  const [slot, setSlot] = useState("");
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [timeWithinSlot, setTimeWithinSlot] = useState("");
  const [endTimeWithinSlot, setEndTimeWithinSlot] = useState("");
  // Flow state
  const [createdBookingId, setCreatedBookingId] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState("UPI");
  const [isPaying, setIsPaying] = useState(false);

  // Prefill from URL query params
  useEffect(() => {
    const pid = searchParams.get("providerId");
    const sid = searchParams.get("serviceId");
    const d = searchParams.get("date");
    const slotStart = searchParams.get("slotStart");
    const slotEnd = searchParams.get("slotEnd");
    if (pid) setProviderId(String(pid));
    if (sid) setServiceId(String(sid));
    if (d) setDate(d);
    if (slotStart || slotEnd) {
      const s = { startTime: slotStart || "", endTime: slotEnd || "" };
      setSelectedSlot(s);
      setSlot(slotStart || "");
      if (slotStart) setTimeWithinSlot(String(slotStart).slice(0, 5));
    }
  }, [searchParams]);

  // Reset service selection when provider changes
  useEffect(() => {
    setServiceId("");
  }, [providerId]);

  // Providers from backend with server-side filtering
  const providersQ = useQuery({
    queryKey: ["providers", query],
    queryFn: async () => {
      const filter = query
        ? query.includes("@")
          ? { providerEmail: query }
          : { providerName: query }
        : undefined;
      return Users.getProviders(filter, { page: 0, size: 20 });
    },
  });

  // Services for selected provider
  const servicesQ = useQuery({
    queryKey: ["provider-services", providerId],
    queryFn: async () =>
      Services.getAll(
        // Filter by provider; send multiple keys for backend compatibility
        providerId
          ? {
              serviceProviderId: Number(providerId),
              providerId: Number(providerId),
            }
          : {},
        { page: 0, size: 100 }
      ),
    enabled: !!providerId,
  });

  // Available slots when provider, service, and date are present
  const slotsQ = useAvailableSlots(Number(providerId), Number(serviceId), date);

  useEffect(() => {
    setSlot("");
    setSelectedSlot(null);
    setTimeWithinSlot("");
    setEndTimeWithinSlot("");
  }, [providerId, serviceId, date]);

  // Compute end time (+60 min) and clamp to slot end if provided
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

  // Create booking (separate from payment)
  const createBookingMutation = useMutation({
    mutationFn: async () => {
      const start = (timeWithinSlot || selectedSlot?.startTime || slot || "")
        .toString()
        .slice(0, 5);
      const chosenEnd = (endTimeWithinSlot || "").toString().slice(0, 5);
      const computed = computeEndTime(start, selectedSlot?.endTime);
      // Prefer user-chosen end time if valid; otherwise fallback to computed
      let end = chosenEnd || computed;
      // Clamp to slot end if provided
      if (selectedSlot?.endTime) {
        const max = String(selectedSlot.endTime).slice(0, 5);
        if (end > max) end = max;
      }
      const service = selectedService || {};
      const serviceCategory = service.serviceCategory || service.category || "";
      const dto = {
        serviceProviderId: Number(providerId),
        serviceId: Number(serviceId),
        customerId: Number(user?.userId),
        serviceCategory,
        bookingDate: date,
        bookingStartTime: start,
        bookingEndTime: end,
      };
      return Bookings.create(dto);
    },
    onError: (e) => {
      toast.error(e?.response?.data?.message || "Failed to create booking");
    },
    onSuccess: (resp) => {
      const bid = resp?.bookingId || resp?.id;
      setCreatedBookingId(bid || null);
      toast.success("Booking created");
    },
  });

  // Helper: compute amount based on selected service price and chosen time window
  function computeAmount() {
    const pricePerHour = Number(selectedService?.servicePricePerHour || 0);
    const start = (timeWithinSlot || selectedSlot?.startTime || slot || "")
      .toString()
      .slice(0, 5);
    const end = (
      endTimeWithinSlot ||
      computeEndTime(start, selectedSlot?.endTime) ||
      ""
    )
      .toString()
      .slice(0, 5);
    const toMin = (t) => {
      if (!t) return 0;
      const [h, m] = t.split(":").map((n) => parseInt(n, 10));
      return (h || 0) * 60 + (m || 0);
    };
    const mins = Math.max(0, toMin(end) - toMin(start));
    const hours = mins / 60;
    return Number((pricePerHour * hours).toFixed(2));
  }

  // Create booking and initiate payment transaction
  async function handleCreateAndPay() {
    setIsPaying(true);
    try {
      // 1) Ensure booking exists
      const bookingResp = await createBookingMutation.mutateAsync();
      const bookingId = bookingResp?.bookingId || bookingResp?.id;
      if (!bookingId) throw new Error("Missing booking id");

      // 2) Build request shapes
      const pricePerHour = Number(selectedService?.servicePricePerHour || 0);
      const start = (timeWithinSlot || selectedSlot?.startTime || slot || "")
        .toString()
        .slice(0, 5);
      const end = (
        endTimeWithinSlot ||
        computeEndTime(start, selectedSlot?.endTime) ||
        ""
      )
        .toString()
        .slice(0, 5);
      const amount = computeAmount();

      // 3) Get orderId (skip PayPal order for CASH)
      let orderId = `CASH-${bookingId}-${Date.now()}`;
      if (String(paymentMethod).toUpperCase() !== "CASH") {
        const orderReq = {
          serviceId: Number(serviceId),
          slot: { startTime: start, endTime: end },
          pricePerHour,
        };
        orderId = await Payments.createOrder(orderReq);
        if (!orderId) throw new Error("Failed to create payment order");
      }

      // 4) Create transaction as PENDING
      const payReq = {
        orderId,
        bookingId: Number(bookingId),
        customerId: Number(user?.userId),
        amount,
        paymentMethod: String(paymentMethod).toUpperCase(),
      };
      const txn = await Payments.processPayment(payReq);
      const status = txn?.paymentStatus || "PENDING";
      toast.success(`Payment initiated (status: ${status})`);

      // 5) Navigate to details
      navigate(`/bookings/${bookingId}`);
    } catch (e) {
      const msg = e?.response?.data?.message || e?.message || "Payment failed";
      toast.error(msg);
    } finally {
      setIsPaying(false);
    }
  }

  const providers = useMemo(() => {
    const raw = providersQ.data ?? [];
    return Array.isArray(raw?.content)
      ? raw.content
      : Array.isArray(raw)
      ? raw
      : [];
  }, [providersQ.data]);

  const services = useMemo(() => {
    const raw = servicesQ.data ?? [];
    const list = Array.isArray(raw?.content)
      ? raw.content
      : Array.isArray(raw)
      ? raw
      : [];
    return list;
  }, [servicesQ.data]);

  const slots = slotsQ.data ?? [];

  // Validation: ensure end time is after start time and within slot bounds
  const startMins = timeToMinutes(
    timeWithinSlot || selectedSlot?.startTime || slot
  );
  const endMins = timeToMinutes(endTimeWithinSlot);
  const slotEndMins = timeToMinutes(
    selectedSlot?.endTime || searchParams.get("slotEnd")
  );
  const endAfterStart =
    startMins != null && endMins != null ? endMins > startMins : true;
  const withinSlot =
    endMins != null && slotEndMins != null ? endMins <= slotEndMins : true;
  const canSubmit =
    !!providerId &&
    !!serviceId &&
    !!date &&
    !!(timeWithinSlot || selectedSlot?.startTime || slot) &&
    endAfterStart &&
    withinSlot &&
    !createBookingMutation.isPending;

  const selectedProvider = useMemo(
    () => providers.find((p) => String(p.providerId) === String(providerId)),
    [providers, providerId]
  );
  const selectedService = useMemo(
    () =>
      services.find((s) => String(s.id ?? s.serviceId) === String(serviceId)),
    [services, serviceId]
  );

  // No payment, so skip amount derivation
  const showPayPal = String(paymentMethod).toUpperCase() === "PAYPAL";

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Create a booking</h1>
      <Card title="Choose provider">
        <div className="flex flex-col gap-3">
          <Input
            placeholder="Search providers by name…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            disabled={!!searchParams.get("providerId")}
          />
          <div className="max-h-48 overflow-auto rounded-md border">
            <ul className="divide-y">
              {providers.map((p) => (
                <li
                  key={p.providerId}
                  className={
                    "flex items-center justify-between px-3 py-2 text-sm cursor-pointer " +
                    (String(p.providerId) === String(providerId)
                      ? "bg-blue-50 border-l-4 border-blue-500"
                      : "hover:bg-zinc-50")
                  }
                  onClick={() => setProviderId(String(p.providerId))}
                >
                  <div>
                    <div className="font-medium">
                      {p.providerName ?? p.name ?? p.username ?? "Provider"}
                    </div>
                    {(p.providerEmail || p.email) && (
                      <div className="text-zinc-600">
                        {p.providerEmail ?? p.email}
                      </div>
                    )}
                  </div>
                  <div>
                    {String(p.providerId) === String(providerId) ? (
                      <span className="rounded bg-blue-100 px-2 py-1 text-xs font-medium text-blue-700">
                        Selected
                      </span>
                    ) : (
                      <Button
                        variant="outline"
                        onClick={(e) => {
                          e.stopPropagation();
                          setProviderId(String(p.providerId));
                        }}
                        disabled={!!searchParams.get("providerId")}
                      >
                        Select
                      </Button>
                    )}
                  </div>
                </li>
              ))}
              {providersQ.isLoading && (
                <li className="px-3 py-2 text-sm text-zinc-500">Loading…</li>
              )}
              {!providersQ.isLoading && providers.length === 0 && (
                <li className="px-3 py-2 text-sm text-zinc-500">
                  {query
                    ? "No providers match your search."
                    : "No providers found."}
                </li>
              )}
            </ul>
          </div>
        </div>
      </Card>

      <Card title="Service and date">
        <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
          <div>
            <Label>Service</Label>
            <select
              className="w-full rounded-md border px-3 py-2"
              value={serviceId}
              onChange={(e) => setServiceId(e.target.value)}
              disabled={
                !providerId ||
                servicesQ.isLoading ||
                !!searchParams.get("serviceId")
              }
            >
              <option value="">
                {servicesQ.isLoading
                  ? "Loading services…"
                  : services.length > 0
                  ? "Select a service"
                  : "No services found — enter ID below"}
              </option>
              {services.map((s) => (
                <option key={s.id ?? s.serviceId} value={s.id ?? s.serviceId}>
                  {s.serviceName ??
                    s.name ??
                    s.title ??
                    `Service #${s.id ?? s.serviceId}`}
                </option>
              ))}
            </select>
            {services.length === 0 && (
              <div className="mt-2 text-xs text-zinc-500">
                As a fallback, enter service ID manually.
              </div>
            )}
            {services.length === 0 && (
              <div className="mt-2">
                <Input
                  type="number"
                  placeholder="Enter service id"
                  value={serviceId}
                  onChange={(e) => setServiceId(e.target.value)}
                />
              </div>
            )}
          </div>
          <div>
            <Label>Date</Label>
            <Input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
            />
          </div>
          <div>
            <Label>Available slots</Label>
            <select
              className="w-full rounded-md border px-3 py-2"
              value={selectedSlot?.startTime || slot}
              onChange={(e) => {
                const start = e.target.value;
                const found = (slots || []).find(
                  (s) => String(s.startTime) === String(start)
                );
                setSelectedSlot(found || null);
                setSlot(start);
                const startVal = found?.startTime
                  ? String(found.startTime).slice(0, 5)
                  : "";
                setTimeWithinSlot(startVal);
                // Prefill end time to +60m (clamped) when slot changes
                const defaultEnd = computeEndTime(startVal, found?.endTime);
                setEndTimeWithinSlot(defaultEnd || "");
              }}
              disabled={!providerId || !serviceId || !date || slotsQ.isLoading}
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
            {(selectedSlot?.startTime || searchParams.get("slotStart")) && (
              <div className="mt-2">
                <Label>Choose time within slot</Label>
                <Input
                  type="time"
                  value={timeWithinSlot}
                  min={String(
                    selectedSlot?.startTime ||
                      searchParams.get("slotStart") ||
                      ""
                  ).slice(0, 5)}
                  max={String(
                    selectedSlot?.endTime || searchParams.get("slotEnd") || ""
                  ).slice(0, 5)}
                  onChange={(e) => {
                    const newStart = e.target.value;
                    setTimeWithinSlot(newStart);
                    // If end is empty or now before start, adjust end to default
                    if (!endTimeWithinSlot) {
                      setEndTimeWithinSlot(
                        computeEndTime(newStart, selectedSlot?.endTime) || ""
                      );
                    } else {
                      const em = timeToMinutes(endTimeWithinSlot);
                      const sm = timeToMinutes(newStart);
                      if (em != null && sm != null && em <= sm) {
                        setEndTimeWithinSlot(
                          computeEndTime(newStart, selectedSlot?.endTime) || ""
                        );
                      }
                    }
                  }}
                />
                <p className="mt-1 text-xs text-zinc-500">
                  Allowed range:{" "}
                  {(
                    selectedSlot?.startTime ||
                    searchParams.get("slotStart") ||
                    ""
                  )?.slice(0, 5)}
                  {selectedSlot?.endTime || searchParams.get("slotEnd")
                    ? ` - ${(
                        selectedSlot?.endTime ||
                        searchParams.get("slotEnd") ||
                        ""
                      )?.slice(0, 5)}`
                    : ""}
                </p>
                <div className="mt-3">
                  <Label>End time</Label>
                  <Input
                    type="time"
                    value={endTimeWithinSlot}
                    min={(timeWithinSlot || "").slice(0, 5)}
                    max={String(
                      selectedSlot?.endTime || searchParams.get("slotEnd") || ""
                    ).slice(0, 5)}
                    onChange={(e) => setEndTimeWithinSlot(e.target.value)}
                  />
                  {endTimeWithinSlot && !endAfterStart && (
                    <p className="mt-1 text-xs text-red-600">
                      End time must be after start time.
                    </p>
                  )}
                  {endTimeWithinSlot && !withinSlot && (
                    <p className="mt-1 text-xs text-red-600">
                      End time must be within the selected slot.
                    </p>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </Card>

      {/* Confirmation step */}
      {providerId && serviceId && date && (
        <Card title="Confirm details">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <div className="text-sm text-zinc-500">Provider</div>
              <div className="font-medium">
                {selectedProvider?.providerName ??
                  selectedProvider?.name ??
                  selectedProvider?.username ??
                  `#${providerId}`}
              </div>
            </div>
            <div>
              <div className="text-sm text-zinc-500">Service</div>
              <div className="font-medium">
                {selectedService?.name ??
                  selectedService?.title ??
                  `#${serviceId}`}
              </div>
            </div>
            <div>
              <div className="text-sm text-zinc-500">When</div>
              <div className="font-medium">
                {date}{" "}
                {(timeWithinSlot || selectedSlot?.startTime || slot) && (
                  <>
                    {(timeWithinSlot || selectedSlot?.startTime || slot)
                      ?.toString()
                      .slice(0, 5)}{" "}
                    -{" "}
                    {(
                      endTimeWithinSlot ||
                      computeEndTime(timeWithinSlot, selectedSlot?.endTime)
                    )
                      ?.toString()
                      .slice(0, 5)}
                  </>
                )}
              </div>
            </div>
            {/* Payment */}
            <div>
              <div className="text-sm text-zinc-500">Payment method</div>
              <select
                className="mt-1 w-full rounded-md border px-3 py-2"
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
              >
                <option value="UPI">UPI</option>
                <option value="CREDIT_CARD">Credit/Debit Card</option>
                <option value="NET_BANKING">Net Banking</option>
                <option value="WALLET">Wallet</option>
                <option value="CASH">Cash</option>
                <option value="PAYPAL">PayPal</option>
              </select>
            </div>
            <div>
              <div className="text-sm text-zinc-500">Estimated amount</div>
              <div className="font-medium">₹{computeAmount()}</div>
            </div>
          </div>

          <div className="mt-4 flex flex-col gap-3">
            {!showPayPal ? (
              <div className="flex items-center gap-2">
                <Button
                  onClick={handleCreateAndPay}
                  disabled={!canSubmit || isPaying}
                >
                  {isPaying ? "Processing…" : "Create & Pay"}
                </Button>
                <Button
                  variant="outline"
                  onClick={async () => {
                    try {
                      const resp = await createBookingMutation.mutateAsync();
                      const bid = resp?.id ?? resp?.bookingId;
                      navigate(bid ? `/bookings/${bid}` : "/bookings");
                    } catch {
                      /* handled */
                    }
                  }}
                  disabled={!canSubmit || createBookingMutation.isPending}
                >
                  {createBookingMutation.isPending
                    ? "Creating…"
                    : "Create without payment"}
                </Button>
                <Button variant="outline" onClick={() => navigate("/bookings")}>
                  Cancel
                </Button>
              </div>
            ) : (
              <div className="space-y-2">
                <div className="text-sm text-zinc-600">
                  Complete your payment securely with PayPal
                </div>
                <PayPalButtons
                  style={{ layout: "vertical", shape: "rect" }}
                  disabled={!canSubmit}
                  forceReRender={[computeAmount()]}
                  createOrder={async () => {
                    try {
                      // Ensure booking exists
                      let bid = createdBookingId;
                      if (!bid) {
                        const b = await createBookingMutation.mutateAsync();
                        bid = b?.bookingId || b?.id;
                        setCreatedBookingId(bid || null);
                      }
                      // Build order via backend
                      const pricePerHour = Number(
                        selectedService?.servicePricePerHour || 0
                      );
                      const start = (
                        timeWithinSlot ||
                        selectedSlot?.startTime ||
                        slot ||
                        ""
                      )
                        .toString()
                        .slice(0, 5);
                      const end = (
                        endTimeWithinSlot ||
                        computeEndTime(start, selectedSlot?.endTime) ||
                        ""
                      )
                        .toString()
                        .slice(0, 5);
                      const orderId = await Payments.createOrder({
                        serviceId: Number(serviceId),
                        slot: { startTime: start, endTime: end },
                        pricePerHour,
                      });
                      if (!orderId) throw new Error("No order id returned");
                      return orderId;
                    } catch (e) {
                      const msg =
                        e?.response?.data?.message ||
                        e?.message ||
                        "Failed to create PayPal order";
                      toast.error(msg);
                      throw e;
                    }
                  }}
                  onApprove={async (data, actions) => {
                    try {
                      const orderId = data?.orderID;
                      const bookingId = createdBookingId;
                      if (!orderId || !bookingId)
                        throw new Error("Missing order or booking id");
                      const amount = computeAmount();
                      // Let backend capture and mark transaction
                      const txn = await Payments.processPayment({
                        orderId,
                        bookingId: Number(bookingId),
                        customerId: Number(user?.userId),
                        amount,
                        // Backend enum does not include PAYPAL; map PayPal UI to CREDIT_CARD for storage
                        paymentMethod: "CREDIT_CARD",
                      });
                      const status = txn?.paymentStatus || "COMPLETED";
                      toast.success(`Payment ${status.toLowerCase()}`);
                      navigate(`/bookings/${bookingId}`);
                    } catch (e) {
                      const msg =
                        e?.response?.data?.message ||
                        e?.message ||
                        "PayPal approval failed";
                      toast.error(msg);
                    }
                  }}
                  onError={(err) => {
                    const msg = err?.message || "PayPal error";
                    toast.error(msg);
                  }}
                />
                <div>
                  <Button
                    variant="outline"
                    onClick={() => navigate("/bookings")}
                  >
                    Cancel
                  </Button>
                </div>
              </div>
            )}
          </div>
        </Card>
      )}
    </div>
  );
}
