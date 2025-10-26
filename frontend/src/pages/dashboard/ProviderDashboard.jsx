import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../../components/ui/card";
import { Button } from "../../components/ui/button";
import { Package, Calendar, Clock, IndianRupee, User } from "lucide-react";
import { formatINR } from "../../lib/currency";
import { useAuth } from "../../context/AuthContext";
import { serviceService } from "../../services/serviceService";
import { bookingService } from "../../services/bookingService";
import { userService } from "../../services/userService";

const ProviderDashboard = ({ user }) => {
  const navigate = useNavigate();
  const auth = useAuth();
  const currentUser = user || auth?.user;
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    servicesCount: 0,
    pendingBookings: 0,
    totalReviews: 0,
    monthEarnings: 0,
  });
  const [upcoming, setUpcoming] = useState([]);
  const [svcNames, setSvcNames] = useState({});
  const [custNames, setCustNames] = useState({});

  useEffect(() => {
    const loadStats = async () => {
      try {
        if (!currentUser?.userId) return;
        // Fetch total services count for this provider using server-side pagination metadata
        const res = await serviceService.getServices(0, 1, "id", "asc", {
          userId: currentUser.userId,
        });
        const servicesCount =
          res.totalElements ??
          (Array.isArray(res.content) ? res.content.length : 0);
        setStats((s) => ({ ...s, servicesCount }));

        // Fetch bookings for this provider and compute upcoming (next few)
        const page = await bookingService.getBookingsByProvider(
          currentUser.userId,
          0,
          50,
          "bookingDate",
          "asc"
        );
        const list = page?.content || [];
        const todayStr = new Date().toISOString().slice(0, 10);
        const upcomingOnly = list
          .filter((b) => {
            // include future or today; exclude deleted/cancelled
            if (!b?.bookingDate) return false;
            if (["DELETED", "CANCELLED"].includes(b.bookingStatus))
              return false;
            return b.bookingDate >= todayStr;
          })
          .sort((a, b) => a.bookingDate.localeCompare(b.bookingDate))
          .slice(0, 5);
        setUpcoming(upcomingOnly);

        // Enrich names for display
        const uniqueSvc = Array.from(
          new Set(upcomingOnly.map((b) => b.serviceId).filter(Boolean))
        );
        const uniqueCust = Array.from(
          new Set(upcomingOnly.map((b) => b.customerId).filter(Boolean))
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
        setSvcNames(Object.fromEntries(svcPairs));
        setCustNames(Object.fromEntries(custPairs));
      } catch (e) {
        // Non-blocking: keep zero if error
        console.error("Failed to load provider stats", e);
      } finally {
        setLoading(false);
      }
    };
    loadStats();
  }, [currentUser?.userId]);

  const quickActions = [
    {
      title: "My Services",
      description: "Manage your service listings",
      icon: Package,
      action: () => navigate("/my-services"),
      color: "bg-primary",
    },
    {
      title: "Bookings",
      description: "View and manage bookings",
      icon: Calendar,
      action: () => navigate("/provider-bookings"),
      color: "bg-accent",
    },
    {
      title: "Availability",
      description: "Set your availability",
      icon: Clock,
      action: () => navigate("/availability"),
      color: "bg-success",
    },
    {
      title: "Earnings",
      description: "View your earnings",
      icon: IndianRupee,
      action: () => navigate("/earnings"),
      color: "bg-muted-foreground",
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Provider Dashboard</h1>
        <p className="text-muted-foreground">
          Manage your services and bookings
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Services
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? "—" : stats.servicesCount}
            </div>
            <p className="text-xs text-muted-foreground">Active listings</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Pending Bookings
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? "—" : stats.pendingBookings}
            </div>
            <p className="text-xs text-muted-foreground">
              Awaiting confirmation
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Reviews
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? "—" : stats.totalReviews}
            </div>
            <p className="text-xs text-muted-foreground">Average rating: N/A</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              This Month
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? "—" : formatINR(stats.monthEarnings)}
            </div>
            <p className="text-xs text-muted-foreground">Total earnings</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {quickActions.map((action) => (
          <Card
            key={action.title}
            className="cursor-pointer transition-all hover:scale-105 hover:shadow-lg"
            onClick={action.action}
          >
            <CardHeader className="pb-3">
              <div
                className={`mb-2 flex h-12 w-12 items-center justify-center rounded-lg ${action.color}`}
              >
                <action.icon className="h-6 w-6 text-white" />
              </div>
              <CardTitle className="text-lg">{action.title}</CardTitle>
              <CardDescription>{action.description}</CardDescription>
            </CardHeader>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Upcoming Bookings</CardTitle>
          <CardDescription>
            Your next appointments and who booked them
          </CardDescription>
        </CardHeader>
        <CardContent>
          {upcoming.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              No upcoming bookings
            </p>
          ) : (
            <div className="space-y-3">
              {upcoming.map((b) => (
                <div
                  key={b.bookingId}
                  className="flex items-center justify-between rounded-md border p-3 text-sm"
                >
                  <div>
                    <div className="font-medium">
                      {svcNames[b.serviceId] || `Service #${b.serviceId}`} ·{" "}
                      {b.bookingDate}
                    </div>
                    <div className="text-muted-foreground flex items-center gap-2">
                      <User className="h-4 w-4" />
                      <span>
                        Booked by{" "}
                        {custNames[b.customerId] || `Customer #${b.customerId}`}
                      </span>
                      <span>
                        • {b.bookingStartTime} - {b.bookingEndTime}
                      </span>
                      <span>• {b.bookingStatus}</span>
                    </div>
                  </div>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => navigate(`/provider-bookings`)}
                  >
                    Manage
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {stats.servicesCount === 0 && !loading && (
        <Card>
          <CardHeader>
            <CardTitle>Get Started</CardTitle>
            <CardDescription>
              Start offering your services to local customers
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-start space-x-4">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm font-medium text-primary-foreground">
                1
              </div>
              <div>
                <h4 className="font-medium">Create your first service</h4>
                <p className="text-sm text-muted-foreground">
                  Add details about the services you offer
                </p>
              </div>
            </div>
            <div className="flex items-start space-x-4">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm font-medium text-primary-foreground">
                2
              </div>
              <div>
                <h4 className="font-medium">Set your availability</h4>
                <p className="text-sm text-muted-foreground">
                  Let customers know when you're available
                </p>
              </div>
            </div>
            <div className="flex items-start space-x-4">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm font-medium text-primary-foreground">
                3
              </div>
              <div>
                <h4 className="font-medium">Start accepting bookings</h4>
                <p className="text-sm text-muted-foreground">
                  Connect with customers and grow your business
                </p>
              </div>
            </div>
            <Button onClick={() => navigate("/my-services")} className="w-full">
              Create Your First Service
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default ProviderDashboard;
