import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Availability,
  Bookings,
  Users,
  Services,
  Payments,
} from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import toast from "react-hot-toast";
import { useNavigate } from "react-router-dom";

function toISODate(d) {
  const dt = new Date(d);
  return dt.toISOString().slice(0, 10);
}

export default function BookingCreate() {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [providerId, setProviderId] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [date, setDate] = useState(toISODate(new Date()));
  const [slot, setSlot] = useState("");
  const [amount, setAmount] = useState("");

  // Flow states
  const [createdBookingId, setCreatedBookingId] = useState(null);
  const [orderId, setOrderId] = useState(null);

  // Provider search
  const providersQ = useQuery({
    queryKey: ["providers", query],
    queryFn: async () => Users.getProviders({ q: query, page: 0, size: 10 }),
  });

  // Services for selected provider
  const servicesQ = useQuery({
    queryKey: ["provider-services", providerId],
    queryFn: async () =>
      Services.getAll(
        // Try filtering by provider; backend should support this in query filter
        providerId ? { serviceProviderId: Number(providerId) } : {},
        { page: 0, size: 100 }
      ),
    enabled: !!providerId,
  });

  // Available slots when provider, service, and date are present
  const slotsQ = useQuery({
    queryKey: ["slots", providerId, serviceId, date],
    queryFn: async () =>
      Availability.getAvailableSlots(
        Number(providerId),
        Number(serviceId),
        date
      ),
    enabled: !!providerId && !!serviceId && !!date,
  });

  useEffect(() => {
    setSlot("");
  }, [providerId, serviceId, date]);

  // Create booking (used during confirm step)
  const createBookingMutation = useMutation({
    mutationFn: async () => {
      const dto = {
        serviceProviderId: Number(providerId),
        serviceId: Number(serviceId),
        startTime: slot || date,
      };
      return Bookings.create(dto);
    },
    onError: (e) => {
      toast.error(e?.response?.data?.message || "Failed to create booking");
    },
  });

  // Payment order creation
  const createOrderMutation = useMutation({
    mutationFn: async (amt) => Payments.createOrder(Number(amt)),
    onError: (e) => {
      toast.error(e?.response?.data?.message || "Failed to create order");
    },
  });

  // Payment processing
  const processPaymentMutation = useMutation({
    mutationFn: async ({ orderId: oid, bookingId: bid, amt }) =>
      Payments.processPayment({ orderId: oid, bookingId: bid, amount: amt }),
    onError: (e) => {
      toast.error(e?.response?.data?.message || "Payment failed");
    },
  });

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

  const slots = useMemo(() => {
    const raw = slotsQ.data ?? [];
    return Array.isArray(raw) ? raw : raw?.slots ?? [];
  }, [slotsQ.data]);

  const selectedProvider = useMemo(
    () =>
      providers.find((p) => String(p.id ?? p.userId) === String(providerId)),
    [providers, providerId]
  );
  const selectedService = useMemo(
    () =>
      services.find((s) => String(s.id ?? s.serviceId) === String(serviceId)),
    [services, serviceId]
  );

  // Derive a default amount from service if available (editable)
  useEffect(() => {
    if (!selectedService) return;
    const inferred =
      selectedService.price ??
      selectedService.rate ??
      selectedService.amount ??
      "";
    setAmount(
      inferred !== undefined && inferred !== null ? String(inferred) : ""
    );
  }, [selectedService]);

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Create a booking</h1>
      <Card title="Choose provider">
        <div className="flex flex-col gap-3">
          <Input
            placeholder="Search providers by name…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <div className="max-h-48 overflow-auto rounded-md border">
            <ul className="divide-y">
              {providers.map((p) => (
                <li
                  key={p.id ?? p.userId}
                  className="flex items-center justify-between px-3 py-2 text-sm"
                >
                  <div>
                    <div className="font-medium">
                      {p.name ?? p.username ?? "Provider"}
                    </div>
                    {p.email && <div className="text-zinc-600">{p.email}</div>}
                  </div>
                  <Button
                    variant="outline"
                    onClick={() => setProviderId(String(p.id ?? p.userId))}
                  >
                    Select
                  </Button>
                </li>
              ))}
              {providers.length === 0 && (
                <li className="px-3 py-2 text-sm text-zinc-500">
                  No providers found.
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
              disabled={!providerId || servicesQ.isLoading}
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
                  {s.name ?? s.title ?? `Service #${s.id ?? s.serviceId}`}
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
              value={slot}
              onChange={(e) => setSlot(e.target.value)}
              disabled={!providerId || !serviceId || !date || slotsQ.isLoading}
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
        </div>
      </Card>

      {/* Confirmation step */}
      {providerId && serviceId && date && (
        <Card title="Confirm details">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <div className="text-sm text-zinc-500">Provider</div>
              <div className="font-medium">
                {selectedProvider?.name ??
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
              <div className="font-medium">{slot || date}</div>
            </div>
            <div>
              <Label>Amount</Label>
              <Input
                type="number"
                min="0"
                step="0.01"
                placeholder="Enter amount"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
              />
            </div>
          </div>

          {!createdBookingId && (
            <div className="mt-4 flex items-center gap-2">
              <Button
                onClick={async () => {
                  try {
                    const resp = await createBookingMutation.mutateAsync();
                    const bid = resp?.id ?? resp?.bookingId;
                    if (!bid) throw new Error("Missing booking id");
                    setCreatedBookingId(bid);
                    toast.success("Booking created");
                    // Create order immediately
                    const order = await createOrderMutation.mutateAsync(
                      amount || 0
                    );
                    const oid =
                      order?.id ?? order?.orderId ?? order?.paypalOrderId;
                    if (!oid) {
                      toast.error("Order id missing from response");
                    } else {
                      setOrderId(oid);
                      toast.success("Order created");
                    }
                  } catch (e) {
                    // errors already toasted
                  }
                }}
                disabled={
                  !providerId ||
                  !serviceId ||
                  !date ||
                  createBookingMutation.isPending ||
                  createOrderMutation.isPending ||
                  !amount
                }
              >
                {createBookingMutation.isPending ||
                createOrderMutation.isPending
                  ? "Preparing checkout…"
                  : "Confirm & Pay"}
              </Button>
              <Button variant="outline" onClick={() => navigate("/bookings")}>
                Cancel
              </Button>
            </div>
          )}

          {createdBookingId && (
            <div className="mt-4 space-y-2">
              <div className="text-sm text-zinc-600">
                Booking #{createdBookingId} created.{" "}
                {orderId ? `Order #${orderId} ready.` : "Creating order…"}
              </div>
              {orderId && (
                <div className="flex items-center gap-2">
                  <Button
                    onClick={async () => {
                      try {
                        await processPaymentMutation.mutateAsync({
                          orderId,
                          bookingId: createdBookingId,
                          amt: Number(amount || 0),
                        });
                        // Update booking status to CONFIRMED by default
                        try {
                          await Bookings.updateStatus(
                            createdBookingId,
                            "CONFIRMED"
                          );
                        } catch (_) {
                          // non-fatal
                        }
                        toast.success("Payment successful");
                        navigate(`/bookings/${createdBookingId}`);
                      } catch (e) {
                        // errors already surfaced
                      }
                    }}
                    disabled={processPaymentMutation.isPending}
                  >
                    {processPaymentMutation.isPending
                      ? "Processing…"
                      : "Complete Payment"}
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => navigate(`/bookings/${createdBookingId}`)}
                  >
                    View booking
                  </Button>
                </div>
              )}
            </div>
          )}
        </Card>
      )}
    </div>
  );
}
