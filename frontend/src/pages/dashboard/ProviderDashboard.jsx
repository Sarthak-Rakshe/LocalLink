import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../../context/AuthContext.jsx";
import { Bookings, Availability } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Skeleton from "../../components/ui/Skeleton.jsx";
import { Link } from "react-router-dom";
import {
  CalendarDaysIcon,
  ListBulletIcon,
  Cog6ToothIcon,
} from "@heroicons/react/24/outline";

function todayISO() {
  return new Date().toISOString().slice(0, 10);
}

function formatDateTime(d) {
  try {
    const dt = new Date(d);
    return dt.toLocaleString();
  } catch {
    return String(d);
  }
}

export default function ProviderDashboard() {
  const { user } = useAuth();
  const providerId = user?.id ?? user?.userId;
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

  const scheduleQ = useQuery({
    queryKey: ["provider-schedule", providerId, todayISO()],
    queryFn: async () => {
      const day = todayISO();
      const from = `${day}T00:00:00Z`;
      const to = `${day}T23:59:59Z`;
      return Bookings.getList(
        { serviceProviderId: providerId, dateFrom: from, dateTo: to },
        { page: 0, size: 10, "sort-by": "createdAt", "sort-dir": "asc" }
      );
    },
    enabled: !!providerId,
  });

  const recentQ = useQuery({
    queryKey: ["provider-recent", providerId],
    queryFn: async () =>
      Bookings.getList(
        { serviceProviderId: providerId },
        { page: 0, size: 5, "sort-by": "createdAt", "sort-dir": "desc" }
      ),
    enabled: !!providerId,
  });

  const rulesQ = useQuery({
    queryKey: ["provider-rules", providerId],
    queryFn: async () => Availability.getRulesForProvider(providerId),
    enabled: !!providerId,
  });

  const exceptionsQ = useQuery({
    queryKey: ["provider-exceptions", providerId],
    queryFn: async () => Availability.getExceptionsForProvider(providerId),
    enabled: !!providerId,
  });

  const schedule = useMemo(() => {
    const raw = scheduleQ.data ?? [];
    return Array.isArray(raw?.content)
      ? raw.content
      : Array.isArray(raw)
      ? raw
      : [];
  }, [scheduleQ.data]);

  const recent = useMemo(() => {
    const raw = recentQ.data ?? [];
    const arr = Array.isArray(raw?.content)
      ? raw.content
      : Array.isArray(raw)
      ? raw
      : [];
    // Try to surface pending first if status exists
    return arr.sort((a, b) => {
      const ap = a.status === "PENDING" ? 0 : 1;
      const bp = b.status === "PENDING" ? 0 : 1;
      if (ap !== bp) return ap - bp;
      return 0;
    });
  }, [recentQ.data]);

  const rulesCount = Array.isArray(rulesQ.data)
    ? rulesQ.data.length
    : rulesQ.data?.totalElements ?? 0;
  const exceptionsCount = Array.isArray(exceptionsQ.data)
    ? exceptionsQ.data.length
    : exceptionsQ.data?.totalElements ?? 0;

  function customerLabel(b) {
    return (
      b?.customer?.customerName ||
      b?.customerName ||
      (b?.customerId != null ? `Customer #${b.customerId}` : "Customer")
    );
  }

  function serviceLabel(b) {
    const id = b?.serviceId ?? b?.service?.serviceId ?? b?.service?.id;
    return (
      b?.service?.serviceName ||
      b?.serviceName ||
      (id != null ? `Service #${id}` : "Service")
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
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">Welcome, {name}</h1>
        <p className="text-zinc-600 dark:text-zinc-400">
          Your schedule and availability at a glance.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <Card
          title="Quick actions"
          description="Jump back into the most common tasks"
        >
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2">
            <Button
              as={Link}
              to="/bookings"
              variant="primary"
              leftIcon={<ListBulletIcon className="size-4" />}
              className="w-full h-10"
            >
              View bookings
            </Button>
            <Button
              as={Link}
              to="/bookings/summary"
              variant="secondary"
              leftIcon={<CalendarDaysIcon className="size-4" />}
              className="w-full h-10"
            >
              Booking summary
            </Button>
            <Button
              as={Link}
              to="/availability"
              variant="outline"
              leftIcon={<Cog6ToothIcon className="size-4" />}
              className="w-full h-10"
            >
              Manage availability
            </Button>
          </div>
        </Card>

        <Card title="Availability" description="Rules and one-off exceptions">
          <div className="grid grid-cols-2 gap-3 text-sm text-zinc-700 dark:text-zinc-300">
            <div className="rounded-lg border border-zinc-200 p-3 dark:border-zinc-800">
              <div className="text-xs text-zinc-500 dark:text-zinc-500">
                Rules
              </div>
              <div className="mt-1 text-xl font-semibold text-zinc-900 dark:text-zinc-100">
                {rulesQ.isLoading ? "…" : rulesCount}
              </div>
            </div>
            <div className="rounded-lg border border-zinc-200 p-3 dark:border-zinc-800">
              <div className="text-xs text-zinc-500 dark:text-zinc-500">
                Exceptions
              </div>
              <div className="mt-1 text-xl font-semibold text-zinc-900 dark:text-zinc-100">
                {exceptionsQ.isLoading ? "…" : exceptionsCount}
              </div>
            </div>
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <Card title="Today’s schedule">
          {scheduleQ.isLoading && (
            <div className="space-y-2">
              <Skeleton className="h-4 w-28" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </div>
          )}
          {scheduleQ.isError && (
            <div className="text-sm text-red-600">
              {scheduleQ.error?.response?.data?.message ||
                "Failed to load schedule."}
            </div>
          )}
          {!scheduleQ.isLoading &&
            !scheduleQ.isError &&
            schedule.length === 0 && (
              <EmptyState
                title="No bookings today"
                message="You don’t have any bookings for today."
              />
            )}
          <ul className="divide-y divide-zinc-200 dark:divide-zinc-800">
            {schedule.map((b) => (
              <li key={b.id ?? b.bookingId} className="py-3">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="font-medium text-zinc-900 dark:text-zinc-100">
                      {customerLabel(b)}
                    </div>
                    <div className="text-sm text-zinc-600 dark:text-zinc-400">
                      {whenLabel(b)}
                    </div>
                  </div>
                  {(b.bookingStatus ?? b.status) && (
                    <Badge className="mt-1">
                      {b.bookingStatus ?? b.status}
                    </Badge>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </Card>

        <Card title="Recent / pending requests">
          {recentQ.isLoading && (
            <div className="space-y-2">
              <Skeleton className="h-4 w-40" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </div>
          )}
          {recentQ.isError && (
            <div className="text-sm text-red-600">
              {recentQ.error?.response?.data?.message ||
                "Failed to load recent bookings."}
            </div>
          )}
          {!recentQ.isLoading && !recentQ.isError && recent.length === 0 && (
            <EmptyState
              title="No recent updates"
              message="New requests will show up here."
            />
          )}
          <ul className="divide-y divide-zinc-200 dark:divide-zinc-800">
            {recent.map((b) => (
              <li key={b.id ?? b.bookingId} className="py-3">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="font-medium text-zinc-900 dark:text-zinc-100">
                      {serviceLabel(b)}
                    </div>
                    <div className="text-sm text-zinc-600 dark:text-zinc-400">
                      {whenLabel(b)}
                    </div>
                  </div>
                  {b.status && (
                    <Badge color={b.status === "PENDING" ? "amber" : "gray"}>
                      {b.status}
                    </Badge>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </Card>
      </div>
    </div>
  );
}
