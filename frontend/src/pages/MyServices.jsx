import { useEffect, useMemo, useState } from "react";
import servicesApi from "../api/services";
import { getStoredUser } from "../api/session";
import { Card, CardBody } from "../components/ui/Card.jsx";
import Input from "../components/ui/Input.jsx";
import Button from "../components/ui/Button.jsx";
import MapPicker from "../components/ui/MapPicker.jsx";

const emptyForm = {
  serviceName: "",
  serviceDescription: "",
  serviceCategory: "",
  servicePricePerHour: "",
  latitude: "",
  longitude: "",
};

export default function MyServices() {
  const user = useMemo(() => getStoredUser(), []);
  const providerId = user?.userId;

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [showMap, setShowMap] = useState(false);

  const load = async () => {
    if (!providerId) return;
    setLoading(true);
    setError("");
    try {
      const res = await servicesApi.byProvider({
        providerId,
        page: 0,
        size: 50,
      });
      setItems(res?.content ?? []);
    } catch (e) {
      setError(e?.response?.data?.message || "Failed to fetch your services");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [providerId]);

  const startAdd = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowForm(true);
  };

  const startEdit = (svc) => {
    setEditingId(svc.serviceId);
    setForm({
      serviceName: svc.serviceName || "",
      serviceDescription: svc.serviceDescription || "",
      serviceCategory: svc.serviceCategory || "",
      servicePricePerHour: svc.servicePricePerHour ?? "",
      latitude: svc.latitude ?? "",
      longitude: svc.longitude ?? "",
    });
    setShowForm(true);
  };

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const payload = {
        ...form,
        servicePricePerHour: Number(form.servicePricePerHour),
        serviceProviderId: providerId,
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
      };
      if (editingId) {
        await servicesApi.update(editingId, payload);
      } else {
        await servicesApi.create(payload);
      }
      setForm(emptyForm);
      setEditingId(null);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Save failed");
    }
  };

  const onDelete = async (id) => {
    if (!window.confirm("Delete this service?")) return;
    try {
      await servicesApi.remove(id);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Delete failed");
    }
  };

  return (
    <div>
      <div className="mb-4">
        <h1>My Services</h1>
        <p className="text-gray-600 mt-2">
          Add, update, or delete your listed services.
        </p>
      </div>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-red-700 mb-3">
          {error}
        </div>
      )}

      <div className="flex items-center justify-between mb-3">
        <h3 className="m-0">Your listings</h3>
        <div className="flex gap-2">
          {!showForm && (
            <Button variant="ghost" onClick={startAdd}>
              Create Service
            </Button>
          )}
          {showForm && (
            <Button
              variant="ghost"
              onClick={() => {
                setShowForm(false);
                setEditingId(null);
                setForm(emptyForm);
                setShowMap(false);
              }}
            >
              Close Form
            </Button>
          )}
        </div>
      </div>

      {showForm && (
        <Card className="mb-6">
          <CardBody>
            <h3 className="mb-2">
              {editingId ? "Update service" : "Create service"}
            </h3>
            <form
              onSubmit={onSubmit}
              className="grid grid-cols-1 sm:grid-cols-2 gap-3"
            >
              <Input
                label="Service name"
                id="serviceName"
                name="serviceName"
                value={form.serviceName}
                onChange={onChange}
                required
              />
              <Input
                label="Category"
                id="serviceCategory"
                name="serviceCategory"
                value={form.serviceCategory}
                onChange={onChange}
                required
              />
              <Input
                label="Price per hour (INR)"
                id="servicePricePerHour"
                name="servicePricePerHour"
                type="number"
                min="0"
                step="1"
                value={form.servicePricePerHour}
                onChange={onChange}
                required
              />
              <div className="sm:col-span-2 grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Input
                  label="Latitude"
                  id="latitude"
                  name="latitude"
                  type="number"
                  step="0.000001"
                  value={form.latitude}
                  onChange={onChange}
                  required
                />
                <Input
                  label="Longitude"
                  id="longitude"
                  name="longitude"
                  type="number"
                  step="0.000001"
                  value={form.longitude}
                  onChange={onChange}
                  required
                />
              </div>

              <label
                className="label sm:col-span-2"
                htmlFor="serviceDescription"
              >
                Description
                <textarea
                  id="serviceDescription"
                  name="serviceDescription"
                  className="input mt-1 h-24"
                  value={form.serviceDescription}
                  onChange={onChange}
                  required
                />
              </label>

              <div className="sm:col-span-2">
                <Button
                  variant="ghost"
                  type="button"
                  onClick={() => setShowMap((v) => !v)}
                >
                  {showMap ? "Hide Map" : "Pick on Map"}
                </Button>
                {showMap && (
                  <div className="mt-3">
                    {/* MapPicker is dynamically loaded below to select coordinates */}
                    <MapPicker
                      value={{
                        latitude: Number(form.latitude) || undefined,
                        longitude: Number(form.longitude) || undefined,
                      }}
                      onChange={({ latitude, longitude }) =>
                        setForm((f) => ({
                          ...f,
                          latitude: latitude,
                          longitude: longitude,
                        }))
                      }
                    />
                  </div>
                )}
              </div>

              <div className="sm:col-span-2 flex gap-2 justify-end mt-1">
                <Button
                  type="button"
                  variant="ghost"
                  onClick={() => {
                    setShowForm(false);
                    setEditingId(null);
                    setForm(emptyForm);
                    setShowMap(false);
                  }}
                >
                  Cancel
                </Button>
                <Button type="submit">{editingId ? "Update" : "Create"}</Button>
              </div>
            </form>
          </CardBody>
        </Card>
      )}

      {/* List section header moved above with form toggle */}

      {loading ? (
        <div className="text-gray-600">Loading…</div>
      ) : items.length === 0 ? (
        <div className="text-gray-600">You have no services yet.</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map((svc) => (
            <Card key={svc.serviceId}>
              <CardBody>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="font-semibold">{svc.serviceName}</div>
                    <div className="text-xs text-gray-500 mt-1">
                      {svc.serviceCategory} • ₹{svc.servicePricePerHour}/hr
                    </div>
                    <p className="mt-2 text-sm text-gray-700 line-clamp-3">
                      {svc.serviceDescription}
                    </p>
                  </div>
                </div>
                <div className="mt-3 flex gap-2 justify-end">
                  <Button variant="ghost" onClick={() => startEdit(svc)}>
                    Edit
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => onDelete(svc.serviceId)}
                    title="Delete"
                  >
                    Delete
                  </Button>
                </div>
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
