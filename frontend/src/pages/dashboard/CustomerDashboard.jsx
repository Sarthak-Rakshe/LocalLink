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
  const email = user?.email || user?.emailAddress || user?.mail || null;
  const contact =
    user?.contact ||
    user?.contactNumber ||
    user?.phone ||
    user?.phoneNumber ||
    user?.mobile ||
    user?.mobileNumber ||
    null;
  const userId = user?.id ?? user?.userId;
  const name = (() => {
    const first =
      user?.firstName || user?.givenName || user?.firstname || user?.first_name;
    const last =
      user?.lastName || user?.surname || user?.lastname || user?.last_name;
    const full =
      user?.fullName ||
      user?.displayName ||
      user?.name ||
      (first && last ? `${first} ${last}` : first || last);
    const handle =
      user?.username ||
      user?.userName ||
      user?.preferredUsername ||
      user?.preferred_username ||
      user?.nickname ||
      user?.sub;
    return full || handle || user?.email || "User";
  })();
  const secondary = (() => {
    const email = user?.email || user?.emailAddress || user?.mail;
    const handle =
      user?.username ||
      user?.userName ||
      user?.preferredUsername ||
      user?.preferred_username ||
      user?.nickname;
    return email || handle || null;
  })();
  const initials =
    (name || "U")
      .split(/\s+|[_\.]/)
      .filter(Boolean)
      .slice(0, 2)
      .map((s) => s[0]?.toUpperCase())
      .join("") || "U";

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["customer-upcoming", userId],
    queryFn: async () =>
      Bookings.getList(
        { customerId: userId },
        { page: 0, size: 5, "sort-by": "createdAt", "sort-dir": "asc" }
      ),
    enabled: !!userId,
  });

  const upcoming = Array.isArray(data?.content) ? data.content : data ?? [];

  function serviceLabel(b) {
    const id = b?.serviceId ?? b?.service?.serviceId ?? b?.service?.id;
    return (
      b?.service?.serviceName ||
      b?.serviceName ||
      (id != null ? `Service #${id}` : "Service")
    );
  }

  function providerLabel(b) {
    const id =
      b?.serviceProviderId ??
      b?.serviceProvider?.serviceProviderId ??
      b?.serviceProvider?.id;
    return (
      b?.serviceProvider?.serviceProviderName ||
      b?.serviceProviderName ||
      (id != null ? `#${id}` : "—")
    );
  }

  function whenLabel(b) {
    if (b?.bookingDate && (b?.bookingStartTime || b?.bookingEndTime)) {
      const start = (b.bookingStartTime || "").toString().slice(0, 5);
      const end = (b.bookingEndTime || "").toString().slice(0, 5);
      return `${b.bookingDate} ${start}${end ? ` - ${end}` : ""}`;
    }
    return formatDateTime(b?.createdAt ?? b?.date ?? b?.startTime);
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Welcome, {name}</h1>
        <p className="text-zinc-600 dark:text-zinc-400 mt-1">
          Here’s a quick look at your upcoming bookings.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <Card title="Quick actions">
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2">
            <Button
              as={Link}
              to="/bookings"
              variant="primary"
              className="w-full h-10"
            >
              View all bookings
            </Button>
            <Button
              as={Link}
              to="/bookings/summary"
              variant="secondary"
              className="w-full h-10"
            >
              Booking summary
            </Button>
            <Button
              as={Link}
              to="/payments"
              variant="outline"
              className="w-full h-10"
            >
              Payments
            </Button>
          </div>
        </Card>

        <Card title="Tips">
          <p className="text-sm text-zinc-600 dark:text-zinc-400">
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

        <ul className="divide-y divide-zinc-200 dark:divide-zinc-800">
          {upcoming.map((b) => (
            <li key={b.id ?? b.bookingId} className="py-3">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <div className="font-medium text-zinc-900 dark:text-zinc-100">
                    {serviceLabel(b)}
                  </div>
                  <div className="text-sm text-zinc-600 dark:text-zinc-400">
                    {whenLabel(b)}
                  </div>
                  {(b.bookingStatus ?? b.status) && (
                    <Badge className="mt-1">
                      {b.bookingStatus ?? b.status}
                    </Badge>
                  )}
                </div>
                <div className="text-sm text-zinc-600 dark:text-zinc-400 text-right">
                  <div>{providerLabel(b)}</div>
                  <div className="text-zinc-500 dark:text-zinc-500">
                    {serviceLabel(b)}
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
