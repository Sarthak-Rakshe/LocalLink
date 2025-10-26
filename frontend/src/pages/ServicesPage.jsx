import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { serviceService } from "../services/serviceService";
import Navbar from "../components/layout/Navbar";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Search, MapPin, IndianRupee, Star, Navigation } from "lucide-react";
import { formatINR } from "../lib/currency";
import { toast } from "sonner";

const ServicesPage = () => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({
    serviceName: "",
    category: "",
    minPrice: "",
    maxPrice: "",
  });
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  // Nearby features moved to dedicated page; keep this page focused on all services

  const fetchServices = async ({ pageOverride, filtersOverride } = {}) => {
    setLoading(true);
    try {
      const activeFilters = filtersOverride ?? filters;
      const filterData = {};
      if (activeFilters.serviceName)
        filterData.serviceName = activeFilters.serviceName;
      if (activeFilters.category) filterData.category = activeFilters.category;
      if (activeFilters.minPrice !== "")
        filterData.minPrice = parseFloat(activeFilters.minPrice);
      if (activeFilters.maxPrice !== "")
        filterData.maxPrice = parseFloat(activeFilters.maxPrice);

      const currentPage =
        typeof pageOverride === "number" ? pageOverride : page;
      const response = await serviceService.getServices(
        currentPage,
        12,
        "id",
        "asc",
        filterData
      );
      setServices(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (error) {
      toast.error("Failed to load services");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchServices();
  }, [page]);

  // Nearby handling removed from Services page

  const handleFilterChange = (e) => {
    setFilters({ ...filters, [e.target.name]: e.target.value });
  };

  const handleSearch = () => {
    const snapshot = { ...filters };
    setPage(0);
    // Call with explicit overrides to avoid stale state values
    fetchServices({ pageOverride: 0, filtersOverride: snapshot });
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="mb-2 text-3xl font-bold">Browse Services</h1>
          <p className="text-muted-foreground">
            Discover local services in your area
          </p>
        </div>

        {/* Filters */}
        <Card className="mb-8">
          <CardHeader>
            <CardTitle>Search & Filter</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-4">
              <div className="space-y-2">
                <Label htmlFor="serviceName">Service Name</Label>
                <Input
                  id="serviceName"
                  name="serviceName"
                  placeholder="e.g., Plumbing"
                  value={filters.serviceName}
                  onChange={handleFilterChange}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="category">Category</Label>
                <Input
                  id="category"
                  name="category"
                  placeholder="e.g., Home Repair"
                  value={filters.category}
                  onChange={handleFilterChange}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="minPrice">Min Price</Label>
                <Input
                  id="minPrice"
                  name="minPrice"
                  type="number"
                  placeholder="0"
                  value={filters.minPrice}
                  onChange={handleFilterChange}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="maxPrice">Max Price</Label>
                <Input
                  id="maxPrice"
                  name="maxPrice"
                  type="number"
                  placeholder="1000"
                  value={filters.maxPrice}
                  onChange={handleFilterChange}
                />
              </div>
            </div>
            <div className="mt-4 flex flex-col gap-2 md:flex-row md:items-center">
              <Button onClick={handleSearch} className="w-full md:w-auto">
                <Search className="mr-2 h-4 w-4" />
                Search Services
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Nearby section removed; use the dedicated Nearby page via navbar */}

        {/* Services Grid */}
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          </div>
        ) : services.length === 0 ? (
          <div className="flex flex-col items-center justify-center rounded-lg border bg-card p-12 text-center">
            <Search className="mb-4 h-12 w-12 text-muted-foreground" />
            <h3 className="mb-2 text-lg font-semibold">No services found</h3>
            <p className="text-muted-foreground">
              Try adjusting your search filters
            </p>
          </div>
        ) : (
          <>
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {services.map((service) => (
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
                      {(() => {
                        const apiKm =
                          service.distanceInKm ??
                          service.distanceKM ??
                          service.distance;
                        if (typeof apiKm === "number") {
                          const km = Math.round(apiKm * 100) / 100;
                          return (
                            <div className="flex items-center text-sm text-muted-foreground">
                              <Navigation className="mr-2 h-4 w-4" />
                              {`${km} km away`}
                            </div>
                          );
                        }
                        return null;
                      })()}
                      {service.reviewAggregate && (
                        <div className="flex items-center text-sm text-muted-foreground">
                          <Star className="mr-1 h-4 w-4 fill-yellow-400 text-yellow-400" />
                          {service.reviewAggregate.averageRating?.toFixed(1) ||
                            "N/A"}{" "}
                          ({service.reviewAggregate.reviewCount || 0} reviews)
                        </div>
                      )}
                    </div>
                    <Link to={`/services/${service.serviceId}`}>
                      <Button className="mt-4 w-full">View Details</Button>
                    </Link>
                  </CardContent>
                </Card>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-8 flex items-center justify-center gap-2">
                <Button
                  variant="outline"
                  onClick={() => setPage(Math.max(0, page - 1))}
                  disabled={page === 0}
                >
                  Previous
                </Button>
                <span className="text-sm text-muted-foreground">
                  Page {page + 1} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                  disabled={page >= totalPages - 1}
                >
                  Next
                </Button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
};

export default ServicesPage;
