import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import servicesApi from "../api/services";
import ServiceCard from "../components/ServiceCard.jsx";
import Button from "../components/ui/Button.jsx";

export default function Services() {
  const location = useLocation();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [items, setItems] = useState([]);
  const [pageNumber, setPageNumber] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [size, setSize] = useState(12);
  const [userLocation, setUserLocation] = useState(null);
  const [providerId, setProviderId] = useState("");
  const [allCategories, setAllCategories] = useState([]);

  // Filters
  const [category, setCategory] = useState("");
  const [sortBy, setSortBy] = useState("name");
  const [sortDir, setSortDir] = useState("asc");
  // Server-side only filtering: we rely on backend (category endpoint) and sort/paging

  // Category options: prefer the pre-fetched full set; fallback to what's in the current page
  const categories = useMemo(() => {
    if (allCategories.length > 0) return allCategories;
    const set = new Set(items.map((s) => s.serviceCategory).filter(Boolean));
    return Array.from(set).sort();
  }, [allCategories, items]);

  const load = async (page = 0) => {
    setLoading(true);
    setError("");
    try {
      // Server-side filtering only
      let res;
      if (providerId) {
        res = await servicesApi.byProvider({
          providerId,
          page,
          size,
          sortBy,
          sortDir,
        });
      } else if (category) {
        res = await servicesApi.byCategory({
          category,
          page,
          size,
          sortBy,
          sortDir,
        });
      } else {
        res = await servicesApi.list({ page, size, sortBy, sortDir });
      }
      const content = res?.content ?? [];

      // Derive rating from reviewAggregate in DTO
      const enriched = content.map((s) => {
        const avg = s?.reviewAggregate?.averageRating ?? 0;
        const count = s?.reviewAggregate?.totalReviews;
        return {
          ...s,
          _averageRating: Number(avg) || 0,
          _ratingCount: typeof count === "number" ? count : undefined,
        };
      });

      setItems(enriched);
      setPageNumber(res.pageNumber ?? 0);
      setTotalPages(res.totalPages ?? 0);
    } catch (e) {
      setError(e?.response?.data?.message || "Failed to load services");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [size, sortBy, sortDir, providerId, category]);

  // Fetch categories once (unfiltered) so the dropdown doesn't collapse after filtering
  useEffect(() => {
    let canceled = false;
    (async () => {
      try {
        const res = await servicesApi.list({
          page: 0,
          size: 200,
          sortBy: "name",
          sortDir: "asc",
        });
        const content = res?.content ?? [];
        const set = new Set(
          content.map((s) => s.serviceCategory).filter(Boolean)
        );
        const list = Array.from(set).sort();
        if (!canceled) setAllCategories(list);
      } catch {
        // ignore; categories will fallback to current page
      }
    })();
    return () => {
      canceled = true;
    };
  }, []);

  // Parse query params for deep links like /services?category=... or ?providerId=...
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const qCat = params.get("category") || "";
    const qProv = params.get("providerId") || "";
    setCategory(qCat);
    setProviderId(qProv);
    // load will be triggered by deps effect above
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.search]);

  // Get user geolocation once so we can show distance on all cards
  useEffect(() => {
    if (!navigator.geolocation) return;
    let canceled = false;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        if (canceled) return;
        setUserLocation({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
        });
      },
      () => {
        // Silently ignore; distance just won't show
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );
    return () => {
      canceled = true;
    };
  }, []);

  const applyFilters = () => {
    const params = new URLSearchParams();
    if (category) params.set("category", category);
    if (providerId) params.set("providerId", providerId);
    navigate({ pathname: "/services", search: params.toString() });
    load(0);
  };

  return (
    <div>
      <div className="mb-4">
        <h1>Services</h1>
        <p className="text-gray-600 mt-2">
          Browse all available services. Use filters to narrow results.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-4">
        <label className="label">
          Category
          <select
            className="input mt-1"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            <option value="">All</option>
            {categories.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </label>
        <label className="label">
          Sort field
          <select
            className="input mt-1"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="name">Name</option>
            <option value="price">Price</option>
            <option value="id">Created (ID)</option>
            <option value="category">Category</option>
            <option value="provider">Provider</option>
          </select>
        </label>
        <label className="label">
          Sort direction
          <select
            className="input mt-1"
            value={sortDir}
            onChange={(e) => setSortDir(e.target.value)}
          >
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
          </select>
        </label>
        <label className="label">
          Per page
          <select
            className="input mt-1"
            value={size}
            onChange={(e) => setSize(Number(e.target.value))}
          >
            {[6, 12, 18, 24].map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="mb-4 flex gap-2 justify-end">
        <Button
          variant="ghost"
          onClick={() => {
            setCategory("");
            setProviderId("");
            navigate({ pathname: "/services" });
          }}
        >
          Clear
        </Button>
        <Button onClick={applyFilters}>Apply</Button>
      </div>

      {loading && <div className="text-gray-600">Loading services…</div>}
      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-red-700 mb-3">
          {error}
        </div>
      )}

      {!loading && !error && items.length === 0 && (
        <div className="text-gray-600">
          No services found for the current filters.
        </div>
      )}

      {!loading && !error && items.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 items-stretch">
          {items.map((svc) => (
            <div key={svc.serviceId} className="h-full">
              <ServiceCard
                service={{ ...svc }}
                userLocation={userLocation}
                linkState={{ from: "services" }}
              />
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      <div className="mt-6 flex items-center justify-between">
        <div className="text-sm text-gray-500">
          Page {pageNumber + 1} of {Math.max(totalPages, 1)}
        </div>
        <div className="flex gap-2">
          <Button
            variant="ghost"
            disabled={pageNumber <= 0 || loading}
            onClick={() => load(pageNumber - 1)}
          >
            Previous
          </Button>
          <Button
            variant="ghost"
            disabled={pageNumber >= totalPages - 1 || loading}
            onClick={() => load(pageNumber + 1)}
          >
            Next
          </Button>
        </div>
      </div>
    </div>
  );
}
