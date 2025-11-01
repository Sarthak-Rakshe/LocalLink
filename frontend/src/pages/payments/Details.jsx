import { useParams, Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Payments } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";

export default function PaymentDetails() {
  const { id } = useParams();

  const q = useQuery({
    queryKey: ["payment", id],
    queryFn: async () => Payments.getById(Number(id)),
    enabled: !!id,
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Payment #{id}</h1>
        <div className="flex items-center gap-2">
          <Link to="/payments">
            <Button variant="outline">Back</Button>
          </Link>
        </div>
      </div>

      {q.isLoading && <div className="p-3 text-sm text-zinc-600">Loading…</div>}
      {q.isError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {q.error?.response?.data?.message ||
            q.error?.message ||
            "Failed to load payment"}
        </div>
      )}

      {!q.isLoading && !q.isError && q.data && (
        <Card>
          <dl className="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div>
              <dt className="text-sm text-zinc-500">Transaction ID</dt>
              <dd className="font-medium">{q.data.transactionId}</dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">Booking ID</dt>
              <dd className="font-medium">{q.data.bookingId ?? "-"}</dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">Customer ID</dt>
              <dd className="font-medium">{q.data.customerId ?? "-"}</dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">Amount</dt>
              <dd className="font-medium">
                ₹{q.data.amount?.toFixed?.(2) ?? q.data.amount}
              </dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">Method</dt>
              <dd className="font-medium">{q.data.paymentMethod}</dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">Status</dt>
              <dd className="font-medium">{q.data.paymentStatus}</dd>
            </div>
            <div className="md:col-span-2">
              <dt className="text-sm text-zinc-500">Transaction Reference</dt>
              <dd className="font-medium break-all">
                {q.data.transactionReference ?? "-"}
              </dd>
            </div>
            <div className="md:col-span-2">
              <dt className="text-sm text-zinc-500">Created</dt>
              <dd className="font-medium">{q.data.createdAt}</dd>
            </div>
          </dl>
        </Card>
      )}
    </div>
  );
}
