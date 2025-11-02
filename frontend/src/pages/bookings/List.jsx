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
    const dateStr = b?.bookingDate || b?.date;
    const endRaw = b?.bookingEndTime || b?.endTime || b?.slot?.endTime;
    if (!dateStr || !endRaw) return null;
    const hhmm = String(endRaw).slice(0, 5);
    // Construct local datetime: YYYY-MM-DDTHH:mm:00
    const ts = `${dateStr}T${hhmm}:00`;
    const d = new Date(ts);
    return isNaN(d.getTime()) ? null : d;
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

        <ul className="divide-y">
          {items.map((b) => (
            <li key={b.id ?? b.bookingId} className="py-3">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <Link
                    to={`/bookings/${b.id ?? b.bookingId}`}
                    className="font-medium text-zinc-900 hover:underline"
                  >
                    Booking #{b.id ?? b.bookingId}
                  </Link>
                  <div className="text-sm text-zinc-600">
                    {formatDateTime(b.date ?? b.startTime ?? b.createdAt)}
                  </div>
                </div>
                <div className="text-right text-sm text-zinc-600">
                  <div>{b.serviceName ?? "Service"}</div>
                  <div className="text-zinc-500">
                    {b.providerName ??
                      b.serviceProviderName ??
                      b.customerName ??
                      ""}
                  </div>
                  {b.status && <Badge className="mt-1">{b.status}</Badge>}
                  {user?.userType === "CUSTOMER" &&
                    String(b.status ?? b.bookingStatus ?? "")
                      .toUpperCase()
                      .includes("COMPLETED") && (
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
          ))}
        </ul>

        {total > pageSize && (
          <div className="mt-3 flex items-center justify-between text-sm">
            <div className="text-zinc-600">Page {page + 1}</div>
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
