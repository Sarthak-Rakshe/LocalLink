import React, { useEffect, useState } from "react";
import Navbar from "../components/layout/Navbar";
import { serviceService } from "../services/serviceService";
import { toast } from "sonner";
import { haversineKm } from "../lib/geo";
import { formatINR } from "../lib/currency";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card";
import { Button } from "../components/ui/button";
import { IndianRupee, MapPin, Navigation } from "lucide-react";
import { Link } from "react-router-dom";

const NearbyServicesPage = () => {
  const [userLoc, setUserLoc] = useState(null); // { lat, lng }
  const [loadingLoc, setLoadingLoc] = useState(false);
  const [nearby, setNearby] = useState({
    items: [],
    loading: false,
    loaded: false,
  });

  // Try to capture location on mount
  useEffect(() => {
    if (!userLoc) captureLocation();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const captureLocation = () => {
    if (!("geolocation" in navigator)) {
      toast.error("Geolocation is not supported by this browser");
      return;
    }
    setLoadingLoc(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        const loc = {
          lat: Number(latitude.toFixed(6)),
          lng: Number(longitude.toFixed(6)),
        };
        setUserLoc(loc);
        setLoadingLoc(false);
        toast.success("Location captured");
      },
      (err) => {
        setLoadingLoc(false);
        toast.error(err.message || "Failed to get location");
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  };

  // Load nearby when we have user location
  useEffect(() => {
    const loadNearby = async () => {
      if (!userLoc) return;
      setNearby((s) => ({ ...s, loading: true }));
      try {
        const res = await serviceService.getNearbyServices(
          userLoc.lat,
          userLoc.lng,
          0,
          24
        );
        const items = res?.content || res?.items || [];
        setNearby({ items, loading: false, loaded: true });
      } catch (e) {
        console.error(e);
        toast.error("Failed to load nearby services");
        setNearby((s) => ({ ...s, loading: false, loaded: true }));
      }
    };
    loadNearby();
  }, [userLoc]);

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-6">
          <h1 className="mb-1 text-3xl font-bold">Nearby Services</h1>
          <p className="text-sm text-muted-foreground">
            We use your current location to find services within your area.
          </p>
        </div>

        <div className="mb-6 flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
          <span>
            {userLoc
              ? `Your location: ${userLoc.lat}, ${userLoc.lng}`
              : "Location not set yet"}
          </span>
          <Button
            type="button"
            variant="secondary"
            onClick={captureLocation}
            disabled={loadingLoc}
          >
            <Navigation className="mr-2 h-4 w-4" />
            {loadingLoc
              ? "Locating..."
              : userLoc
              ? "Update my location"
              : "Use my location"}
          </Button>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Results</CardTitle>
            <CardDescription>
              {userLoc
                ? "Sorted by distance from your location"
                : "Capture your location to see nearby services"}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {nearby.loading ? (
              <div className="flex items-center justify-center py-12">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
              </div>
            ) : !userLoc ? (
              <p className="text-sm text-muted-foreground">
                Click "Use my location" to load nearby services.
              </p>
            ) : nearby.items.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                No nearby services found
              </p>
            ) : (
              <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                {nearby.items.map((service) => {
                  const apiKm =
                    service.distanceInKm ??
                    service.distanceKM ??
                    service.distance;
                  const km =
                    typeof apiKm === "number"
                      ? Math.round(apiKm * 100) / 100
                      : haversineKm(
                          userLoc.lat,
                          userLoc.lng,
                          service.latitude,
                          service.longitude
                        );
                  return (
                    <Card
                      key={service.serviceId}
                      className="transition-all hover:shadow-lg"
                    >
                      <CardHeader>
                        <CardTitle className="line-clamp-1">
                          {service.serviceName}
                        </CardTitle>
                        <CardDescription className="line-clamp-2">
                          {service.serviceDescription}
                        </CardDescription>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-2">
                          <div className="flex items-center text-sm text-muted-foreground">
                            <MapPin className="mr-2 h-4 w-4" />
                            {service.serviceCategory}
                          </div>
                          <div className="flex items-center text-sm font-semibold text-primary">
                            <IndianRupee className="mr-1 h-4 w-4" />
                            {formatINR(service.servicePricePerHour)} / hour
                          </div>
                          <div className="flex items-center text-sm text-muted-foreground">
                            <Navigation className="mr-2 h-4 w-4" />
                            {km == null
                              ? "Distance unavailable"
                              : `${km} km away`}
                          </div>
                        </div>
                        <Link to={`/services/${service.serviceId}`}>
                          <Button className="mt-4 w-full">View Details</Button>
                        </Link>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  );
};

export default NearbyServicesPage;
