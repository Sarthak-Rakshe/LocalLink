import { useEffect, useMemo, useState } from "react";
import { Services } from "../../services/api.js";
import ServiceCard from "../../components/services/ServiceCard.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";

export default function ServicesExplore() {
  // Filters as per backend QueryFilter
  const [filters, setFilters] = useState({
    serviceName: "",
    category: "",
    minPrice: "",
    maxPrice: "",
    providerId: "",
  });
  const [useNearby, setUseNearby] = useState(false);
  const [coords, setCoords] = useState({ lat: null, lng: null, error: null });
  const [sortBy, setSortBy] = useState("name");
  const [sortDir, setSortDir] = useState("asc");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(12);
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState({
    content: [],
    totalPages: 0,
    totalElements: 0,
    pageNumber: 0,
  });
  const [error, setError] = useState("");

  // When Nearby is enabled, backend endpoint doesn't accept filters.
  // Apply client-side filtering to the nearby result so filters still work together.
  const displayData = useMemo(() => {
    if (!useNearby) return data;
    const src = Array.isArray(data?.content) ? data.content : [];
    const norm = (v) => (v ?? "").toString().trim().toLowerCase();
    const nameQ = norm(filters.serviceName);
    const catQ = norm(filters.category);
    const min = filters.minPrice !== "" ? Number(filters.minPrice) : null;
    const max = filters.maxPrice !== "" ? Number(filters.maxPrice) : null;
    const provider =
      filters.providerId !== "" ? Number(filters.providerId) : null;

    const filtered = src.filter((s) => {
      // serviceName contains
      if (nameQ && !norm(s?.serviceName).includes(nameQ)) return false;
      // category equals (case-insensitive)
      if (catQ && norm(s?.serviceCategory) !== catQ) return false;
      // price range
      if (min !== null && Number(s?.servicePricePerHour) < min) return false;
      if (max !== null && Number(s?.servicePricePerHour) > max) return false;
      // provider id (matches serviceProviderId)
      if (provider !== null && Number(s?.serviceProviderId) !== provider)
        return false;
      return true;
    });

    // Keep paging simple on nearby: we reflect the filtered count but don't repaginate across pages client-side
    return {
      ...data,
      content: filtered,
      totalElements: filtered.length,
      totalPages: data.totalPages,
    };
  }, [useNearby, data, filters]);

  // Normalize filter payload to backend shape
  const filterPayload = useMemo(() => {
    const payload = {};
    if (filters.serviceName?.trim())
      payload.serviceName = filters.serviceName.trim();
    if (filters.category?.trim()) payload.category = filters.category.trim();
    if (filters.minPrice !== "" && !Number.isNaN(Number(filters.minPrice)))
      payload.minPrice = Number(filters.minPrice);
    if (filters.maxPrice !== "" && !Number.isNaN(Number(filters.maxPrice)))
      payload.maxPrice = Number(filters.maxPrice);
    // Provider filter: backend expects QueryFilter.userId; it resolves based on principal userType
    if (
      filters.providerId !== "" &&
      !Number.isNaN(Number(filters.providerId))
    ) {
      payload.userId = Number(filters.providerId);
    }
    return payload;
  }, [filters]);

  const fetchData = async () => {
    setLoading(true);
    setError("");
    try {
      if (useNearby) {
        if (coords.lat == null || coords.lng == null) {
          throw new Error("Location not available yet");
        }
        const res = await Services.getNearby({
          userLatitude: coords.lat,
          userLongitude: coords.lng,
          page,
          size,
          sortBy,
          sortDir,
        });
        setData(res);
      } else {
        const res = await Services.getAll(filterPayload, {
          page,
          size,
          sortBy,
          sortDir,
        });
        setData(res);
      }
    } catch (e) {
      setError(
        e?.response?.data?.message || e.message || "Failed to load services"
      );
    } finally {
      setLoading(false);
    }
  };

  // Geolocation when nearby enabled
  useEffect(() => {
    if (!useNearby) return;
    if (!("geolocation" in navigator)) {
      setCoords({ lat: null, lng: null, error: "Geolocation not supported" });
      return;
    }
    const watchId = navigator.geolocation.getCurrentPosition(
      (pos) => {
        setCoords({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          error: null,
        });
      },
      (err) => {
        setCoords({
          lat: null,
          lng: null,
          error: err.message || "Location permission denied",
        });
      },
      { enableHighAccuracy: false, maximumAge: 60_000, timeout: 10_000 }
    );
    return () => {
      if (typeof watchId === "number")
        navigator.geolocation.clearWatch(watchId);
    };
  }, [useNearby]);

  // Refetch when dependencies change
  useEffect(() => {
    // reset to first page on filter changes
    setPage(0);
  }, [filterPayload, useNearby, sortBy, sortDir, size]);

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    page,
    filterPayload,
    useNearby,
    coords.lat,
    coords.lng,
    sortBy,
    sortDir,
    size,
  ]);

  return (
    <div className="grid gap-6 lg:grid-cols-12">
      {/* Filters */}
      <aside className="lg:col-span-3 space-y-4">
        <div className="rounded-xl border bg-white p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold">Filters</h2>
            <button
              type="button"
              onClick={() =>
                setFilters({
                  serviceName: "",
                  category: "",
                  minPrice: "",
                  maxPrice: "",
                  providerId: "",
                })
              }
              className="text-xs text-indigo-600 hover:underline"
            >
              Reset
            </button>
          </div>
          <div className="mt-3 space-y-3">
            <div>
              <Label htmlFor="serviceName">Search</Label>
              <Input
                id="serviceName"
                placeholder="Service name..."
                value={filters.serviceName}
                onChange={(e) =>
                  setFilters((f) => ({ ...f, serviceName: e.target.value }))
                }
              />
            </div>
            <div>
              <Label htmlFor="category">Category</Label>
              <Input
                id="category"
                placeholder="e.g. Plumbing, Cleaning"
                value={filters.category}
                onChange={(e) =>
                  setFilters((f) => ({ ...f, category: e.target.value }))
                }
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="minPrice">Min price</Label>
                <Input
                  id="minPrice"
                  type="number"
                  inputMode="decimal"
                  placeholder="0"
                  value={filters.minPrice}
                  onChange={(e) =>
                    setFilters((f) => ({ ...f, minPrice: e.target.value }))
                  }
                />
              </div>
              <div>
                <Label htmlFor="maxPrice">Max price</Label>
                <Input
                  id="maxPrice"
                  type="number"
                  inputMode="decimal"
                  placeholder="1000"
                  value={filters.maxPrice}
                  onChange={(e) =>
                    setFilters((f) => ({ ...f, maxPrice: e.target.value }))
                  }
                />
              </div>
            </div>
            <div>
              <Label htmlFor="providerId">Provider ID</Label>
              <Input
                id="providerId"
                type="number"
                inputMode="numeric"
                placeholder="e.g. 101"
                value={filters.providerId}
                onChange={(e) =>
                  setFilters((f) => ({ ...f, providerId: e.target.value }))
                }
              />
              <p className="mt-1 text-xs text-zinc-500">
                Sends userId in QueryFilter. Backend resolves by your user type
                (PROVIDER filters by serviceProviderId).
              </p>
            </div>
          </div>
        </div>

        <div className="rounded-xl border bg-white p-4 shadow-sm">
          <h2 className="text-sm font-semibold">Options</h2>
          <div className="mt-3 space-y-3">
            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  className="size-4 rounded border-zinc-300"
                  checked={useNearby}
                  onChange={(e) => setUseNearby(e.target.checked)}
                />
                Show nearby services
              </label>
              {useNearby && (
                <span className="text-xs text-zinc-500">
                  {coords.error
                    ? coords.error
                    : coords.lat
                    ? "Using your location"
                    : "Getting location..."}
                </span>
              )}
            </div>

            <div className="grid grid-cols-2 gap-2">
              <select
                className="w-full rounded-md border border-zinc-300 px-2 py-2 text-sm"
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
              >
                <option value="name">Sort: Name</option>
                <option value="price">Sort: Price</option>
                <option value="category">Sort: Category</option>
                <option value="id">Sort: Newest</option>
              </select>
              <select
                className="w-full rounded-md border border-zinc-300 px-2 py-2 text-sm"
                value={sortDir}
                onChange={(e) => setSortDir(e.target.value)}
              >
                <option value="asc">Asc</option>
                <option value="desc">Desc</option>
              </select>
            </div>

            <div>
              <Label htmlFor="pageSize">Page size</Label>
              <select
                id="pageSize"
                className="w-full rounded-md border border-zinc-300 px-2 py-2 text-sm"
                value={size}
                onChange={(e) => setSize(Number(e.target.value))}
              >
                {[6, 12, 24, 48].map((n) => (
                  <option key={n} value={n}>
                    {n} per page
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </aside>

      {/* Results */}
      <section className="lg:col-span-9">
        <div className="mb-3 flex items-center justify-between">
          <h1 className="text-lg font-semibold text-zinc-900">
            Explore services
          </h1>
          {displayData?.totalElements != null && (
            <div className="text-sm text-zinc-600">
              {displayData.totalElements} results
            </div>
          )}
        </div>

        {error && (
          <div className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
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
        ) : displayData?.content?.length ? (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {displayData.content.map((s) => (
              <ServiceCard key={s.serviceId} service={s} />
            ))}
          </div>
        ) : (
          <div className="mt-10">
            <EmptyState
              title="No services found"
              message="Try adjusting filters or search."
            />
          </div>
        )}

        {/* Pagination */}
        {displayData?.totalPages > 1 && (
          <div className="mt-6 flex items-center justify-center gap-2">
            <button
              type="button"
              className="inline-flex items-center rounded-md border px-3 py-1.5 text-sm disabled:opacity-50"
              disabled={page <= 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              Previous
            </button>
            <div className="text-sm text-zinc-600">
              Page {displayData.pageNumber + 1} of {displayData.totalPages}
            </div>
            <button
              type="button"
              className="inline-flex items-center rounded-md border px-3 py-1.5 text-sm disabled:opacity-50"
              disabled={displayData.pageNumber + 1 >= displayData.totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        )}
      </section>
    </div>
  );
}
