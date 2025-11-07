import { useEffect, useMemo, useRef, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../../context/AuthContext.jsx";
import { Bookings } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import Alert from "../../components/ui/Alert.jsx";
import Badge from "../../components/ui/Badge.jsx";
import Button from "../../components/ui/Button.jsx";
import Skeleton from "../../components/ui/Skeleton.jsx";
import { Link } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import toast from "react-hot-toast";
import PageHeader from "../../components/ui/PageHeader.jsx";
import { Input } from "../../components/ui/Input.jsx";
import {
  CalendarDaysIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
} from "@heroicons/react/24/outline";

function formatDateTime(d) {
  try {
    return new Date(d).toLocaleString();
  } catch {
    return String(d);
  }
}

function statusColor(s) {
  const v = String(s || "").toUpperCase();
  if (v.includes("COMPLETE")) return "green";
  if (v.includes("CONFIRM")) return "blue";
  if (v.includes("PEND")) return "amber";
  if (v.includes("CANCEL")) return "red";
  if (v.includes("RESCHED")) return "amber";
  return "gray";
}

function capitalizeWords(str) {
  return String(str || "")
    .toLowerCase()
    .replace(/\b\w/g, (m) => m.toUpperCase());
}

function toStartEndDates(b) {
  const dateStr = b?.bookingDate || b?.date;
  const startRaw = b?.bookingStartTime || b?.startTime || b?.slot?.startTime;
  const endRaw = b?.bookingEndTime || b?.endTime || b?.slot?.endTime;
  if (!dateStr || (!startRaw && !endRaw)) return { start: null, end: null };
  const toDate = (raw) => {
    if (!raw) return null;
    const hhmm = String(raw).slice(0, 5);
    const ts = `${dateStr}T${hhmm}:00`;
    const d = new Date(ts);
    return isNaN(d.getTime()) ? null : d;
  };
  return { start: toDate(startRaw), end: toDate(endRaw) };
}

function formatDateRangeLabel(b) {
  const { start, end } = toStartEndDates(b);
  if (!start && !end) {
    // fallback to any available timestamp-ish field
    return formatDateTime(b?.date ?? b?.startTime ?? b?.createdAt);
  }
  const dateLabel = (start || end).toLocaleDateString(undefined, {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
  });
  const fmtTime = (d) =>
    d?.toLocaleTimeString(undefined, { hour: "numeric", minute: "2-digit" });
  const startTime = fmtTime(start);
  const endTime = fmtTime(end);
  if (startTime && endTime) return `${dateLabel} • ${startTime}–${endTime}`;
  return `${dateLabel} • ${startTime || endTime}`;
}

function joinName(obj) {
  if (!obj) return null;
  const first = obj.firstName || obj.givenName || obj.firstname;
  const last = obj.lastName || obj.surname || obj.lastname;
  const full = obj.fullName || obj.name;
  if (full && String(full).trim()) return String(full);
  if ((first || last) && String(`${first || ""} ${last || ""}`).trim())
    return String(`${first || ""} ${last || ""}`).trim();
  return obj.username || obj.email || null;
}

function serviceLabel(b) {
  const id = b?.serviceId ?? b?.service?.serviceId ?? b?.service?.id;
  return (
    b?.serviceName ||
    b?.serviceTitle ||
    b?.service?.serviceName ||
    b?.service?.name ||
    b?.service?.title ||
    (id != null ? `Service #${id}` : "Service")
  );
}

function providerLabel(b) {
  const id =
    b?.serviceProviderId ??
    b?.providerId ??
    b?.provider?.id ??
    b?.serviceProvider?.serviceProviderId ??
    b?.serviceProvider?.id;
  return (
    b?.providerName ||
    b?.serviceProviderName ||
    b?.serviceProvider?.serviceProviderName ||
    b?.providerFullName ||
    b?.serviceProviderFullName ||
    joinName(b?.provider) ||
    joinName(b?.serviceProvider) ||
    b?.provider?.name ||
    b?.serviceProvider?.name ||
    (id != null ? `#${id}` : "—")
  );
}

function customerLabel(b) {
  const id = b?.customerId ?? b?.customer?.customerId ?? b?.customer?.id;
  return (
    b?.customerName ||
    b?.customer?.customerName ||
    b?.customerFullName ||
    joinName(b?.customer) ||
    b?.customer?.name ||
    (id != null ? `#${id}` : "—")
  );
}

export default function BookingsList() {
  const { user } = useAuth();
  const [page, setPage] = useState(0);
  // lightweight, client-side view controls (do not change server logic)
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const pageSize = 10;
  const id = user?.id ?? user?.userId;
  const isProvider = user?.userType === "PROVIDER";
  const promptedRef = useRef(new Set());

  const query = useQuery({
    queryKey: ["bookings-list", id, isProvider, page, pageSize],
    queryFn: async () => {
      const params = {
        page,
        size: pageSize,
        "sort-by": "createdAt",
        "sort-dir": "desc",
      };
      const filter = isProvider
        ? { serviceProviderId: id }
        : { customerId: id };
      return Bookings.getList(filter, params);
    },
    enabled: !!id,
  });

  const { items, total, last } = useMemo(() => {
    const raw = query.data ?? {};
    if (Array.isArray(raw))
      return { items: raw, total: raw.length, last: true };
    return {
      items: raw.content ?? [],
      total: raw.totalElements ?? 0,
      last: raw.last ?? true,
    };
  }, [query.data]);

  const grouped = useMemo(() => {
    const now = new Date();
    const upcoming = [];
    const past = [];
    for (const b of items) {
      const { end } = toStartEndDates(b);
      if (end && end < now) past.push(b);
      else upcoming.push(b);
    }
    return { upcoming, past };
  }, [items]);

  const statusCounts = useMemo(() => {
    const counts = { ALL: items.length };
    for (const b of items) {
      const s = String(b.status ?? b.bookingStatus ?? "").toUpperCase();
      counts[s] = (counts[s] || 0) + 1;
    }
    return counts;
  }, [items]);

  const completionRate = useMemo(() => {
    const completed = items.filter((b) =>
      String(b.status ?? b.bookingStatus ?? "")
        .toUpperCase()
        .includes("COMPLETED")
    ).length;
    return items.length ? Math.round((completed / items.length) * 100) : 0;
  }, [items]);

  // Derived, view-only filtering for search/status pills
  const filtered = useMemo(() => {
    const f = items.filter((b) => {
      // query match
      const okQuery = (() => {
        if (!search) return true;
        const q = search.toLowerCase();
        const hay = [serviceLabel(b), providerLabel(b), customerLabel(b)]
          .filter(Boolean)
          .join(" ")
          .toLowerCase();
        return hay.includes(q);
      })();
      // status pill match
      const okStatus = (() => {
        if (statusFilter === "ALL") return true;
        return String(b.status ?? b.bookingStatus ?? "")
          .toUpperCase()
          .includes(statusFilter);
      })();
      return okQuery && okStatus;
    });
    const now = new Date();
    return {
      upcoming: f.filter((b) => {
        const { end } = toStartEndDates(b);
        return !(end && end < now);
      }),
      past: f.filter((b) => {
        const { end } = toStartEndDates(b);
        return end && end < now;
      }),
    };
  }, [items, search, statusFilter]);

  const updateStatusMutation = useMutation({
    mutationFn: ({ bookingId, status }) =>
      Bookings.updateStatus(bookingId, status),
    onSuccess: (_, variables) => {
      toast.success(
        `Booking #${variables.bookingId} marked ${variables.status}`
      );
      query.refetch();
    },
    onError: (e) => {
      toast.error(e?.response?.data?.message || "Failed to update status");
    },
  });

  function toEndDate(b) {
    return toStartEndDates(b).end;
  }

  useEffect(() => {
    // Prompt once per eligible booking
    if (!items || items.length === 0) return;
    (async () => {
      for (const b of items) {
        const bookingId = b.id ?? b.bookingId;
        if (!bookingId) continue;
        if (promptedRef.current.has(bookingId)) continue;
        const status = String(b.status ?? b.bookingStatus ?? "").toUpperCase();
        if (status !== "CONFIRMED") continue;
        const endDate = toEndDate(b);
        if (!endDate) continue;
        const now = new Date();
        if (now <= endDate) continue;

        // Mark as prompted immediately to avoid re-asks on fast re-renders
        promptedRef.current.add(bookingId);
        const occurred = window.confirm(
          `Booking #${bookingId} ended on ${endDate.toLocaleString()}. Did this booking occur?\nPress OK for Yes (mark Completed). Press Cancel for No (mark Cancelled).`
        );
        try {
          if (occurred) {
            await updateStatusMutation.mutateAsync({
              bookingId,
              status: "COMPLETED",
            });
          } else {
            await updateStatusMutation.mutateAsync({
              bookingId,
              status: "CANCELLED",
            });
          }
        } catch {
          // errors are handled in onError; allow loop to continue
          void 0;
        }
      }
    })();
    // We intentionally do not include mutation/query in deps to avoid loops
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [items]);

  return (
    <div className="space-y-4">
      <PageHeader
        title="Bookings"
        actions={
          user?.userType === "CUSTOMER" ? (
            <Button as={Link} to="/bookings/create">
              New booking
            </Button>
          ) : null
        }
      />

      {/* Top summaries inspired by the referenced dashboard */}
      <div className="grid gap-4 sm:grid-cols-3">
        <Card className="p-0">
          <div className="flex items-center gap-3 p-5">
            <div className="flex size-10 items-center justify-center rounded-lg bg-indigo-50 text-indigo-600">
              <CalendarDaysIcon className="size-5" />
            </div>
            <div>
              <p className="text-xs text-zinc-500">Upcoming</p>
              <p className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
                {grouped.upcoming.length}
              </p>
            </div>
          </div>
        </Card>
        <Card className="p-0">
          <div className="flex items-center gap-3 p-5">
            <div className="flex size-10 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600">
              <CheckCircleIcon className="size-5" />
            </div>
            <div>
              <p className="text-xs text-zinc-500">Completion rate</p>
              <p className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
                {completionRate}%
              </p>
            </div>
          </div>
        </Card>
        <Card className="p-0">
          <div className="flex items-center gap-3 p-5">
            <div className="flex size-10 items-center justify-center rounded-lg bg-amber-50 text-amber-600">
              <ExclamationTriangleIcon className="size-5" />
            </div>
            <div>
              <p className="text-xs text-zinc-500">Pending</p>
              <p className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
                {statusCounts.PENDING || statusCounts["PEND"] || 0}
              </p>
            </div>
          </div>
        </Card>
      </div>

      <Card>
        {/* Search and status chips */}
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="sm:w-80">
            <Input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by service, provider or customer"
            />
          </div>
          <div className="flex flex-wrap items-center gap-1.5">
            {[
              { key: "ALL", label: `All (${statusCounts.ALL || 0})` },
              {
                key: "CONFIRMED",
                label: `Confirmed (${statusCounts.CONFIRMED || 0})`,
              },
              {
                key: "COMPLETED",
                label: `Completed (${statusCounts.COMPLETED || 0})`,
              },
              {
                key: "PENDING",
                label: `Pending (${statusCounts.PENDING || 0})`,
              },
              {
                key: "CANCELLED",
                label: `Cancelled (${statusCounts.CANCELLED || 0})`,
              },
            ].map((t) => (
              <button
                key={t.key}
                onClick={() => setStatusFilter(t.key)}
                className={
                  "rounded-full border px-3 py-1 text-xs transition " +
                  (statusFilter === t.key
                    ? "border-indigo-500 bg-indigo-50 text-indigo-700 dark:border-indigo-400 dark:bg-indigo-500/10 dark:text-indigo-300"
                    : "border-zinc-300 bg-white text-zinc-700 hover:bg-zinc-50 dark:border-zinc-700 dark:bg-transparent dark:text-zinc-300 dark:hover:bg-white/5")
                }
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>

        {query.isLoading && (
          <div className="space-y-2">
            <Skeleton className="h-4 w-24" />
            <Skeleton className="h-16 w-full" />
            <Skeleton className="h-16 w-full" />
            <Skeleton className="h-16 w-full" />
          </div>
        )}
        {query.isError && (
          <Alert variant="error">
            {query.error?.response?.data?.message || "Failed to load bookings."}
          </Alert>
        )}
        {!query.isLoading && !query.isError && items.length === 0 && (
          <EmptyState
            title="No bookings"
            message={
              isProvider
                ? "You have no bookings yet."
                : "Create your first booking."
            }
          />
        )}

        {!query.isLoading && !query.isError && items.length > 0 && (
          <div className="space-y-6">
            {filtered.upcoming.length > 0 && (
              <section>
                <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-500">
                  Upcoming
                </h3>
                <ul className="divide-y divide-zinc-200 rounded-lg bg-white dark:divide-zinc-800 dark:bg-zinc-900">
                  {filtered.upcoming.map((b) => {
                    const id = b.id ?? b.bookingId;
                    const status = b.status ?? b.bookingStatus;
                    return (
                      <li key={id} className="px-4 py-3">
                        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                          <div className="min-w-0">
                            <Link
                              to={`/bookings/${id}`}
                              className="block truncate font-medium text-zinc-900 hover:underline dark:text-zinc-100"
                            >
                              {serviceLabel(b)}
                            </Link>
                            <div className="mt-0.5 text-sm text-zinc-600 dark:text-zinc-400">
                              {formatDateRangeLabel(b)}
                            </div>
                            <div className="mt-0.5 text-sm text-zinc-500 dark:text-zinc-400">
                              Provider: {providerLabel(b)}
                            </div>
                            <div className="mt-0.5 text-sm text-zinc-500 dark:text-zinc-400">
                              Customer: {customerLabel(b)}
                            </div>
                          </div>
                          <div className="shrink-0 text-right">
                            {status && (
                              <Badge
                                color={statusColor(status)}
                                className="mt-0.5"
                              >
                                {capitalizeWords(status)}
                              </Badge>
                            )}
                            <div className="mt-2">
                              <Button
                                as={Link}
                                to={`/bookings/${id}`}
                                size="sm"
                                variant="outline"
                              >
                                View
                              </Button>
                            </div>
                          </div>
                        </div>
                      </li>
                    );
                  })}
                </ul>
              </section>
            )}

            {filtered.past.length > 0 && (
              <section>
                <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-500">
                  Past
                </h3>
                <ul className="divide-y divide-zinc-200 rounded-lg bg-white dark:divide-zinc-800 dark:bg-zinc-900">
                  {filtered.past.map((b) => {
                    const id = b.id ?? b.bookingId;
                    const status = b.status ?? b.bookingStatus;
                    const isCompleted = String(status || "")
                      .toUpperCase()
                      .includes("COMPLETED");
                    return (
                      <li key={id} className="px-4 py-3">
                        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                          <div className="min-w-0">
                            <Link
                              to={`/bookings/${id}`}
                              className="block truncate font-medium text-zinc-900 hover:underline dark:text-zinc-100"
                            >
                              {serviceLabel(b)}
                            </Link>
                            <div className="mt-0.5 text-sm text-zinc-600 dark:text-zinc-400">
                              {formatDateRangeLabel(b)}
                            </div>
                            <div className="mt-0.5 text-sm text-zinc-500 dark:text-zinc-400">
                              Provider: {providerLabel(b)}
                            </div>
                            <div className="mt-0.5 text-sm text-zinc-500 dark:text-zinc-400">
                              Customer: {customerLabel(b)}
                            </div>
                          </div>
                          <div className="shrink-0 text-right">
                            {status && (
                              <Badge
                                color={statusColor(status)}
                                className="mt-0.5"
                              >
                                {capitalizeWords(status)}
                              </Badge>
                            )}
                            {user?.userType === "CUSTOMER" && isCompleted && (
                              <div className="mt-2">
                                <Button
                                  as={Link}
                                  to={`/reviews?serviceId=${
                                    b.serviceId ?? ""
                                  }&providerId=${
                                    b.serviceProviderId ?? b.providerId ?? ""
                                  }`}
                                  size="sm"
                                  variant="outline"
                                >
                                  Add review
                                </Button>
                              </div>
                            )}
                          </div>
                        </div>
                      </li>
                    );
                  })}
                </ul>
              </section>
            )}
          </div>
        )}

        {total > pageSize && (
          <div className="mt-3 flex items-center justify-between text-sm">
            <div className="text-zinc-600">
              Page {page + 1} • Showing {page * pageSize + 1}–
              {Math.min((page + 1) * pageSize, total)} of {total}
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                onClick={() => setPage((p) => (last ? p : p + 1))}
                disabled={last}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}
