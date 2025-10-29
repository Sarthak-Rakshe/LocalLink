import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import { Services } from "../../services/api.js";
import Button from "../../components/ui/Button.jsx";
import { Input, Label, HelpText } from "../../components/ui/Input.jsx";
import MapPicker from "../../components/map/MapPicker.jsx";

export default function EditService() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { user } = useAuth();
  const providerId = user?.id ?? user?.userId;

  const [form, setForm] = useState({
    serviceName: "",
    serviceDescription: "",
    serviceCategory: "",
    servicePricePerHour: "",
    latitude: "",
    longitude: "",
  });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const data = await Services.getById(id);
        if (!active) return;
        setForm({
          serviceName: data.serviceName ?? "",
          serviceDescription: data.serviceDescription ?? "",
          serviceCategory: data.serviceCategory ?? "",
          servicePricePerHour:
            data.servicePricePerHour != null
              ? String(data.servicePricePerHour)
              : "",
          latitude: data.latitude != null ? String(data.latitude) : "",
          longitude: data.longitude != null ? String(data.longitude) : "",
        });
      } catch (e) {
        setError(
          e?.response?.data?.message || e.message || "Failed to load service"
        );
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, [id]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      setSubmitting(true);
      const payload = {
        serviceName: form.serviceName.trim(),
        serviceDescription: form.serviceDescription.trim(),
        serviceCategory: form.serviceCategory.trim(),
        servicePricePerHour: Number(form.servicePricePerHour),
        serviceProviderId: Number(providerId),
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
      };
      await Services.update(id, payload);
      navigate("/services/manage", { replace: true });
    } catch (e2) {
      setError(
        e2?.response?.data?.message || e2.message || "Failed to update service"
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="p-4">Loading…</div>;

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-4 text-2xl font-semibold">Edit service</h1>
      {error && (
        <div className="mb-3 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}
      <form onSubmit={onSubmit} className="space-y-4">
        <div>
          <Label htmlFor="name">Name</Label>
          <Input
            id="name"
            value={form.serviceName}
            onChange={(e) =>
              setForm((f) => ({ ...f, serviceName: e.target.value }))
            }
            required
          />
        </div>
        <div>
          <Label htmlFor="category">Category</Label>
          <Input
            id="category"
            value={form.serviceCategory}
            onChange={(e) =>
              setForm((f) => ({ ...f, serviceCategory: e.target.value }))
            }
            required
          />
        </div>
        <div>
          <Label htmlFor="desc">Description</Label>
          <Input
            id="desc"
            value={form.serviceDescription}
            onChange={(e) =>
              setForm((f) => ({ ...f, serviceDescription: e.target.value }))
            }
            required
          />
        </div>
        <div>
          <Label htmlFor="price">Price per hour</Label>
          <Input
            id="price"
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            value={form.servicePricePerHour}
            onChange={(e) =>
              setForm((f) => ({ ...f, servicePricePerHour: e.target.value }))
            }
            required
          />
        </div>
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          <div>
            <Label htmlFor="lat">Latitude</Label>
            <Input
              id="lat"
              type="number"
              inputMode="decimal"
              value={form.latitude}
              onChange={(e) =>
                setForm((f) => ({ ...f, latitude: e.target.value }))
              }
              required
            />
          </div>
          <div>
            <Label htmlFor="lng">Longitude</Label>
            <Input
              id="lng"
              type="number"
              inputMode="decimal"
              value={form.longitude}
              onChange={(e) =>
                setForm((f) => ({ ...f, longitude: e.target.value }))
              }
              required
            />
          </div>
        </div>
        <MapPicker
          value={
            Number.isFinite(parseFloat(form.latitude)) &&
            Number.isFinite(parseFloat(form.longitude))
              ? {
                  lat: parseFloat(form.latitude),
                  lng: parseFloat(form.longitude),
                }
              : undefined
          }
          onChange={({ lat, lng }) =>
            setForm((f) => ({
              ...f,
              latitude: String(lat),
              longitude: String(lng),
            }))
          }
        />
        <HelpText>
          Only the service owner can update this service. Provider ID is
          validated by the backend.
        </HelpText>
        <div className="flex items-center gap-2">
          <Button type="submit" disabled={submitting}>
            {submitting ? "Saving…" : "Save changes"}
          </Button>
          <Button type="button" variant="outline" onClick={() => navigate(-1)}>
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
}
