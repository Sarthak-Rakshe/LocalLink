import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import Navbar from "../components/layout/Navbar";
import { serviceService } from "../services/serviceService";
import { userService } from "../services/userService";
import { availabilityService } from "../services/availabilityService";
import { toast } from "sonner";
import { formatINR } from "../lib/currency";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card";
import { Button } from "../components/ui/button";
import {
  MapPin,
  IndianRupee,
  CalendarDays,
  Clock,
  Phone,
  Mail,
  Star,
} from "lucide-react";

const ServiceDetailsPage = () => {
  const { serviceId } = useParams();
  const [service, setService] = useState(null);
  const [loading, setLoading] = useState(true);
  const [provider, setProvider] = useState(null);
  const [providerLoading, setProviderLoading] = useState(false);

  // Availability state
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10)); // YYYY-MM-DD
  const [slots, setSlots] = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [customStart, setCustomStart] = useState("");
  const [customEnd, setCustomEnd] = useState("");

  const toHHMM = (t) => (t && t.length >= 5 ? t.slice(0, 5) : t || "");
  const toHHMMSS = (t) => (t && t.length === 5 ? `${t}:00` : t || "");
  const timeLTE = (a, b) => (a || "").localeCompare(b || "") <= 0; // works for HH:MM strings
  const timeLT = (a, b) => (a || "").localeCompare(b || "") < 0;

  useEffect(() => {
    const load = async () => {
      try {
        const data = await serviceService.getServiceById(serviceId);
        setService(data);
      } catch (e) {
        console.error(e);
        toast.error("Failed to load service details");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [serviceId]);

  // Load provider once service is available
  useEffect(() => {
    const loadProvider = async () => {
      if (!service?.serviceProviderId) return;
      setProviderLoading(true);
      try {
        // Use new UserService provider endpoint; pass providerId
        const p = await userService.getProviderById(service.serviceProviderId);
        setProvider(p);
      } catch (e) {
        console.error(e);
        toast.error("Failed to load provider details");
      } finally {
        setProviderLoading(false);
      }
    };
    loadProvider();
  }, [service]);

  const fetchSlots = async () => {
    if (!service?.serviceId || !service?.serviceProviderId) return;
    setSlotsLoading(true);
    setSelectedSlot(null);
    setCustomStart("");
    setCustomEnd("");
    try {
      const resp = await availabilityService.getAvailableSlots(
        service.serviceProviderId,
        service.serviceId,
        date
      );
      setSlots(resp?.availableSlots || []);
      if (
        resp &&
        resp.isDayAvailable &&
        (resp.availableSlots || []).length === 0
      ) {
        toast.info(
          "Provider is available that day but no discrete slots are defined."
        );
      }
      if (!resp?.isDayAvailable) {
        toast.info("Provider is not available on selected day.");
      }
    } catch (e) {
      console.error(e);
      toast.error("Failed to fetch availability");
    } finally {
      setSlotsLoading(false);
    }
  };

  const proceedToBooking = async () => {
    if (!selectedSlot) return;
    const slotStart = toHHMM(selectedSlot.startTime);
    const slotEnd = toHHMM(selectedSlot.endTime);
    const start = customStart || slotStart;
    const end = customEnd || slotEnd;

    // Validate within slot and start < end
    if (!timeLT(start, end)) {
      toast.error("Start time must be before end time");
      return;
    }
    if (
      !timeLTE(slotStart, start) ||
      !timeLTE(start, slotEnd) ||
      !timeLTE(slotStart, end) ||
      !timeLTE(end, slotEnd)
    ) {
      toast.error("Selected time must be within the chosen slot");
      return;
    }

    // Optional: verify availability before navigating
    try {
      const resp = await availabilityService.checkAvailability({
        serviceProviderId: service.serviceProviderId,
        serviceId: service.serviceId,
        date,
        startTime: toHHMMSS(start),
        endTime: toHHMMSS(end),
      });
      if (resp?.status && resp.status !== "AVAILABLE") {
        toast.error("Selected time is no longer available");
        return;
      }
    } catch (e) {
      // If check fails, still allow navigation, backend will enforce
      console.warn("Availability check failed", e);
    }

    const params = new URLSearchParams({
      serviceId: String(service.serviceId),
      providerId: String(service.serviceProviderId),
      date,
      start: toHHMMSS(start),
      end: toHHMMSS(end),
    });
    window.location.href = `/bookings?${params.toString()}`;
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          </div>
        ) : !service ? (
          <div className="rounded-lg border bg-card p-8 text-center text-sm text-muted-foreground">
            Service not found.
          </div>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="text-2xl">{service.serviceName}</CardTitle>
              <CardDescription>{service.serviceCategory}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-6 md:grid-cols-3">
                <div className="md:col-span-2 space-y-4">
                  <div>
                    <h3 className="mb-1 font-semibold">Description</h3>
                    <p className="text-sm text-muted-foreground">
                      {service.serviceDescription || "No description provided."}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="inline-flex items-center text-primary font-semibold">
                      <IndianRupee className="mr-1 h-4 w-4" />
                      {formatINR(service.servicePricePerHour)} / hour
                    </span>
                    <span className="inline-flex items-center text-sm text-muted-foreground">
                      <MapPin className="mr-1 h-4 w-4" />
                      {typeof service.latitude === "number" &&
                      typeof service.longitude === "number"
                        ? `${service.latitude}, ${service.longitude}`
                        : "Location not specified"}
                    </span>
                  </div>

                  {/* Provider details */}
                  <div className="mt-4 rounded-lg border p-4">
                    <h3 className="mb-2 font-semibold">Provider</h3>
                    {providerLoading ? (
                      <div className="h-4 w-40 animate-pulse rounded bg-muted" />
                    ) : provider ? (
                      <div className="grid gap-2 text-sm">
                        <div className="font-medium">
                          {provider.providerName || provider.userName}
                        </div>
                        <div className="flex items-center text-muted-foreground">
                          <Mail className="mr-2 h-4 w-4" />{" "}
                          {provider.providerEmail || provider.userEmail}
                        </div>
                        <div className="flex items-center text-muted-foreground">
                          <Phone className="mr-2 h-4 w-4" />{" "}
                          {provider.providerContact ||
                            provider.userContact ||
                            "—"}
                        </div>
                        {(provider.providerAddress || provider.userAddress) && (
                          <div className="flex items-start text-muted-foreground">
                            <MapPin className="mr-2 mt-0.5 h-4 w-4" />
                            <span>
                              {provider.providerAddress || provider.userAddress}
                            </span>
                          </div>
                        )}
                        {(provider.providerReviewAggregateResponse ||
                          service.reviewAggregate) && (
                          <div className="flex items-center">
                            <Star className="mr-1 h-4 w-4 fill-yellow-400 text-yellow-400" />
                            <span className="text-sm">
                              {(() => {
                                const agg =
                                  provider.providerReviewAggregateResponse;
                                if (
                                  agg &&
                                  typeof agg.averageRating === "number"
                                ) {
                                  return agg.averageRating.toFixed(1);
                                }
                                const sAgg = service.reviewAggregate;
                                if (
                                  sAgg &&
                                  typeof sAgg.averageRating === "number"
                                ) {
                                  return sAgg.averageRating.toFixed(1);
                                }
                                return "N/A";
                              })()}{" "}
                              (
                              {(() => {
                                const agg =
                                  provider.providerReviewAggregateResponse;
                                if (
                                  agg &&
                                  typeof agg.totalReviews === "number"
                                ) {
                                  return agg.totalReviews;
                                }
                                const sAgg = service.reviewAggregate;
                                return (
                                  (sAgg &&
                                    (sAgg.reviewCount || sAgg.totalReviews)) ||
                                  0
                                );
                              })()}{" "}
                              reviews)
                            </span>
                          </div>
                        )}
                      </div>
                    ) : (
                      <p className="text-sm text-muted-foreground">
                        Provider details not available.
                      </p>
                    )}
                  </div>
                </div>
                {/* Availability + actions */}
                <div className="space-y-4">
                  <div className="rounded-lg border p-4">
                    <h3 className="mb-3 flex items-center gap-2 font-semibold">
                      <CalendarDays className="h-4 w-4" /> Check Availability
                    </h3>
                    <div className="space-y-3">
                      <div className="flex items-center gap-2">
                        <input
                          type="date"
                          value={date}
                          onChange={(e) => setDate(e.target.value)}
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                        />
                        <Button
                          size="sm"
                          onClick={fetchSlots}
                          disabled={slotsLoading}
                        >
                          {slotsLoading ? "Loading..." : "Find Slots"}
                        </Button>
                      </div>

                      {slots.length > 0 && (
                        <div>
                          <div className="mb-2 text-sm font-medium">
                            Available slots
                          </div>
                          <div className="flex flex-wrap gap-2">
                            {slots.map((s, idx) => {
                              const label = `${s.startTime} - ${s.endTime}`;
                              const active =
                                selectedSlot &&
                                selectedSlot.startTime === s.startTime &&
                                selectedSlot.endTime === s.endTime;
                              return (
                                <Button
                                  key={`${s.startTime}-${s.endTime}-${idx}`}
                                  variant={active ? "default" : "outline"}
                                  size="sm"
                                  onClick={() => {
                                    setSelectedSlot(s);
                                    setCustomStart(toHHMM(s.startTime));
                                    setCustomEnd(toHHMM(s.endTime));
                                  }}
                                  className="whitespace-nowrap"
                                >
                                  <Clock className="mr-1 h-4 w-4" /> {label}
                                </Button>
                              );
                            })}
                          </div>
                          {selectedSlot && (
                            <div className="mt-4 space-y-2">
                              <div className="text-sm font-medium">
                                Customize time within the selected slot
                              </div>
                              <div className="grid grid-cols-2 gap-2">
                                <div>
                                  <label className="mb-1 block text-xs text-muted-foreground">
                                    Start
                                  </label>
                                  <input
                                    type="time"
                                    min={toHHMM(selectedSlot.startTime)}
                                    max={toHHMM(selectedSlot.endTime)}
                                    value={customStart}
                                    onChange={(e) =>
                                      setCustomStart(e.target.value)
                                    }
                                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                  />
                                </div>
                                <div>
                                  <label className="mb-1 block text-xs text-muted-foreground">
                                    End
                                  </label>
                                  <input
                                    type="time"
                                    min={toHHMM(selectedSlot.startTime)}
                                    max={toHHMM(selectedSlot.endTime)}
                                    value={customEnd}
                                    onChange={(e) =>
                                      setCustomEnd(e.target.value)
                                    }
                                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                  />
                                </div>
                              </div>
                              <div className="text-xs text-muted-foreground">
                                Slot bounds: {toHHMM(selectedSlot.startTime)} -{" "}
                                {toHHMM(selectedSlot.endTime)}
                              </div>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </div>

                  <Button
                    className="w-full"
                    onClick={proceedToBooking}
                    disabled={!selectedSlot}
                  >
                    {selectedSlot
                      ? "Proceed to Booking"
                      : "Select a slot to book"}
                  </Button>
                  <Link to="/services">
                    <Button variant="outline" className="w-full">
                      Back to Services
                    </Button>
                  </Link>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </main>
    </div>
  );
};

export default ServiceDetailsPage;
