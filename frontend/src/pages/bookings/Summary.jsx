import { useQuery } from "@tanstack/react-query";
import { Bookings } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";
import { useAuth } from "../../context/AuthContext.jsx";

export default function BookingSummary() {
  const { user } = useAuth();
  const q = useQuery({
    queryKey: ["booking-summary"],
    queryFn: () => Bookings.mySummary(),
  });

  if (q.isLoading) {
    return <div className="p-6">Loading booking summary...</div>;
  }
  if (q.isError) {
    return (
      <div className="p-6 text-red-600">
        Failed to load summary.{" "}
        {q.error?.response?.data?.message || q.error?.message}
      </div>
    );
  }

  const data = q.data || {};
  const title =
    user?.userType === "PROVIDER"
      ? "Your Booking Summary"
      : "Your Bookings Overview";
  const subtitle =
    user?.userType === "PROVIDER"
      ? "Aggregated counts across your provided services"
      : "Aggregated counts across services you've booked";

  const items = [
    { label: "Total", value: data.totalBookings },
    { label: "Completed", value: data.completedBookings },
    { label: "Confirmed", value: data.confirmedBookings },
    { label: "Pending", value: data.pendingBookings },
    { label: "Cancelled", value: data.cancelledBookings },
    { label: "Rescheduled", value: data.rescheduledBookings },
    { label: "Deleted", value: data.deletedBookings },
  ];

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">{title}</h1>
        <p className="text-zinc-600 mt-1">{subtitle}</p>
      </div>

      {data.requesterId != null && (
        <Card className="p-4">
          <div className="text-sm text-zinc-600">Requester ID</div>
          <div className="mt-1 text-xl font-medium">{data.requesterId}</div>
        </Card>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {items.map((it) => (
          <Card key={it.label} className="p-4">
            <div className="text-sm text-zinc-600">{it.label}</div>
            <div className="mt-1 text-3xl font-semibold">{it.value ?? 0}</div>
          </Card>
        ))}
      </div>

      <div className="flex gap-3">
        <Button onClick={() => q.refetch()} variant="secondary">
          Refresh
        </Button>
      </div>
    </div>
  );
}
