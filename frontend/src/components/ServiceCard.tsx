import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Star, MapPin, DollarSign } from "lucide-react";
import { Link } from "react-router-dom";

interface ServiceCardProps {
  service: {
    serviceId: number | string;
    serviceName: string;
    serviceDescription?: string;
    serviceCategory?: string;
    servicePricePerHour?: number;
    distanceKm?: number;
    providerName?: string;
    reviewAggregate?: {
      averageRating?: number;
      totalReviews?: number;
    };
  };
}

export const ServiceCard = ({ service }: ServiceCardProps) => {
  const rating = service.reviewAggregate?.averageRating ?? 0;
  const reviewCount = service.reviewAggregate?.totalReviews ?? 0;

  return (
    <Card className="group overflow-hidden transition-smooth hover:shadow-lg hover:-translate-y-1">
      <CardContent className="p-6">
        <div className="mb-3 flex items-start justify-between">
          <div className="flex-1">
            <h3 className="font-semibold text-lg mb-1">{service.serviceName}</h3>
            {service.providerName && (
              <p className="text-sm text-muted-foreground">by {service.providerName}</p>
            )}
          </div>
          {service.serviceCategory && (
            <Badge variant="secondary" className="ml-2">
              {service.serviceCategory}
            </Badge>
          )}
        </div>

        {service.serviceDescription && (
          <p className="text-sm text-muted-foreground line-clamp-2 mb-4">
            {service.serviceDescription}
          </p>
        )}

        <div className="flex items-center gap-4 text-sm">
          {rating > 0 && (
            <div className="flex items-center gap-1 text-amber-500">
              <Star className="h-4 w-4 fill-current" />
              <span className="font-medium">{rating.toFixed(1)}</span>
              <span className="text-muted-foreground">({reviewCount})</span>
            </div>
          )}

          {service.distanceKm !== undefined && (
            <div className="flex items-center gap-1 text-muted-foreground">
              <MapPin className="h-4 w-4" />
              <span>{service.distanceKm.toFixed(1)} km</span>
            </div>
          )}
        </div>
      </CardContent>

      <CardFooter className="p-6 pt-0 flex items-center justify-between">
        {service.servicePricePerHour !== undefined && (
          <div className="flex items-center gap-1 font-semibold text-primary">
            <DollarSign className="h-4 w-4" />
            <span>{service.servicePricePerHour}</span>
            <span className="text-sm text-muted-foreground">/hr</span>
          </div>
        )}
        <Link to={`/services/${service.serviceId}`} className="ml-auto">
          <Button size="sm" className="transition-smooth">
            View Details
          </Button>
        </Link>
      </CardFooter>
    </Card>
  );
};
