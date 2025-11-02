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
  const id = b?.serviceId ?? b?.service?.id;
  return (
    b?.serviceName ||
    b?.serviceTitle ||
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
    b?.serviceProvider?.id;
  return (
    b?.providerName ||
    b?.serviceProviderName ||
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
  const id = b?.customerId ?? b?.customer?.id;
  return (
    b?.customerName ||
    b?.customerFullName ||
    joinName(b?.customer) ||
    b?.customer?.name ||
    (id != null ? `#${id}` : "—")
  );
}

export default function BookingsList() {
  const { user } = useAuth();
  const [page, setPage] = useState(0);
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
        "sort-by": "date",
        "sort-dir": "desc",
      };
      return isProvider
        ? Bookings.listByProvider(id, params)
        : Bookings.listByCustomer(id, params);
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

      <Card>
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
            {grouped.upcoming.length > 0 && (
              <section>
                <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-500">
                  Upcoming
                </h3>
                <ul className="divide-y">
                  {grouped.upcoming.map((b) => {
                    const id = b.id ?? b.bookingId;
                    const status = b.status ?? b.bookingStatus;
                    const counterparty = isProvider
                      ? b.customerName
                      : b.providerName ?? b.serviceProviderName;
                    return (
                      <li key={id} className="py-3">
                        <div className="flex items-start justify-between gap-4">
                          <div className="min-w-0">
                            <Link
                              to={`/bookings/${id}`}
                              className="block truncate font-medium text-zinc-900 hover:underline"
                            >
                              {serviceLabel(b)}
                            </Link>
                            <div className="mt-0.5 text-sm text-zinc-600">
                              {formatDateRangeLabel(b)}
                            </div>
                            <div className="mt-0.5 text-sm text-zinc-500">
                              Provider: {providerLabel(b)}
                            </div>
                            <div className="mt-0.5 text-sm text-zinc-500">
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

            {grouped.past.length > 0 && (
              <section>
                <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-500">
                  Past
                </h3>
                <ul className="divide-y">
                  {grouped.past.map((b) => {
                    const id = b.id ?? b.bookingId;
                    const status = b.status ?? b.bookingStatus;
                    const counterparty = isProvider
                      ? b.customerName
                      : b.providerName ?? b.serviceProviderName;
                    const isCompleted = String(status || "")
                      .toUpperCase()
                      .includes("COMPLETED");
                    return (
                      <li key={id} className="py-3">
                        <div className="flex items-start justify-between gap-4">
                          <div className="min-w-0">
                            <Link
                              to={`/bookings/${id}`}
                              className="block truncate font-medium text-zinc-900 hover:underline"
                            >
                              {serviceLabel(b)}
                            </Link>
                            <div className="mt-0.5 text-sm text-zinc-600">
                              {formatDateRangeLabel(b)}
                            </div>
                            <div className="mt-0.5 text-sm text-zinc-500">
                              Provider: {providerLabel(b)}
                            </div>
                            <div className="mt-0.5 text-sm text-zinc-500">
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
