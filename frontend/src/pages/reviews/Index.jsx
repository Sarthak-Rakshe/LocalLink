import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useLocation, useSearchParams } from "react-router-dom";
import { Reviews, Bookings, Services } from "../../services/api.js";
import { useAuth } from "../../context/AuthContext.jsx";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";
import { Label } from "../../components/ui/Input.jsx";
import toast from "react-hot-toast";
import PageHeader from "../../components/ui/PageHeader.jsx";
import { StarIcon } from "@heroicons/react/24/solid";

export default function ReviewsHome() {
  const { user } = useAuth();
  const isCustomer = user?.userType === "CUSTOMER";
  const isProvider = user?.userType === "PROVIDER";
  const userId = user?.id ?? user?.userId;
  const [search] = useSearchParams();
  const location = useLocation();

  // Preselect from query params
  const preServiceId = search.get("serviceId") ?? location.state?.serviceId;
  const preProviderId = search.get("providerId") ?? location.state?.providerId;
  const preBookingId = search.get("bookingId") ?? location.state?.bookingId;

  // Fetch my reviews (customer)
  const myReviewsQ = useQuery({
    queryKey: ["my-reviews", userId, isCustomer],
    queryFn: () =>
      Reviews.myReviewsCustomer({
        page: 0,
        size: 100,
        sortBy: "createdAt",
        sortDir: "desc",
      }),
    enabled: !!userId && isCustomer,
  });

  // Fetch completed bookings for this customer to discover which services can be reviewed.
  const completedBookingsQ = useQuery({
    queryKey: ["completed-bookings", userId, isCustomer],
    queryFn: () =>
      Bookings.getList(
        { bookingStatus: "COMPLETED", customerId: userId },
        { page: 0, size: 200, "sort-by": "createdAt", "sort-dir": "desc" }
      ),
    enabled: !!userId && isCustomer,
    staleTime: 1000 * 60,
  });

  // Fetch received reviews (provider)
  const providerReviewsQ = useQuery({
    queryKey: ["provider-reviews", userId, isProvider],
    queryFn: () =>
      Reviews.myReviewsProvider({
        page: 0,
        size: 100,
        sortBy: "createdAt",
        sortDir: "desc",
      }),
    enabled: !!userId && isProvider,
  });

  // Removed bookings-based eligibility and service expansion to avoid extra load

  const myReviewsByService = useMemo(() => {
    const raw = myReviewsQ.data;
    const items = raw?.content ?? raw ?? [];
    /** @type {Record<number, any>} */
    const map = {};
    for (const r of items) {
      if (r?.serviceId != null) map[r.serviceId] = r;
    }
    return map;
  }, [myReviewsQ.data]);

  // Derive eligible (completed & not yet reviewed) bookings
  const eligibleBookings = useMemo(() => {
    if (!completedBookingsQ.data) return [];
    const list =
      completedBookingsQ.data.content ?? completedBookingsQ.data ?? [];
    /** dedupe by serviceId so we only show one row per service */
    const seen = new Set();
    const out = [];
    for (const b of list) {
      const serviceId = b?.service?.serviceId;
      if (!serviceId) continue;
      if (myReviewsByService[serviceId]) continue; // already reviewed
      if (seen.has(serviceId)) continue; // already added
      seen.add(serviceId);
      out.push(b);
    }
    return out;
  }, [completedBookingsQ.data, myReviewsByService]);

  // UI state for editing
  const [editing, setEditing] = useState(null); // {serviceId, providerId, bookingId?, reviewId?}
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");

  useEffect(() => {
    // Pre-open editor if navigated with query params. Allow fallback to existing review's providerId.
    if (!isCustomer || !preServiceId) return;
    const sId = Number(preServiceId);
    if (!sId) return; // invalid service id
    const existing = myReviewsByService[sId];
    // Prefer explicit providerId from query; fall back to existing review's provider/providerId fields.
    const pIdRaw =
      preProviderId || existing?.serviceProviderId || existing?.providerId;
    const pId = Number(pIdRaw);
    if (!pId) return; // can't open form without provider id
    setEditing({
      serviceId: sId,
      providerId: pId,
      bookingId:
        existing?.bookingId ||
        (preBookingId ? Number(preBookingId) : undefined),
      reviewId: existing?.reviewId,
    });
    setRating(existing?.rating ?? 5);
    setComment(existing?.comment ?? "");
  }, [isCustomer, preServiceId, preProviderId, myReviewsByService]);

  const addMutation = useMutation({
    mutationFn: (payload) => Reviews.add(payload),
    onSuccess: () => {
      toast.success("Review added");
      myReviewsQ.refetch();
      setEditing(null);
      setComment("");
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to add review"),
  });

  const updateMutation = useMutation({
    mutationFn: ({ reviewId, payload }) => Reviews.update(reviewId, payload),
    onSuccess: () => {
      toast.success("Review updated");
      myReviewsQ.refetch();
      setEditing(null);
    },
    onError: (e) =>
      toast.error(e?.response?.data?.message || "Failed to update review"),
  });

  function startEdit(serviceId, providerId, bookingId) {
    const existing = myReviewsByService[serviceId];
    setEditing({
      serviceId,
      providerId,
      bookingId: bookingId || existing?.bookingId,
      reviewId: existing?.reviewId,
    });
    setRating(existing?.rating ?? 5);
    setComment(existing?.comment ?? "");
  }

  function cancelEdit() {
    setEditing(null);
    setComment("");
  }

  function submitEdit() {
    if (!editing) return;
    const customerId = userId;
    const trimmed = (comment ?? "").trim();
    if (!trimmed) {
      toast.error("Please add a comment to your review");
      return;
    }
    if (!editing.bookingId && !editing.reviewId) {
      toast.error("Missing booking reference for new review.");
      return;
    }
    const payload = {
      serviceProviderId: Number(editing.providerId),
      serviceId: Number(editing.serviceId),
      bookingId: editing.bookingId ? Number(editing.bookingId) : undefined,
      customerId: Number(customerId),
      rating: Number(rating),
      comment: trimmed,
    };
    if (editing.reviewId) {
      updateMutation.mutate({ reviewId: editing.reviewId, payload });
    } else {
      addMutation.mutate(payload);
    }
  }

  return (
    <div className="space-y-4">
      <PageHeader
        title="Reviews"
        description={
          isCustomer
            ? "Share feedback on services you’ve used, or update your past reviews."
            : "See what customers are saying about your services."
        }
      />
      {isCustomer && editing && (
        <Card>
          <div className="mb-3 text-lg font-medium">
            {editing.reviewId ? "Update your review" : "Add a review"}
          </div>
          <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div>
              <Label>Rating</Label>
              <div className="flex items-center gap-1 py-2">
                {[1, 2, 3, 4, 5].map((r) => (
                  <button
                    key={r}
                    type="button"
                    className="p-1"
                    aria-label={`${r} star${r > 1 ? "s" : ""}`}
                    onClick={() => setRating(r)}
                  >
                    <StarIcon
                      className={`size-6 ${
                        r <= rating ? "text-amber-400" : "text-zinc-300"
                      }`}
                    />
                  </button>
                ))}
                <span className="ml-2 text-sm text-zinc-600">{rating}/5</span>
              </div>
            </div>
            <div className="md:col-span-2">
              <Label>Comment</Label>
              <textarea
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm shadow-sm placeholder:text-zinc-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/50"
                rows={3}
                placeholder="Share your experience"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
              />
            </div>
            <div className="md:col-span-2 flex items-center gap-2">
              <Button
                onClick={submitEdit}
                disabled={
                  addMutation.isPending ||
                  updateMutation.isPending ||
                  !(rating >= 1 && rating <= 5) ||
                  !(comment && comment.trim().length > 0)
                }
              >
                {addMutation.isPending || updateMutation.isPending
                  ? "Saving…"
                  : editing.reviewId
                  ? "Update"
                  : "Submit"}
              </Button>
              <Button variant="outline" onClick={cancelEdit}>
                Cancel
              </Button>
            </div>
          </div>
        </Card>
      )}

      {isCustomer && !editing && (preServiceId || preProviderId) && (
        <Card>
          <div className="text-sm text-amber-700">
            {(!preServiceId || !Number(preServiceId)) && (
              <div>Provided service id is invalid.</div>
            )}
            {preServiceId && !preProviderId && (
              <div>
                Missing provider id in URL. Please navigate from a completed
                booking or supply both serviceId and providerId query
                parameters.
              </div>
            )}
          </div>
        </Card>
      )}

      {isCustomer && !editing && (
        <Card>
          <div className="mb-3 text-lg font-medium">Pending reviews</div>
          {completedBookingsQ.isLoading && (
            <div className="text-sm text-zinc-500">
              Loading completed bookings…
            </div>
          )}
          {completedBookingsQ.isError && (
            <div className="text-sm text-red-600">
              {completedBookingsQ.error?.response?.data?.message ||
                "Failed to load completed bookings."}
            </div>
          )}
          {completedBookingsQ.data && eligibleBookings.length === 0 && (
            <div className="text-sm text-zinc-600">
              You have no completed services awaiting a review.
            </div>
          )}
          {eligibleBookings.length > 0 && (
            <ul className="divide-y">
              {eligibleBookings.map((b) => {
                const service = b.service;
                const provider = b.serviceProvider;
                const serviceId = service?.serviceId;
                const providerId = provider?.serviceProviderId;
                return (
                  <li
                    key={b.bookingId}
                    className="py-3 text-sm flex items-start justify-between gap-3"
                  >
                    <div>
                      <div className="font-medium">
                        {service?.serviceName ||
                          service?.serviceTitle ||
                          `Service #${serviceId}`}
                      </div>
                      <div className="text-xs text-zinc-500">
                        Provider #{providerId}
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button
                        onClick={() =>
                          startEdit(serviceId, providerId, b.bookingId)
                        }
                        variant="primary"
                      >
                        Review
                      </Button>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </Card>
      )}

      {isCustomer && (
        <Card>
          <div className="mb-3 text-lg font-medium">My reviews</div>
          {myReviewsQ.isLoading && (
            <div className="text-sm text-zinc-500">Loading…</div>
          )}
          {myReviewsQ.isError && (
            <div className="text-sm text-red-600">
              {myReviewsQ.error?.response?.data?.message ||
                "Failed to load my reviews."}
            </div>
          )}
          {myReviewsQ.data && (
            <ul className="divide-y">
              {(myReviewsQ.data.content ?? myReviewsQ.data ?? []).map((r) => (
                <li key={r.reviewId} className="py-3">
                  <div className="flex items-start justify-between gap-3 text-sm">
                    <div className="flex items-center gap-3">
                      <Button
                        variant="outline"
                        onClick={() =>
                          startEdit(
                            r.serviceId,
                            r.serviceProviderId,
                            r.bookingId
                          )
                        }
                      >
                        Edit
                      </Button>
                      <div>
                        <div className="font-medium">
                          <ServiceName serviceId={r.serviceId} />
                        </div>
                        {r.comment && (
                          <div className="text-zinc-600">{r.comment}</div>
                        )}
                      </div>
                    </div>
                    <div className="text-zinc-700 whitespace-nowrap flex items-center gap-1">
                      {Array.from({ length: 5 }).map((_, i) => (
                        <StarIcon
                          key={i}
                          className={`size-4 ${
                            i < (r.rating ?? 0)
                              ? "text-amber-400"
                              : "text-zinc-300"
                          }`}
                        />
                      ))}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>
      )}

      {isProvider && (
        <Card>
          <div className="mb-3 text-lg font-medium">My received reviews</div>
          {providerReviewsQ.isLoading && (
            <div className="text-sm text-zinc-500">Loading…</div>
          )}
          {providerReviewsQ.isError && (
            <div className="text-sm text-red-600">
              {providerReviewsQ.error?.response?.data?.message ||
                "Failed to load reviews."}
            </div>
          )}
          {providerReviewsQ.data && (
            <ul className="divide-y">
              {(
                providerReviewsQ.data.content ??
                providerReviewsQ.data ??
                []
              ).map((r) => (
                <li key={r.reviewId} className="py-3">
                  <div className="flex items-center justify-between text-sm">
                    <div>
                      <div className="font-medium">
                        <ServiceName serviceId={r.serviceId} />
                      </div>
                      {r.comment && (
                        <div className="text-zinc-600">{r.comment}</div>
                      )}
                    </div>
                    <div className="text-zinc-700 flex items-center gap-1">
                      {Array.from({ length: 5 }).map((_, i) => (
                        <StarIcon
                          key={i}
                          className={`size-4 ${
                            i < (r.rating ?? 0)
                              ? "text-amber-400"
                              : "text-zinc-300"
                          }`}
                        />
                      ))}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>
      )}
    </div>
  );
}

function ServiceName({ serviceId, className, fallback }) {
  const enabled = !!serviceId;
  const q = useQuery({
    queryKey: ["service", serviceId],
    queryFn: () => Services.getById(serviceId),
    enabled,
    staleTime: 1000 * 60 * 5,
  });
  const name = q.data?.serviceName || q.data?.serviceTitle;
  return (
    <span className={className}>
      {name || fallback || `Service #${serviceId}`}
    </span>
  );
}
