import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { serviceService } from "../services/serviceService";
import { useAuth } from "../context/AuthContext";
import Navbar from "../components/layout/Navbar";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Plus, Edit, Trash2, IndianRupee } from "lucide-react";
import { formatINR } from "../lib/currency";
import { toast } from "sonner";

const MyServicesPage = () => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const fetchServices = async () => {
    setLoading(true);
    try {
      const filters = { userId: user?.userId };
      const response = await serviceService.getServices(
        0,
        50,
        "id",
        "asc",
        filters
      );
      setServices(response.content || []);
    } catch (error) {
      toast.error("Failed to load services");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.userId) {
      fetchServices();
    }
  }, [user]);

  const handleDelete = async (serviceId) => {
    if (!confirm("Are you sure you want to delete this service?")) return;

    try {
      await serviceService.deleteService(serviceId);
      toast.success("Service deleted successfully");
      fetchServices();
    } catch (error) {
      toast.error("Failed to delete service");
      console.error(error);
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="mb-2 text-3xl font-bold">My Services</h1>
            <p className="text-muted-foreground">
              Manage your service listings
            </p>
          </div>
          <Button onClick={() => navigate("/my-services/create")}>
            <Plus className="mr-2 h-4 w-4" />
            Add Service
          </Button>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          </div>
        ) : services.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-16 text-center">
              <Plus className="mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="mb-2 text-lg font-semibold">No services yet</h3>
              <p className="mb-4 text-muted-foreground">
                Create your first service to start receiving bookings
              </p>
              <Button onClick={() => navigate("/my-services/create")}>
                <Plus className="mr-2 h-4 w-4" />
                Create Service
              </Button>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {services.map((service) => (
              <Card key={service.serviceId}>
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
                    <p className="text-sm text-muted-foreground">
                      Category: {service.serviceCategory}
                    </p>
                    <p className="text-lg font-semibold text-primary flex items-center">
                      <IndianRupee className="mr-1 h-4 w-4" />
                      {formatINR(service.servicePricePerHour)} / hour
                    </p>
                  </div>
                  <div className="mt-4 flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      className="flex-1"
                      onClick={() =>
                        navigate(`/my-services/edit/${service.serviceId}`)
                      }
                    >
                      <Edit className="mr-2 h-4 w-4" />
                      Edit
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      className="text-destructive hover:bg-destructive hover:text-destructive-foreground"
                      onClick={() => handleDelete(service.serviceId)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default MyServicesPage;
