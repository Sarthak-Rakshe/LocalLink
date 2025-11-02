import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../../context/AuthContext.jsx";
import { Bookings } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Skeleton from "../../components/ui/Skeleton.jsx";
import { Link } from "react-router-dom";

function formatDateTime(d) {
  try {
    const dt = new Date(d);
    return dt.toLocaleString();
  } catch {
    return String(d);
  }
}

export default function CustomerDashboard() {
  const { user } = useAuth();
  const userId = user?.id ?? user?.userId;

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["customer-upcoming", userId],
    queryFn: async () =>
      Bookings.listByCustomer(userId, {
        page: 0,
        size: 5,
        "sort-by": "date",
        "sort-dir": "asc",
      }),
    enabled: !!userId,
  });

  const upcoming = Array.isArray(data?.content) ? data.content : data ?? [];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">
          Welcome{user?.username ? `, ${user.username}` : ""}
        </h1>
        <p className="text-zinc-600 mt-1">
          Here’s a quick look at your upcoming bookings.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <Card title="Quick actions">
          <div className="flex flex-wrap gap-2">
            <Button as={Link} to="/bookings" variant="primary">
              View all bookings
            </Button>
            <Button as={Link} to="/bookings/summary" variant="secondary">
              Booking summary
            </Button>
            <Button as={Link} to="/payments" variant="outline">
              Payments
            </Button>
          </div>
        </Card>

        <Card title="Account">
          <div className="text-sm text-zinc-600">
            <div>
              <span className="text-zinc-500">Role:</span>{" "}
              {user?.userRole ?? "USER"}
            </div>
            <div>
              <span className="text-zinc-500">Type:</span>{" "}
              {user?.userType ?? "CUSTOMER"}
            </div>
          </div>
        </Card>

        <Card title="Tips">
          <p className="text-sm text-zinc-600">
            You can manage bookings and pay securely via the Payments section.
          </p>
        </Card>
      </div>

      <Card title="Upcoming bookings">
        {isLoading && (
          <div className="space-y-2">
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-16 w-full" />
            <Skeleton className="h-16 w-full" />
          </div>
        )}
        {isError && (
          <div className="text-sm text-red-600">
            {error?.response?.data?.message || "Failed to load bookings."}
          </div>
        )}
        {!isLoading && !isError && upcoming.length === 0 && (
          <EmptyState
            title="No upcoming bookings"
            message="You haven’t booked any services yet."
          />
        )}

        <ul className="divide-y">
          {upcoming.map((b) => (
            <li key={b.id ?? b.bookingId} className="py-3">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <div className="font-medium text-zinc-900">
                    Booking #{b.id ?? b.bookingId}
                  </div>
                  <div className="text-sm text-zinc-600">
                    {formatDateTime(b.date ?? b.startTime ?? b.createdAt)}
                  </div>
                  {b.status && <Badge className="mt-1">{b.status}</Badge>}
                </div>
                <div className="text-sm text-zinc-600 text-right">
                  <div>
                    {b.providerName ?? b.serviceProviderName ?? "Provider"}
                  </div>
                  <div className="text-zinc-500">
                    {b.serviceName ?? "Service"}
                  </div>
                </div>
              </div>
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
}
