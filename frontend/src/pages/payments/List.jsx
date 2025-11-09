import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../context/AuthContext.jsx";
import { Payments } from "../../services/api.js";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import Button from "../../components/ui/Button.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import { Link } from "react-router-dom";
import toast from "react-hot-toast";
import PageHeader from "../../components/ui/PageHeader.jsx";
import Badge from "../../components/ui/Badge.jsx";
import {
  isInCooldown,
  getRemainingMs,
  formatDuration,
  markRetryAttempt,
} from "../../services/retryCooldown.js";

const METHODS = ["CREDIT_CARD", "NET_BANKING", "UPI", "WALLET", "CASH"];
const STATUSES = ["PENDING", "COMPLETED", "FAILED", "DECLINED"];
const SORT_FIELDS = [
  { value: "createdAt", label: "Created" },
  { value: "amount", label: "Amount" },
  { value: "paymentMethod", label: "Method" },
  { value: "paymentStatus", label: "Status" },
  { value: "transactionId", label: "ID" },
];

export default function PaymentsList() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDir, setSortDir] = useState("desc");
  const [paymentMethod, setPaymentMethod] = useState("");
  const [paymentStatus, setPaymentStatus] = useState("");

  const filter = useMemo(() => {
    return {
      paymentMethod: paymentMethod || undefined,
      paymentStatus: paymentStatus || undefined,
    };
  }, [paymentMethod, paymentStatus]);

  // reset page when filters change
  useEffect(
    () => setPage(0),
    [paymentMethod, paymentStatus, sortBy, sortDir, size]
  );

  const q = useQuery({
    queryKey: [
      "payments",
      { page, size, sortBy, sortDir, paymentMethod, paymentStatus },
    ],
    queryFn: async () =>
      Payments.listAll(
        { "sort-by": sortBy, "sort-dir": sortDir, page, size },
        filter
      ),
    keepPreviousData: true,
  });

  const items = useMemo(() => {
    const raw = q.data ?? [];
    return Array.isArray(raw?.content)
      ? raw.content
      : Array.isArray(raw)
      ? raw
      : [];
  }, [q.data]);

  const retryMutation = useMutation({
    mutationFn: async (txId) => Payments.retry(txId),
    onSuccess: (_data, txId) => {
      markRetryAttempt(txId);
      toast.success("Retry requested");
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

  const refreshMutation = useMutation({
    mutationFn: async (txId) => Payments.refresh(txId),
    onSuccess: (_data, txId) => {
      toast.success("Payment status refreshed");
      qc.invalidateQueries({ queryKey: ["payments"] });
    },
    onError: (err) => {
      const msg =
        err?.response?.data?.message || err?.message || "Failed to refresh";
      toast.error(msg);
    },
  });

  const { user } = useAuth();
  const isProvider = user?.userType === "PROVIDER";
  const isCustomer = user?.userType === "CUSTOMER";

  return (
    <div className="space-y-4">
      <PageHeader
        title="Payments"
        description="Track transactions, filter by method or status, and retry pending ones."
        actions={
          <div className="flex items-center gap-2">
            <select
              className="rounded-md border border-zinc-300 px-2 py-2 text-sm dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
            >
              {SORT_FIELDS.map((f) => (
                <option key={f.value} value={f.value}>
                  Sort: {f.label}
                </option>
              ))}
            </select>
            <select
              className="rounded-md border border-zinc-300 px-2 py-2 text-sm dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
              value={sortDir}
              onChange={(e) => setSortDir(e.target.value)}
            >
              <option value="asc">Asc</option>
              <option value="desc">Desc</option>
            </select>
            <select
              className="rounded-md border border-zinc-300 px-2 py-2 text-sm dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
              value={size}
              onChange={(e) => setSize(Number(e.target.value))}
            >
              {[10, 20, 50].map((n) => (
                <option key={n} value={n}>
                  {n} / page
                </option>
              ))}
            </select>
          </div>
        }
      />

      <Card>
        <div className="grid grid-cols-1 gap-3 md:grid-cols-4">
          <div>
            <Label>Payment method</Label>
            <select
              className="w-full rounded-md border border-zinc-300 px-3 py-2 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
            >
              <option value="">All</option>
              {METHODS.map((m) => (
                <option key={m} value={m}>
                  {m}
                </option>
              ))}
            </select>
          </div>
          <div>
            <Label>Payment status</Label>
            <select
              className="w-full rounded-md border border-zinc-300 px-3 py-2 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
              value={paymentStatus}
              onChange={(e) => setPaymentStatus(e.target.value)}
            >
              <option value="">All</option>
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
        </div>
      </Card>

      {q.isError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {q.error?.response?.data?.message ||
            q.error?.message ||
            "Failed to load payments"}
        </div>
      )}

      {q.isLoading ? (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: Math.min(size, 9) }).map((_, i) => (
            <div
              key={i}
              className="h-36 animate-pulse rounded-lg border border-zinc-200 bg-zinc-100"
            />
          ))}
        </div>
      ) : items.length ? (
        <div className="overflow-hidden rounded-lg border border-zinc-200 dark:border-zinc-800">
          <table className="min-w-full divide-y divide-zinc-200 dark:divide-zinc-800">
            <thead className="bg-zinc-50 dark:bg-white/5">
              <tr>
                <th className="px-3 py-2 text-left text-xs font-semibold text-zinc-600 dark:text-zinc-400">
                  ID
                </th>
                <th className="px-3 py-2 text-left text-xs font-semibold text-zinc-600 dark:text-zinc-400">
                  Provider ID
                </th>
                <th className="px-3 py-2 text-left text-xs font-semibold text-zinc-600 dark:text-zinc-400">
                  Reference
                </th>
                <th className="px-3 py-2 text-left text-xs font-semibold text-zinc-600 dark:text-zinc-400">
                  Amount
                </th>
                <th className="px-3 py-2 text-left text-xs font-semibold text-zinc-600 dark:text-zinc-400">
                  Method
                </th>
                <th className="px-3 py-2 text-left text-xs font-semibold text-zinc-600 dark:text-zinc-400">
                  Status
                </th>
                <th className="px-3 py-2 text-left text-xs font-semibold text-zinc-600 dark:text-zinc-400">
                  Created
                </th>
                <th className="px-3 py-2" />
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 bg-white dark:divide-zinc-800 dark:bg-zinc-900">
              {items.map((t) => (
                <tr key={t.transactionId}>
                  <td className="px-3 py-2 text-sm">{t.transactionId}</td>
                  <td className="px-3 py-2 text-sm">
                    {t.serviceProviderId ?? "-"}
                  </td>
                  <td className="px-3 py-2 text-sm">
                    {t.transactionReference || "-"}
                  </td>
                  <td className="px-3 py-2 text-sm">
                    ₹{t.amount?.toFixed?.(2) ?? t.amount}
                  </td>
                  <td className="px-3 py-2 text-sm">
                    <Badge color="blue">{t.paymentMethod}</Badge>
                  </td>
                  <td className="px-3 py-2 text-sm">
                    <Badge
                      color={
                        t.paymentStatus === "COMPLETED"
                          ? "green"
                          : t.paymentStatus === "FAILED" ||
                            t.paymentStatus === "DECLINED"
                          ? "red"
                          : "amber"
                      }
                    >
                      {t.paymentStatus}
                    </Badge>
                  </td>
                  <td className="px-3 py-2 text-sm">
                    {new Date(t.createdAt).toLocaleString?.() || t.createdAt}
                  </td>
                  <td className="px-3 py-2 text-right text-sm">
                    <div className="flex items-center justify-end gap-3">
                      {(t.paymentStatus === "PENDING" ||
                        t.paymentStatus === "DECLINED") && (
                        <>
                          {isProvider ? null : isCustomer ? (
                            <Button
                              size="sm"
                              onClick={() =>
                                refreshMutation.mutate(t.transactionId)
                              }
                              disabled={refreshMutation.isPending}
                            >
                              {refreshMutation.isPending
                                ? "Refreshing…"
                                : "Refresh"}
                            </Button>
                          ) : (
                            // non-customer (admin) keep retry behaviour
                            <Button
                              size="sm"
                              onClick={() =>
                                retryMutation.mutate(t.transactionId)
                              }
                              disabled={
                                isInCooldown(t.transactionId) ||
                                retryMutation.isPending
                              }
                              title={
                                isInCooldown(t.transactionId)
                                  ? `Retry in ${formatDuration(
                                      getRemainingMs(t.transactionId)
                                    )}`
                                  : undefined
                              }
                            >
                              {isInCooldown(t.transactionId)
                                ? `Retry in ${formatDuration(
                                    getRemainingMs(t.transactionId)
                                  )}`
                                : retryMutation.isPending
                                ? "Retrying…"
                                : "Retry"}
                            </Button>
                          )}
                        </>
                      )}
                      <Link
                        to={`/payments/${t.transactionId}`}
                        className="text-indigo-600 hover:underline"
                      >
                        View
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState
          title="No transactions"
          message="No payments found for your account."
        />
      )}

      {q.data?.totalPages > 1 && (
        <div className="mt-2 flex items-center justify-center gap-2">
          <Button
            variant="outline"
            disabled={page <= 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Previous
          </Button>
          <div className="text-sm text-zinc-600">
            Page {(q.data?.pageNumber ?? page) + 1} of {q.data?.totalPages}
          </div>
          <Button
            variant="outline"
            disabled={
              (q.data?.pageNumber ?? page) + 1 >= (q.data?.totalPages ?? 1)
            }
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
}
