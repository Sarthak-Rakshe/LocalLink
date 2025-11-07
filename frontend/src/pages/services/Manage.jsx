import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import { Services } from "../../services/api.js";
import Button from "../../components/ui/Button.jsx";
import Card from "../../components/ui/Card.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";

export default function ManageServices() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const providerId = user?.id ?? user?.userId;

  const [page, setPage] = useState(0);
  const [size, _setSize] = useState(10);
  const [sortBy, setSortBy] = useState("name");
  const [sortDir, setSortDir] = useState("asc");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [data, setData] = useState({
    content: [],
    totalPages: 0,
    totalElements: 0,
    pageNumber: 0,
  });

  const fetchMine = async () => {
    if (!providerId) return;
    setLoading(true);
    setError("");
    try {
      const res = await Services.getAll(
        { serviceProviderId: Number(providerId) },
        { page, size, sortBy, sortDir }
      );
      setData(res);
    } catch (e) {
      setError(
        e?.response?.data?.message ||
          e.message ||
          "Failed to load your services"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMine();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [providerId, page, size, sortBy, sortDir]);

  const items = useMemo(
    () => (Array.isArray(data?.content) ? data.content : []),
    [data]
  );

  const handleDelete = async (id) => {
    if (!id) return;
    const sure = confirm("Delete this service? This cannot be undone.");
    if (!sure) return;
    try {
      await Services.remove(id);
      await fetchMine();
    } catch (e) {
      alert(e?.response?.data?.message || e.message || "Failed to delete");
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">My services</h1>
        <div className="flex items-center gap-2">
          <select
            className="input-base px-2 py-2 text-sm"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="name">Sort: Name</option>
            <option value="price">Sort: Price</option>
            <option value="category">Sort: Category</option>
            <option value="id">Sort: Newest</option>
          </select>
          <select
            className="input-base px-2 py-2 text-sm"
            value={sortDir}
            onChange={(e) => setSortDir(e.target.value)}
          >
            <option value="asc">Asc</option>
            <option value="desc">Desc</option>
          </select>
          <Button variant="primary" onClick={() => navigate("/services/new")}>
            New service
          </Button>
        </div>
      </div>

      {error && (
        <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: Math.min(size, 9) }).map((_, i) => (
            <div
              key={i}
              className="h-36 animate-pulse rounded-lg border bg-zinc-100"
            />
          ))}
        </div>
      ) : items.length ? (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {items.map((s) => (
            <Card key={s.serviceId}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                    {s.serviceName}
                  </div>
                  <div className="text-xs text-zinc-500 dark:text-zinc-400">
                    <span className="inline-flex items-center rounded bg-zinc-100 px-2 py-0.5 text-[11px] font-medium text-zinc-700 dark:bg-white/5 dark:text-zinc-300">
                      {s.serviceCategory}
                    </span>
                    <span className="ml-2">₹{s.servicePricePerHour}/hr</span>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    onClick={() => navigate(`/services/${s.serviceId}/edit`)}
                  >
                    Edit
                  </Button>
                  <Button
                    variant="danger"
                    onClick={() => handleDelete(s.serviceId)}
                  >
                    Delete
                  </Button>
                </div>
              </div>
              {s.serviceDescription && (
                <p className="mt-2 line-clamp-3 text-sm text-zinc-700">
                  {s.serviceDescription}
                </p>
              )}
            </Card>
          ))}
        </div>
      ) : (
        <EmptyState
          title="You have no services"
          message="Create your first service using the button above."
        />
      )}

      {data?.totalPages > 1 && (
        <div className="mt-2 flex items-center justify-center gap-2">
          <Button
            variant="outline"
            disabled={page <= 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Previous
          </Button>
          <div className="text-sm text-zinc-600">
            Page {data.pageNumber + 1} of {data.totalPages}
          </div>
          <Button
            variant="outline"
            disabled={data.pageNumber + 1 >= data.totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
}
