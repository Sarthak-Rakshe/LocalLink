import React, { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Navbar from "../components/layout/Navbar";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card";
import { Button } from "../components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../components/ui/dialog";
import { Calendar, Clock, RefreshCcw, XCircle } from "lucide-react";
import { toast } from "sonner";
import { bookingService } from "../services/bookingService";
import { userService } from "../services/userService";
import { availabilityService } from "../services/availabilityService";
import { serviceService } from "../services/serviceService";
import { formatINR } from "../lib/currency";
import { Link } from "react-router-dom";

const BookingsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [bookings, setBookings] = useState([]);
  const [pageInfo, setPageInfo] = useState({
    pageNumber: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10,
  });
  const [listLoading, setListLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [pendingService, setPendingService] = useState(null); // details for pending booking's service (to get category)
  const [pendingProvider, setPendingProvider] = useState(null); // provider details for pending booking
  const [serviceNames, setServiceNames] = useState({}); // { [serviceId]: name }
  const [providerNames, setProviderNames] = useState({}); // { [providerId]: name }

  // Reschedule dialog state
  const [rescheduleOpen, setRescheduleOpen] = useState(false);
  const [activeBooking, setActiveBooking] = useState(null);
  const [newDate, setNewDate] = useState(() =>
    new Date().toISOString().slice(0, 10)
  );
  const [slots, setSlots] = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [reschedStart, setReschedStart] = useState("");
  const [reschedEnd, setReschedEnd] = useState("");

  const toHHMM = (t) => (t && t.length >= 5 ? t.slice(0, 5) : t || "");
  const toHHMMSS = (t) => (t && t.length === 5 ? `${t}:00` : t || "");
  const timeLTE = (a, b) => (a || "").localeCompare(b || "") <= 0;
  const timeLT = (a, b) => (a || "").localeCompare(b || "") < 0;

  const pendingBooking = useMemo(() => {
    const serviceId = searchParams.get("serviceId");
    const providerId = searchParams.get("providerId");
    const date = searchParams.get("date");
    const start = searchParams.get("start");
    const end = searchParams.get("end");
    if (serviceId && providerId && date && start && end) {
      return {
        serviceId: Number(serviceId),
        providerId: Number(providerId),
        date,
        start,
        end,
      };
    }
    return null;
  }, [searchParams]);

  // Pending booking customizable time
  const [pbStart, setPbStart] = useState("");
  const [pbEnd, setPbEnd] = useState("");
  useEffect(() => {
    if (pendingBooking) {
      setPbStart(toHHMM(pendingBooking.start));
      setPbEnd(toHHMM(pendingBooking.end));
    } else {
      setPbStart("");
      setPbEnd("");
    }
  }, [pendingBooking]);

  // We need serviceCategory for BookingDto (backend validation requires it),
  // so fetch the service details for the pending booking and reuse its category.
  useEffect(() => {
    const loadService = async () => {
      if (!pendingBooking?.serviceId) {
        setPendingService(null);
        return;
      }
      try {
        const svc = await serviceService.getServiceById(
          pendingBooking.serviceId
        );
        setPendingService(svc);
      } catch (e) {
        console.error(e);
        setPendingService(null);
      }
    };
    loadService();
  }, [pendingBooking]);

  useEffect(() => {
    const init = async () => {
      try {
        const me = await userService.getProfile();
        setProfile(me);
      } catch (e) {
        console.error(e);
        toast.error("Failed to load profile");
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  // Load provider details for pending booking to show friendly name
  useEffect(() => {
    const loadProvider = async () => {
      if (!pendingBooking?.providerId) {
        setPendingProvider(null);
        return;
      }
      try {
        const p = await userService.getProviderById(pendingBooking.providerId);
        setPendingProvider(p);
      } catch (e) {
        console.error(e);
        setPendingProvider(null);
      }
    };
    loadProvider();
  }, [pendingBooking]);

  const loadBookings = async (page = 0) => {
    if (!profile?.userId) return;
    setListLoading(true);
    try {
      const resp = await bookingService.getBookingsByCustomer(
        profile.userId,
        page,
        10,
        "createdAt",
        "desc"
      );
      const list = resp.content || [];
      setBookings(list);
      setPageInfo({
        pageNumber: resp.pageNumber,
        totalPages: resp.totalPages,
        totalElements: resp.totalElements,
        pageSize: resp.pageSize,
      });
      // Enrich names for friendlier UI
      try {
        const uniqueServiceIds = Array.from(
          new Set(list.map((b) => b.serviceId).filter(Boolean))
        );
        const uniqueProviderIds = Array.from(
          new Set(list.map((b) => b.serviceProviderId).filter(Boolean))
        );
        const [svcPairs, provPairs] = await Promise.all([
          Promise.all(
            uniqueServiceIds.map(async (sid) => {
              try {
                const s = await serviceService.getServiceById(sid);
                return [sid, s?.serviceName || `Service #${sid}`];
              } catch (err) {
                console.warn("Failed to load service name", sid, err);
                return [sid, `Service #${sid}`];
              }
            })
          ),
          Promise.all(
            uniqueProviderIds.map(async (pid) => {
              try {
                const p = await userService.getProviderById(pid);
                const name = p?.providerName || p?.userName;
                return [pid, name || `Provider #${pid}`];
              } catch (err) {
                console.warn("Failed to load provider name", pid, err);
                return [pid, `Provider #${pid}`];
              }
            })
          ),
        ]);
        setServiceNames(Object.fromEntries(svcPairs));
        setProviderNames(Object.fromEntries(provPairs));
      } catch (err) {
        console.warn("Name enrichment failed", err);
      }
    } catch (e) {
      console.error(e);
      toast.error("Failed to load bookings");
    } finally {
      setListLoading(false);
    }
  };

  useEffect(() => {
    if (profile?.userId) {
      loadBookings(0);
    }
  }, [profile]);

  const confirmBooking = async () => {
    if (!pendingBooking || !profile?.userId) return;
    setCreating(true);
    try {
      const start = pbStart || toHHMM(pendingBooking.start);
      const end = pbEnd || toHHMM(pendingBooking.end);
      if (!timeLT(start, end)) {
        toast.error("Start time must be before end time");
        setCreating(false);
        return;
      }
      // Ensure we have service category for backend validation
      let serviceCategory = pendingService?.serviceCategory;
      if (!serviceCategory) {
        try {
          const svc = await serviceService.getServiceById(
            pendingBooking.serviceId
          );
          serviceCategory = svc?.serviceCategory;
          setPendingService(svc);
        } catch (e) {
          console.error("Failed to load service for category", e);
        }
      }
      if (!serviceCategory) {
        toast.error("Unable to determine service category for booking.");
        setCreating(false);
        return;
      }
      // Availability verification (block if not available)
      try {
        const resp = await availabilityService.checkAvailability({
          serviceProviderId: pendingBooking.providerId,
          serviceId: pendingBooking.serviceId,
          date: pendingBooking.date,
          startTime: toHHMMSS(start),
          endTime: toHHMMSS(end),
        });
        if (resp?.status && resp.status !== "AVAILABLE") {
          toast.error(
            "Selected time is not available. Please pick a different time."
          );
          setCreating(false);
          return;
        }
      } catch (e) {
        // If check fails due to network or server, show message and stop to avoid bad booking
        console.error(e);
        toast.error("Could not verify availability. Try again.");
        setCreating(false);
        return;
      }
      const created = await bookingService.createBooking({
        customerId: profile.userId,
        serviceId: pendingBooking.serviceId,
        serviceProviderId: pendingBooking.providerId,
        serviceCategory,
        bookingDate: pendingBooking.date,
        bookingStartTime: toHHMMSS(start),
        bookingEndTime: toHHMMSS(end),
      });
      toast.success("Booking confirmed");
      // Clear query params
      setSearchParams({});
      await loadBookings(0);
    } catch (e) {
      console.error(e);
      const message =
        e?.response?.data?.message ||
        e?.response?.data ||
        "Failed to create booking";
      toast.error(String(message));
    } finally {
      setCreating(false);
    }
  };

  const startReschedule = (booking) => {
    setActiveBooking(booking);
    setNewDate(booking.bookingDate);
    setSelectedSlot(null);
    setSlots([]);
    setReschedStart("");
    setReschedEnd("");
    setRescheduleOpen(true);
  };

  const fetchSlots = async () => {
    if (!activeBooking) return;
    setSlotsLoading(true);
    try {
      const resp = await availabilityService.getAvailableSlots(
        activeBooking.serviceProviderId,
        activeBooking.serviceId,
        newDate
      );
      setSlots(resp?.availableSlots || []);
    } catch (e) {
      console.error(e);
      toast.error("Failed to fetch slots");
    } finally {
      setSlotsLoading(false);
    }
  };

  const submitReschedule = async () => {
    if (!activeBooking || !selectedSlot) return;
    try {
      const updated = await bookingService.rescheduleBooking(
        activeBooking.bookingId,
        {
          newBookingDate: newDate,
          newBookingStartTime: selectedSlot.startTime,
          newBookingEndTime: selectedSlot.endTime,
        }
      );
      toast.success("Booking rescheduled");
      setRescheduleOpen(false);
      setActiveBooking(null);
      await loadBookings(pageInfo.pageNumber || 0);
    } catch (e) {
      console.error(e);
      toast.error("Failed to reschedule booking");
    }
  };

  const submitRescheduleCustom = async (start, end) => {
    if (!activeBooking) return;
    try {
      await bookingService.rescheduleBooking(activeBooking.bookingId, {
        newBookingDate: newDate,
        newBookingStartTime: toHHMMSS(start),
        newBookingEndTime: toHHMMSS(end),
      });
      toast.success("Booking rescheduled");
      setRescheduleOpen(false);
      setActiveBooking(null);
      await loadBookings(pageInfo.pageNumber || 0);
    } catch (e) {
      console.error(e);
      toast.error("Failed to reschedule booking");
    }
  };

  const cancelBooking = async (bookingId) => {
    try {
      await bookingService.cancelBooking(bookingId);
      toast.success("Booking cancelled");
      await loadBookings(pageInfo.pageNumber || 0);
    } catch (e) {
      console.error(e);
      toast.error("Failed to cancel booking");
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="mb-2 text-3xl font-bold">My Bookings</h1>
          <p className="text-muted-foreground">View and manage your bookings</p>
        </div>

        {/* Pending booking confirmation */}
        {pendingBooking && (
          <Card className="mb-6 border-primary/40">
            <CardHeader>
              <CardTitle>Confirm your booking</CardTitle>
              <CardDescription>
                {pendingService?.serviceName
                  ? pendingService.serviceName
                  : `Service #${pendingBooking.serviceId}`}{" "}
                with{" "}
                {pendingProvider?.providerName || pendingProvider?.userName
                  ? pendingProvider?.providerName || pendingProvider?.userName
                  : `Provider #${pendingBooking.providerId}`}
                {pendingService?.serviceCategory && (
                  <> • Category: {pendingService.serviceCategory}</>
                )}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex flex-col gap-3 text-sm">
                <div className="inline-flex items-center">
                  <Calendar className="mr-2 h-4 w-4" /> {pendingBooking.date}
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="mb-1 block text-xs text-muted-foreground">
                      Start
                    </label>
                    <input
                      type="time"
                      value={pbStart}
                      onChange={(e) => setPbStart(e.target.value)}
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    />
                  </div>
                  <div>
                    <label className="mb-1 block text-xs text-muted-foreground">
                      End
                    </label>
                    <input
                      type="time"
                      value={pbEnd}
                      onChange={(e) => setPbEnd(e.target.value)}
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    />
                  </div>
                </div>
              </div>
              <div className="mt-4 flex gap-2">
                <Button onClick={confirmBooking} disabled={creating}>
                  {creating ? "Booking…" : "Confirm booking"}
                </Button>
                <Button
                  variant="outline"
                  onClick={() => setSearchParams({})}
                  disabled={creating}
                >
                  Cancel
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Bookings list */}
        <Card>
          <CardHeader>
            <CardTitle>Bookings</CardTitle>
            <CardDescription>Your scheduled appointments</CardDescription>
          </CardHeader>
          <CardContent>
            {listLoading ? (
              <div className="py-16 text-center text-sm text-muted-foreground">
                Loading…
              </div>
            ) : bookings.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <Calendar className="mb-4 h-12 w-12 text-muted-foreground" />
                <h3 className="mb-2 text-lg font-semibold">No bookings yet</h3>
                <p className="text-muted-foreground">
                  Your bookings will appear here once you make a reservation
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {bookings.map((b) => (
                  <div
                    key={b.bookingId}
                    className="flex flex-col gap-2 rounded-lg border p-4 md:flex-row md:items-center md:justify-between"
                  >
                    <div className="text-sm">
                      <div className="font-medium">Booking #{b.bookingId}</div>
                      <div className="text-muted-foreground">
                        {serviceNames[b.serviceId] || `Service #${b.serviceId}`}{" "}
                        with{" "}
                        {providerNames[b.serviceProviderId] ||
                          `Provider #${b.serviceProviderId}`}
                      </div>
                      <div className="text-muted-foreground">
                        {b.bookingDate} • {b.bookingStartTime} -{" "}
                        {b.bookingEndTime}
                      </div>
                      <div className="text-muted-foreground">
                        Status: {b.bookingStatus}
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => startReschedule(b)}
                      >
                        <RefreshCcw className="mr-1 h-4 w-4" /> Reschedule
                      </Button>
                      <Button
                        size="sm"
                        variant="destructive"
                        onClick={() => cancelBooking(b.bookingId)}
                      >
                        <XCircle className="mr-1 h-4 w-4" /> Cancel
                      </Button>
                      <Link to={`/services/${b.serviceId}`}>
                        <Button size="sm" variant="secondary">
                          View Service
                        </Button>
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Reschedule dialog */}
        <Dialog open={rescheduleOpen} onOpenChange={setRescheduleOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                Reschedule booking #{activeBooking?.bookingId}
              </DialogTitle>
            </DialogHeader>
            <div className="space-y-3">
              <div>
                <label className="mb-1 block text-sm font-medium">Date</label>
                <input
                  type="date"
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={newDate}
                  onChange={(e) => setNewDate(e.target.value)}
                />
              </div>
              <div className="flex items-center gap-2">
                <Button size="sm" onClick={fetchSlots} disabled={slotsLoading}>
                  {slotsLoading ? "Loading…" : "Find slots"}
                </Button>
              </div>
              {slots.length > 0 && (
                <div>
                  <div className="mb-2 text-sm font-medium">
                    Available slots
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {slots.map((s, idx) => {
                      const active =
                        selectedSlot &&
                        selectedSlot.startTime === s.startTime &&
                        selectedSlot.endTime === s.endTime;
                      return (
                        <Button
                          key={`${s.startTime}-${s.endTime}-${idx}`}
                          size="sm"
                          variant={active ? "default" : "outline"}
                          onClick={() => {
                            setSelectedSlot(s);
                            setReschedStart(toHHMM(s.startTime));
                            setReschedEnd(toHHMM(s.endTime));
                          }}
                        >
                          <Clock className="mr-1 h-4 w-4" /> {s.startTime} -{" "}
                          {s.endTime}
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
                            value={reschedStart}
                            onChange={(e) => setReschedStart(e.target.value)}
                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs text-muted-foreground">
                            End
                          </label>
                          <input
                            type="time"
                            value={reschedEnd}
                            onChange={(e) => setReschedEnd(e.target.value)}
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
            <DialogFooter>
              <Button
                onClick={async () => {
                  if (!selectedSlot) return;
                  const s = toHHMM(selectedSlot.startTime);
                  const e = toHHMM(selectedSlot.endTime);
                  const start = reschedStart || s;
                  const end = reschedEnd || e;
                  if (!timeLT(start, end)) {
                    toast.error("Start time must be before end time");
                    return;
                  }
                  if (
                    !(
                      timeLTE(s, start) &&
                      timeLTE(start, e) &&
                      timeLTE(s, end) &&
                      timeLTE(end, e)
                    )
                  ) {
                    toast.error("Selected time must be within the chosen slot");
                    return;
                  }
                  await submitRescheduleCustom(start, end);
                }}
                disabled={!selectedSlot}
              >
                Apply
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </main>
    </div>
  );
};

export default BookingsPage;
