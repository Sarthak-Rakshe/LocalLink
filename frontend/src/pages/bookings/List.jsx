import { useMemo, useState } from "react";
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

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Bookings</h1>
        {user?.userType === "CUSTOMER" && (
          <Button as={Link} to="/bookings/create">
            New booking
          </Button>
        )}
      </div>

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
          <div className="text-sm text-red-600">
            {query.error?.response?.data?.message || "Failed to load bookings."}
          </div>
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
