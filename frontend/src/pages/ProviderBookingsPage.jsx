import React, { useEffect, useMemo, useState } from "react";
import Navbar from "../components/layout/Navbar";
import { useAuth } from "../context/AuthContext";
import { bookingService } from "../services/bookingService";
import { serviceService } from "../services/serviceService";
import { userService } from "../services/userService";
import { toast } from "sonner";
import { Button } from "../components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card";
import { Calendar, CheckCircle2, XCircle, ListFilter } from "lucide-react";

const statuses = ["ALL", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"];

const ProviderBookingsPage = () => {
  const { user, isProvider } = useAuth();
  const providerId = user?.userId;
  const [loading, setLoading] = useState(true);
  const [bookings, setBookings] = useState([]);
  const [pageInfo, setPageInfo] = useState({
    pageNumber: 0,
    totalPages: 0,
    pageSize: 10,
  });
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [upcomingOnly, setUpcomingOnly] = useState(true);
  const [names, setNames] = useState({ services: {}, customers: {} });
  const [actionBusyId, setActionBusyId] = useState(null);

  const today = useMemo(() => new Date().toISOString().slice(0, 10), []);

  const filtered = useMemo(() => {
    return bookings.filter((b) => {
      if (upcomingOnly && b.bookingDate < today) return false;
      if (statusFilter !== "ALL" && b.bookingStatus !== statusFilter)
        return false;
      return true;
    });
  }, [bookings, upcomingOnly, statusFilter, today]);

  const load = async (page = 0) => {
    if (!providerId) return;
    setLoading(true);
    try {
      const res = await bookingService.getBookingsByProvider(
        providerId,
        page,
        50,
        "bookingDate",
        "asc"
      );
      const list = res?.content || [];
      setBookings(list);
      setPageInfo({
        pageNumber: res.pageNumber,
        totalPages: res.totalPages,
        pageSize: res.pageSize,
      });
      // Enrich names
      const uniqueSvc = Array.from(
        new Set(list.map((b) => b.serviceId).filter(Boolean))
      );
      const uniqueCust = Array.from(
        new Set(list.map((b) => b.customerId).filter(Boolean))
      );
      const [svcPairs, custPairs] = await Promise.all([
        Promise.all(
          uniqueSvc.map(async (sid) => {
            try {
              const s = await serviceService.getServiceById(sid);
              return [sid, s?.serviceName || `Service #${sid}`];
            } catch {
              return [sid, `Service #${sid}`];
            }
          })
        ),
        Promise.all(
          uniqueCust.map(async (cid) => {
            try {
              const u = await userService.getUserById(cid);
              const name = u?.userName || u?.customerName;
              return [cid, name || `Customer #${cid}`];
            } catch {
              return [cid, `Customer #${cid}`];
            }
          })
        ),
      ]);
      setNames({
        services: Object.fromEntries(svcPairs),
        customers: Object.fromEntries(custPairs),
      });
    } catch (e) {
      console.error(e);
      toast.error("Failed to load bookings");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isProvider()) load(0);
    else setLoading(false);
  }, [isProvider, providerId]);

  const act = async (bookingId, status) => {
    setActionBusyId(bookingId);
    try {
      await bookingService.updateBookingStatus(bookingId, status);
      toast.success(`Booking ${status.toLowerCase()}`);
      await load(pageInfo.pageNumber || 0);
    } catch (e) {
      console.error(e);
      toast.error("Failed to update booking");
    } finally {
      setActionBusyId(null);
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-6">
          <h1 className="text-3xl font-bold">Manage Bookings</h1>
          <p className="text-muted-foreground">
            View and manage bookings for your services
          </p>
        </div>

        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Filters</CardTitle>
            <CardDescription>
              Focus your list to what matters now
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3 md:flex-row md:items-center">
            <div className="flex items-center gap-2">
              <ListFilter className="h-4 w-4" />
              <select
                className="h-10 rounded-md border border-input bg-background px-3 text-sm"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                {statuses.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
            <label className="inline-flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={upcomingOnly}
                onChange={(e) => setUpcomingOnly(e.target.checked)}
              />
              Upcoming only
            </label>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Bookings</CardTitle>
            <CardDescription>
              Provider view • shows customer and service names
            </CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="py-16 text-center text-sm text-muted-foreground">
                Loading…
              </div>
            ) : filtered.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <Calendar className="mb-4 h-12 w-12 text-muted-foreground" />
                <h3 className="mb-2 text-lg font-semibold">No bookings</h3>
                <p className="text-muted-foreground">
                  Try changing the filters above
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {filtered.map((b) => (
                  <div
                    key={b.bookingId}
                    className="flex flex-col gap-2 rounded-lg border p-4 md:flex-row md:items-center md:justify-between"
                  >
                    <div className="text-sm">
                      <div className="font-medium">
                        {names.services[b.serviceId] ||
                          `Service #${b.serviceId}`}{" "}
                        • {b.bookingDate}
                      </div>
                      <div className="text-muted-foreground">
                        {b.bookingStartTime} - {b.bookingEndTime}
                      </div>
                      <div className="text-muted-foreground">
                        Customer:{" "}
                        {names.customers[b.customerId] ||
                          `Customer #${b.customerId}`}
                      </div>
                      <div className="text-muted-foreground">
                        Status: {b.bookingStatus}
                      </div>
                    </div>
                    <div className="flex gap-2">
                      {b.bookingStatus === "PENDING" && (
                        <Button
                          size="sm"
                          onClick={() => act(b.bookingId, "CONFIRMED")}
                          disabled={actionBusyId === b.bookingId}
                        >
                          <CheckCircle2 className="mr-1 h-4 w-4" /> Confirm
                        </Button>
                      )}
                      {b.bookingStatus !== "CANCELLED" &&
                        b.bookingStatus !== "COMPLETED" && (
                          <Button
                            size="sm"
                            variant="destructive"
                            onClick={() => act(b.bookingId, "CANCELLED")}
                            disabled={actionBusyId === b.bookingId}
                          >
                            <XCircle className="mr-1 h-4 w-4" /> Cancel
                          </Button>
                        )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  );
};

export default ProviderBookingsPage;
