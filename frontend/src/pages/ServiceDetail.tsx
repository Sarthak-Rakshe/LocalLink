import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { serviceApi, reviewApi } from "@/lib/api";
import { Star, MapPin, DollarSign, Calendar, Loader2 } from "lucide-react";
import { toast } from "@/hooks/use-toast";
import { useAuth } from "@/lib/auth";

export default function ServiceDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const [service, setService] = useState<any>(null);
  const [reviews, setReviews] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchServiceDetails();
  }, [id]);

  const fetchServiceDetails = async () => {
    try {
      const [serviceData, reviewsData] = await Promise.all([
        serviceApi.getService(Number(id)),
        reviewApi.getServiceReviews(Number(id), { page: 0, size: 5 }),
      ]);
      setService(serviceData);
      setReviews(reviewsData.content || []);
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Failed to load service",
        description: error.message,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleBookNow = () => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }
    navigate(`/booking/${id}`);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="flex items-center justify-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      </div>
    );
  }

  if (!service) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container py-20 text-center">
          <p>Service not found</p>
        </div>
      </div>
    );
  }

  const rating = service.reviewAggregate?.averageRating ?? 0;
  const reviewCount = service.reviewAggregate?.totalReviews ?? 0;

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      
      <div className="container py-8">
        <div className="grid lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="text-3xl mb-2">{service.serviceName}</CardTitle>
                    {service.serviceCategory && (
                      <Badge variant="secondary">{service.serviceCategory}</Badge>
                    )}
                  </div>
                  {rating > 0 && (
                    <div className="flex items-center gap-1 text-amber-500">
                      <Star className="h-5 w-5 fill-current" />
                      <span className="font-semibold text-lg">{rating.toFixed(1)}</span>
                      <span className="text-muted-foreground">({reviewCount})</span>
                    </div>
                  )}
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-muted-foreground">{service.serviceDescription}</p>
                
                <div className="flex items-center gap-4 text-sm">
                  {service.servicePricePerHour && (
                    <div className="flex items-center gap-1 font-semibold text-primary text-lg">
                      <DollarSign className="h-5 w-5" />
                      <span>{service.servicePricePerHour}/hr</span>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Reviews</CardTitle>
              </CardHeader>
              <CardContent>
                {reviews.length > 0 ? (
                  <div className="space-y-4">
                    {reviews.map((review) => (
                      <div key={review.reviewId} className="border-b pb-4 last:border-0">
                        <div className="flex items-center gap-2 mb-2">
                          <div className="flex items-center gap-1 text-amber-500">
                            <Star className="h-4 w-4 fill-current" />
                            <span className="font-medium">{review.rating}</span>
                          </div>
                          <span className="text-sm text-muted-foreground">
                            {new Date(review.createdAt).toLocaleDateString()}
                          </span>
                        </div>
                        <p className="text-sm">{review.comment}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-muted-foreground">No reviews yet</p>
                )}
              </CardContent>
            </Card>
          </div>

          <div>
            <Card className="sticky top-20">
              <CardHeader>
                <CardTitle>Book this service</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <Button className="w-full" size="lg" onClick={handleBookNow}>
                  <Calendar className="mr-2 h-5 w-5" />
                  Book Now
                </Button>
                <p className="text-xs text-muted-foreground text-center">
                  Select date and time on the next page
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
