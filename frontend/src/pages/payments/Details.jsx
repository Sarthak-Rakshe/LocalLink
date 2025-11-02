import { useParams, Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Payments } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";
import toast from "react-hot-toast";
import {
  getRemainingMs,
  isInCooldown,
  markRetryAttempt,
  formatDuration,
} from "../../services/retryCooldown.js";
import { useEffect, useMemo, useState } from "react";

export default function PaymentDetails() {
  const { id } = useParams();
  const txId = Number(id);
  const qc = useQueryClient();
  const [tick, setTick] = useState(0);

  const q = useQuery({
    queryKey: ["payment", id],
    queryFn: async () => Payments.getById(txId),
    enabled: !!txId,
  });

  // background timer to update cooldown display every second
  useEffect(() => {
    const t = setInterval(() => setTick((n) => n + 1), 1000);
    return () => clearInterval(t);
  }, []);

  const canRetry = useMemo(() => {
    const status = q.data?.paymentStatus;
    const eligible = status === "PENDING" || status === "DECLINED";
    return Boolean(eligible);
  }, [q.data?.paymentStatus]);

  const remainingMs = useMemo(
    () => (txId ? getRemainingMs(txId) : 0),
    [txId, tick]
  );
  const inCooldown = useMemo(
    () => (txId ? isInCooldown(txId) : false),
    [txId, tick]
  );

  const retryMutation = useMutation({
    mutationFn: async () => Payments.retry(txId),
    onSuccess: () => {
      markRetryAttempt(txId);
      toast.success("Retry requested. Refreshing status…");
      // refresh this payment and list pages
      qc.invalidateQueries({ queryKey: ["payment", id] });
      qc.invalidateQueries({ queryKey: ["payments"] });
    },
    onError: (err) => {
      const msg =
        err?.response?.data?.message ||
        (err?.response?.status === 404
          ? "Retry endpoint not available on the server. Please ensure the backend exposes POST /api/payments/{id}/retry (or share the exact path)."
          : err?.message) ||
        "Failed to retry payment";
      toast.error(msg);
    },
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Payment #{id}</h1>
        <div className="flex items-center gap-2">
          {q.data && (
            <>
              <Button
                onClick={() => retryMutation.mutate()}
                disabled={!canRetry || inCooldown || retryMutation.isPending}
              >
                {retryMutation.isPending
                  ? "Retrying…"
                  : inCooldown
                  ? `Retry in ${formatDuration(remainingMs)}`
                  : "Retry payment"}
              </Button>
            </>
          )}
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
              <dt className="text-sm text-zinc-500">Service Provider ID</dt>
              <dd className="font-medium">{q.data.serviceProviderId ?? "-"}</dd>
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
            {(q.data.paymentStatus === "PENDING" ||
              q.data.paymentStatus === "DECLINED") && (
              <div className="md:col-span-2 text-sm text-zinc-600">
                {inCooldown
                  ? `Retry available in ${formatDuration(remainingMs)}.`
                  : "You can retry this payment if it was declined or still pending."}
              </div>
            )}
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
