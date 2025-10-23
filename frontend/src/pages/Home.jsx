import { useEffect, useMemo, useState } from "react";
import servicesApi from "../api/services.js";
import usersApi from "../api/users.js";
import ServiceTile from "../components/ServiceTile.jsx";
import CategoryTile from "../components/CategoryTile.jsx";
import ProviderTile from "../components/ProviderTile.jsx";
import CarouselRow from "../components/CarouselRow.jsx";

// In-memory module-level cache survives route navigations but resets on full refresh
let __cachedNearby = null; // { services: [...], userLocation: { latitude, longitude } }
let __hasLoadedOnce = false;

export default function Home() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [services, setServices] = useState([]);
  const [userLocation, setUserLocation] = useState(null);
  const [providers, setProviders] = useState([]);
  const categories = useMemo(() => {
    const set = new Set(services.map((s) => s.serviceCategory).filter(Boolean));
    return Array.from(set).slice(0, 4);
  }, [services]);

  useEffect(() => {
    let canceled = false;

    async function fetchData(position) {
      try {
        setLoading(true);
        setError("");

        const latitude = position.coords.latitude;
        const longitude = position.coords.longitude;
        if (!canceled) setUserLocation({ latitude, longitude });

        // Fetch a page of nearby services; grab more than 6 so we can sort by rating and then take top 6
        const pageRes = await servicesApi.getNearby({
          latitude,
          longitude,
          page: 0,
          size: 24,
          sortBy: "id",
          sortDir: "asc",
        });

        const items = Array.isArray(pageRes.content) ? pageRes.content : [];

        // Derive rating from aggregate included in DTO
        const withRatings = items.map((svc) => {
          const avg = svc?.reviewAggregate?.averageRating ?? 0;
          const count = svc?.reviewAggregate?.totalReviews;
          return {
            ...svc,
            _averageRating: Number(avg) || 0,
            _ratingCount: typeof count === "number" ? count : undefined,
          };
        });

        // Sort by rating desc, then take top 12 for a richer slider
        withRatings.sort(
          (a, b) => (b._averageRating || 0) - (a._averageRating || 0)
        );
        const topTwelve = withRatings.slice(0, 12);
        if (!canceled) setServices(topTwelve);

        // Fill cache so revisiting Home doesn't refetch until a full browser refresh
        __cachedNearby = {
          services: topTwelve,
          userLocation: { latitude, longitude },
        };
        __hasLoadedOnce = true;
      } catch (e) {
        console.error(e);
        if (!canceled)
          setError(
            e?.response?.data?.message ||
              "Failed to load nearby services. Please try again."
          );
      } finally {
        if (!canceled) setLoading(false);
      }
    }

    // If we already loaded once in this SPA session, use cache and skip fetch
    if (__hasLoadedOnce && __cachedNearby?.services) {
      setServices(__cachedNearby.services);
      setUserLocation(__cachedNearby.userLocation ?? null);
      setLoading(false);
      return;
    }

    // Request geolocation from the browser
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by your browser.");
      setLoading(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => fetchData(pos),
      (err) => {
        console.error(err);
        setError(
          "We couldn't access your location. Please allow location permissions and refresh."
        );
        setLoading(false);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );

    return () => {
      canceled = true;
    };
  }, []);

  // Fetch featured providers independently (limit 4)
  useEffect(() => {
    let canceled = false;
    (async () => {
      try {
        const res = await usersApi.getProviders({
          page: 0,
          size: 4,
          sortBy: "id",
          sortDir: "asc",
        });
        const content = res?.content ?? res?.data?.content ?? [];
        if (!canceled) setProviders(content);
      } catch {
        if (!canceled) setProviders([]);
      }
    })();
    return () => {
      canceled = true;
    };
  }, []);

  return (
    <div>
      <div className="mb-6">
        <h1>LocalLink</h1>
        <p className="text-gray-600 mt-2">
          Discover top-rated local services near you.
        </p>
      </div>

      {loading && <div className="text-gray-600">Loading nearby services…</div>}

      {!loading && error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-red-700">
          {error}
        </div>
      )}

      {!loading && !error && services.length === 0 && (
        <div className="rounded-lg border border-gray-200 bg-white p-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-gray-600 m-0">No nearby services found.</p>
          </div>
        </div>
      )}

      {!loading && !error && services.length > 0 && (
        <>
          <CarouselRow title="Top near you" itemsPerPage={4}>
            {services.map((svc) => (
              <ServiceTile
                key={svc.serviceId}
                service={svc}
                linkState={{ from: "home" }}
              />
            ))}
          </CarouselRow>

          {categories.length > 0 && (
            <CarouselRow title="Popular categories" itemsPerPage={4}>
              {categories.map((cat) => (
                <CategoryTile key={cat} category={cat} />
              ))}
            </CarouselRow>
          )}

          {providers.length > 0 && (
            <CarouselRow title="Featured providers" itemsPerPage={4}>
              {providers.map((p, idx) => (
                <ProviderTile key={p.id || p.name || idx} provider={p} />
              ))}
            </CarouselRow>
          )}
        </>
      )}
    </div>
  );
}
