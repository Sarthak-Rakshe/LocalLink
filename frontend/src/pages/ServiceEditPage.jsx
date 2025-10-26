import React, { useEffect, useState, lazy, Suspense } from "react";
import Navbar from "../components/layout/Navbar";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "../components/ui/card";
import { Label } from "../components/ui/label";
import { Input } from "../components/ui/input";
import { Button } from "../components/ui/button";
import { toast } from "sonner";
import { serviceService } from "../services/serviceService";
import { useNavigate, useParams } from "react-router-dom";
const LazyMapPicker = lazy(() => import("../components/map/MapPicker"));

const ServiceEditPage = () => {
  const { serviceId } = useParams();
  const [form, setForm] = useState({
    serviceName: "",
    serviceDescription: "",
    serviceCategory: "",
    servicePricePerHour: "",
    latitude: "",
    longitude: "",
  });
  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);
  const [loadingInitial, setLoadingInitial] = useState(true);
  const [showMap, setShowMap] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        const service = await serviceService.getServiceById(serviceId);
        setForm({
          serviceName: service.serviceName || "",
          serviceDescription: service.serviceDescription || "",
          serviceCategory: service.serviceCategory || "",
          servicePricePerHour: service.servicePricePerHour ?? "",
          latitude: service.latitude ?? "",
          longitude: service.longitude ?? "",
        });
      } catch (err) {
        toast.error("Failed to load service");
      } finally {
        setLoadingInitial(false);
      }
    })();
  }, [serviceId]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = {
        serviceName: form.serviceName,
        serviceDescription: form.serviceDescription,
        serviceCategory: form.serviceCategory,
        servicePricePerHour: parseFloat(form.servicePricePerHour),
        latitude: form.latitude === "" ? null : parseFloat(form.latitude),
        longitude: form.longitude === "" ? null : parseFloat(form.longitude),
      };
      await serviceService.updateService(serviceId, payload);
      toast.success("Service updated");
      navigate("/my-services");
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to update service");
    } finally {
      setLoading(false);
    }
  };

  if (loadingInitial) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <Card>
          <CardHeader>
            <CardTitle>Edit Service</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="serviceName">Name</Label>
                <Input
                  id="serviceName"
                  name="serviceName"
                  value={form.serviceName}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="serviceDescription">Description</Label>
                <Input
                  id="serviceDescription"
                  name="serviceDescription"
                  value={form.serviceDescription}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="serviceCategory">Category</Label>
                <Input
                  id="serviceCategory"
                  name="serviceCategory"
                  value={form.serviceCategory}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="servicePricePerHour">Price/hour</Label>
                <Input
                  id="servicePricePerHour"
                  name="servicePricePerHour"
                  type="number"
                  step="0.01"
                  value={form.servicePricePerHour}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="latitude">Latitude</Label>
                  <Input
                    id="latitude"
                    name="latitude"
                    type="number"
                    step="0.000001"
                    value={form.latitude}
                    onChange={handleChange}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="longitude">Longitude</Label>
                  <Input
                    id="longitude"
                    name="longitude"
                    type="number"
                    step="0.000001"
                    value={form.longitude}
                    onChange={handleChange}
                  />
                </div>
              </div>
              {/* Map section (consistent layout with create page) */}
              {showMap ? (
                <div className="space-y-2">
                  <Suspense
                    fallback={
                      <div className="flex h-80 items-center justify-center rounded-md border">
                        Loading map…
                      </div>
                    }
                  >
                    <LazyMapPicker
                      value={{
                        lat:
                          form.latitude === ""
                            ? undefined
                            : Number(form.latitude),
                        lng:
                          form.longitude === ""
                            ? undefined
                            : Number(form.longitude),
                      }}
                      onChange={({ lat, lng }) =>
                        setForm((f) => ({
                          ...f,
                          latitude: String(lat),
                          longitude: String(lng),
                        }))
                      }
                      height={320}
                      autoLocate={true}
                    />
                  </Suspense>
                  <div className="flex justify-end">
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={() => setShowMap(false)}
                    >
                      Hide map
                    </Button>
                  </div>
                </div>
              ) : (
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => setShowMap(true)}
                >
                  Pick location on map
                </Button>
              )}
              {/* Map picker is shown only when toggled above */}
              <div>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    if (!("geolocation" in navigator)) {
                      toast.error(
                        "Geolocation is not supported by this browser"
                      );
                      return;
                    }
                    setLocating(true);
                    navigator.geolocation.getCurrentPosition(
                      (pos) => {
                        const { latitude, longitude } = pos.coords;
                        setForm((f) => ({
                          ...f,
                          latitude: latitude.toFixed(6),
                          longitude: longitude.toFixed(6),
                        }));
                        setLocating(false);
                        toast.success("Location captured");
                      },
                      (err) => {
                        setLocating(false);
                        toast.error(err.message || "Failed to get location");
                      },
                      {
                        enableHighAccuracy: true,
                        timeout: 10000,
                        maximumAge: 0,
                      }
                    );
                  }}
                  disabled={locating}
                >
                  {locating ? "Locating..." : "Use my current location"}
                </Button>
              </div>
              <div className="flex gap-2">
                <Button type="submit" disabled={loading}>
                  {loading ? "Saving..." : "Save"}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate("/my-services")}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </main>
    </div>
  );
};

export default ServiceEditPage;
