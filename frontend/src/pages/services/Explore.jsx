import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { MagnifyingGlassIcon } from "@heroicons/react/24/outline";
import Switch from "../../components/ui/Switch.jsx";
import { Services } from "../../services/api.js";
import ServiceCard, {
  ServiceCardSkeleton,
} from "../../components/services/ServiceCard.jsx";
import { Input, Label } from "../../components/ui/Input.jsx";
import Select from "../../components/ui/Select.jsx";
import Button from "../../components/ui/Button.jsx";
import PageHeader from "../../components/ui/PageHeader.jsx";
import EmptyState from "../../components/ui/EmptyState.jsx";
import Alert from "../../components/ui/Alert.jsx";

export default function ServicesExplore() {
  const [searchParams, setSearchParams] = useSearchParams();
  // Filters as per backend QueryFilter
  const [filters, setFilters] = useState({
    serviceName: searchParams.get("q") || "",
    category: "",
    minPrice: "",
    maxPrice: "",
    providerId: "",
  });
  // UI state: show/hide filters sidebar (default hidden to prioritize results)
  const [showFilters, setShowFilters] = useState(false);
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
    // Provider filter: backend expects QueryFilter.serviceProviderId for public listing
    if (
      filters.providerId !== "" &&
      !Number.isNaN(Number(filters.providerId))
    ) {
      payload.serviceProviderId = Number(filters.providerId);
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

  // Keep URL in sync with search term for shareability
  useEffect(() => {
    const q = filters.serviceName?.trim() || null;
    const next = new URLSearchParams(searchParams);
    if (q) next.set("q", q);
    else next.delete("q");
    setSearchParams(next, { replace: true });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.serviceName]);

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
    <div className="grid gap-4 lg:grid-cols-12">
      <div className="lg:col-span-12">
        <PageHeader
          title="Explore services"
          description="Find trusted local providers. Filter by category, price, and more."
        />
      </div>
      {/* Prominent search bar */}
      <div className="lg:col-span-12">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            setPage(0);
          }}
          className="rounded-xl border border-zinc-200 bg-white p-2 md:p-3 shadow-sm dark:border-zinc-800 dark:bg-zinc-900"
          role="search"
          aria-label="Search services"
        >
          <label htmlFor="top-search" className="sr-only">
            Search services
          </label>
          <div className="relative">
            <input
              id="top-search"
              className="input-base pl-10 pr-28 py-2 text-sm md:text-base"
              placeholder="Search services, categories, providers…"
              value={filters.serviceName}
              onChange={(e) =>
                setFilters((f) => ({ ...f, serviceName: e.target.value }))
              }
            />
            <MagnifyingGlassIcon className="pointer-events-none absolute left-3 top-1/2 size-5 -translate-y-1/2 text-zinc-400" />
            <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1.5">
              {filters.serviceName && (
                <button
                  type="button"
                  onClick={() => setFilters((f) => ({ ...f, serviceName: "" }))}
                  className="btn btn-ghost px-2 py-1 text-xs"
                >
                  Clear
                </button>
              )}
              <Button type="submit" size="sm" className="px-3">
                Search
              </Button>
            </div>
          </div>

          {/* Compact toolbar: filters toggle + sorting + page size */}
          <div className="mt-2 flex flex-wrap items-center justify-between gap-2">
            <div className="inline-flex items-center gap-3">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setShowFilters((v) => !v)}
                aria-expanded={showFilters}
                aria-controls="filters-panel"
              >
                {showFilters ? "Hide filters" : "Show filters"}
              </Button>
              {/* Quick access nearby toggle */}
              <Switch
                label="Nearby"
                checked={useNearby}
                onChange={setUseNearby}
                className="-my-1"
              />
              {useNearby && (
                <span
                  className="hidden sm:inline text-xs text-zinc-500"
                  aria-live="polite"
                >
                  {coords.error
                    ? "Location error"
                    : coords.lat
                    ? "Using location"
                    : "Locating…"}
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 text-sm">
              <select
                className="input-base px-2 py-1.5 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100"
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                aria-label="Sort by"
              >
                <option value="name">Sort: Name</option>
                <option value="price">Sort: Price</option>
                <option value="category">Sort: Category</option>
                <option value="id">Sort: Newest</option>
              </select>
              <select
                className="input-base px-2 py-1.5 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100"
                value={sortDir}
                onChange={(e) => setSortDir(e.target.value)}
                aria-label="Sort direction"
              >
                <option value="asc">Asc</option>
                <option value="desc">Desc</option>
              </select>
              <select
                className="input-base px-2 py-1.5 bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100"
                value={size}
                onChange={(e) => setSize(Number(e.target.value))}
                aria-label="Page size"
              >
                {[6, 12, 24, 48].map((n) => (
                  <option key={n} value={n}>
                    {n} / page
                  </option>
                ))}
              </select>
            </div>
          </div>
        </form>
      </div>
      {/* Filters - collapsible */}
      <aside
        id="filters-panel"
        className={`${showFilters ? "block" : "hidden"} lg:${
          showFilters ? "block" : "hidden"
        } lg:col-span-3 space-y-4 lg:sticky lg:top-20 self-start h-fit`}
        aria-hidden={!showFilters}
      >
        {/* Nearby toggle lives in the top toolbar to avoid redundancy */}

        <div className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
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
                Sends serviceProviderId in query filter.
              </p>
            </div>
          </div>
        </div>
      </aside>

      {/* Results */}
      <section className={showFilters ? "lg:col-span-9" : "lg:col-span-12"}>
        {displayData?.totalElements != null && (
          <div className="mb-3 text-sm text-zinc-600 dark:text-zinc-400">
            {displayData.totalElements} results
          </div>
        )}

        {error && <Alert variant="error">{error}</Alert>}
        {loading ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {Array.from({ length: Math.min(size, 9) }).map((_, i) => (
              <ServiceCardSkeleton key={i} />
            ))}
          </div>
        ) : displayData?.content?.length ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
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
            <Button
              variant="outline"
              size="sm"
              disabled={page <= 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              Previous
            </Button>
            <div className="text-sm text-zinc-600">
              Page {displayData.pageNumber + 1} of {displayData.totalPages}
            </div>
            <Button
              variant="outline"
              size="sm"
              disabled={displayData.pageNumber + 1 >= displayData.totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </Button>
          </div>
        )}
      </section>
    </div>
  );
}
