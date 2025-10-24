import { useState, useEffect } from "react";
import { Navbar } from "@/components/Navbar";
import { ServiceCard } from "@/components/ServiceCard";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { serviceApi } from "@/lib/api";
import { ServiceModel } from "@/lib/models";
import { Search, MapPin, Loader2 } from "lucide-react";
import { toast } from "@/hooks/use-toast";

const CATEGORIES = [
  "All",
  "Plumbing",
  "Electrical",
  "Carpentry",
  "Cleaning",
  "Gardening",
  "Painting",
  "Moving",
  "Tutoring",
  "Beauty",
  "Other",
];

export default function Explore() {
  const [services, setServices] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [useLocation, setUseLocation] = useState(false);
  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null);

  useEffect(() => {
    fetchServices();
  }, [page, selectedCategory, useLocation]);

  const fetchServices = async () => {
    setIsLoading(true);
    try {
      let response;
      
      if (useLocation && userLocation) {
        response = await serviceApi.getNearbyServices({
          userLatitude: userLocation.lat,
          userLongitude: userLocation.lng,
          page,
          size: 12,
        });
      } else if (selectedCategory !== "All") {
        response = await serviceApi.getServicesByCategory(selectedCategory, {
          page,
          size: 12,
        });
      } else {
        response = await serviceApi.getAllServices({
          page,
          size: 12,
        });
      }

      setServices(response.content || []);
      setTotalPages(response.totalPages || 1);
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Failed to load services",
        description: error.message,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleNearbySearch = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          });
          setUseLocation(true);
          setPage(0);
          toast({
            title: "Location enabled",
            description: "Showing services near you",
          });
        },
        () => {
          toast({
            variant: "destructive",
            title: "Location access denied",
            description: "Please enable location to find nearby services",
          });
        }
      );
    }
  };

  const filteredServices = services.filter((service) =>
    service.serviceName?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      
      <div className="container py-8">
        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-2">Discover Local Services</h1>
          <p className="text-muted-foreground">Find the perfect service provider near you</p>
        </div>

        <div className="flex flex-col md:flex-row gap-4 mb-8">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search services..."
              className="pl-10"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          
          <Select value={selectedCategory} onValueChange={setSelectedCategory}>
            <SelectTrigger className="w-full md:w-48">
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent>
              {CATEGORIES.map((cat) => (
                <SelectItem key={cat} value={cat}>
                  {cat}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Button variant="outline" onClick={handleNearbySearch}>
            <MapPin className="mr-2 h-4 w-4" />
            Near Me
          </Button>
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        ) : filteredServices.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-lg text-muted-foreground">No services found</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
              {filteredServices.map((service) => (
                <ServiceCard key={service.serviceId} service={service} />
              ))}
            </div>

            {totalPages > 1 && (
              <div className="flex justify-center gap-2">
                <Button
                  variant="outline"
                  onClick={() => setPage(Math.max(0, page - 1))}
                  disabled={page === 0}
                >
                  Previous
                </Button>
                <span className="flex items-center px-4">
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
      </div>
    </div>
  );
}
