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

  const scheduleQ = useQuery({
    queryKey: ["provider-schedule", providerId, todayISO()],
    queryFn: async () => Bookings.listByProviderOnDate(providerId, todayISO()),
    enabled: !!providerId,
  });

  const recentQ = useQuery({
    queryKey: ["provider-recent", providerId],
    queryFn: async () =>
      Bookings.listByProvider(providerId, {
        page: 0,
        size: 5,
        "sort-by": "date",
        "sort-dir": "desc",
      }),
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
    return Array.isArray(raw) ? raw : raw?.content ?? [];
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

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">
          Welcome{user?.username ? `, ${user.username}` : ""}
        </h1>
        <p className="text-zinc-600">
          Your schedule and availability at a glance.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <Card
          title="Quick actions"
          description="Jump back into the most common tasks"
        >
          <div className="flex flex-wrap gap-2">
            <Button
              as={Link}
              to="/bookings"
              variant="primary"
              leftIcon={<ListBulletIcon className="size-4" />}
            >
              View bookings
            </Button>
            <Button
              as={Link}
              to="/bookings/summary"
              variant="secondary"
              leftIcon={<CalendarDaysIcon className="size-4" />}
            >
              Booking summary
            </Button>
            <Button
              as={Link}
              to="/availability"
              variant="outline"
              leftIcon={<Cog6ToothIcon className="size-4" />}
            >
              Manage availability
            </Button>
          </div>
        </Card>

        <Card title="Availability" description="Rules and one-off exceptions">
          <div className="grid grid-cols-2 gap-3 text-sm text-zinc-700">
            <div className="rounded-lg border p-3">
              <div className="text-xs text-zinc-500">Rules</div>
              <div className="mt-1 text-xl font-semibold text-zinc-900">
                {rulesQ.isLoading ? "…" : rulesCount}
              </div>
            </div>
            <div className="rounded-lg border p-3">
              <div className="text-xs text-zinc-500">Exceptions</div>
              <div className="mt-1 text-xl font-semibold text-zinc-900">
                {exceptionsQ.isLoading ? "…" : exceptionsCount}
              </div>
            </div>
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
              {user?.userType ?? "PROVIDER"}
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
          <ul className="divide-y">
            {schedule.map((b) => (
              <li key={b.id ?? b.bookingId} className="py-3">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="font-medium text-zinc-900">
                      {b.customerName ?? b.customer?.name ?? "Customer"}
                    </div>
                    <div className="text-sm text-zinc-600">
                      {formatDateTime(b.date ?? b.startTime ?? b.createdAt)}
                    </div>
                  </div>
                  {b.status && <Badge className="mt-1">{b.status}</Badge>}
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
          <ul className="divide-y">
            {recent.map((b) => (
              <li key={b.id ?? b.bookingId} className="py-3">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="font-medium text-zinc-900">
                      Booking #{b.id ?? b.bookingId}
                    </div>
                    <div className="text-sm text-zinc-600">
                      {formatDateTime(b.date ?? b.startTime ?? b.createdAt)}
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
