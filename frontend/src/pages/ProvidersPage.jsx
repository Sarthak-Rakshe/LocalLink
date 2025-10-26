import React, { useState, useEffect } from 'react';
import { userService } from '../services/userService';
import Navbar from '../components/layout/Navbar';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Button } from '../components/ui/button';
import { MapPin, Mail, Phone, Star } from 'lucide-react';
import { toast } from 'sonner';

const ProvidersPage = () => {
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchProviders = async () => {
    setLoading(true);
    try {
      const response = await userService.getProviders(page, 12);
      setProviders(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (error) {
      toast.error('Failed to load providers');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProviders();
  }, [page]);

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="mb-2 text-3xl font-bold">Find Providers</h1>
          <p className="text-muted-foreground">Browse trusted local service providers</p>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          </div>
        ) : providers.length === 0 ? (
          <div className="flex flex-col items-center justify-center rounded-lg border bg-card p-12 text-center">
            <MapPin className="mb-4 h-12 w-12 text-muted-foreground" />
            <h3 className="mb-2 text-lg font-semibold">No providers found</h3>
            <p className="text-muted-foreground">Check back soon for new providers</p>
          </div>
        ) : (
          <>
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {providers.map((provider) => (
                <Card key={provider.providerId} className="transition-all hover:shadow-lg">
                  <CardHeader>
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <CardTitle className="line-clamp-1">{provider.providerName}</CardTitle>
                        {provider.isActive ? (
                          <Badge variant="outline" className="mt-2 bg-success/10 text-success">
                            Active
                          </Badge>
                        ) : (
                          <Badge variant="outline" className="mt-2">
                            Inactive
                          </Badge>
                        )}
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      <div className="flex items-start text-sm text-muted-foreground">
                        <MapPin className="mr-2 mt-0.5 h-4 w-4 flex-shrink-0" />
                        <span className="line-clamp-2">{provider.providerAddress}</span>
                      </div>
                      <div className="flex items-center text-sm text-muted-foreground">
                        <Mail className="mr-2 h-4 w-4 flex-shrink-0" />
                        <span className="truncate">{provider.providerEmail}</span>
                      </div>
                      <div className="flex items-center text-sm text-muted-foreground">
                        <Phone className="mr-2 h-4 w-4 flex-shrink-0" />
                        {provider.providerContact}
                      </div>
                      {provider.providerReviewAggregateResponse && (
                        <div className="flex items-center text-sm font-medium">
                          <Star className="mr-1 h-4 w-4 fill-yellow-400 text-yellow-400" />
                          {provider.providerReviewAggregateResponse.averageRating?.toFixed(1) ||
                            'N/A'}{' '}
                          ({provider.providerReviewAggregateResponse.reviewCount || 0} reviews)
                        </div>
                      )}
                    </div>
                    <Button className="mt-4 w-full">View Services</Button>
                  </CardContent>
                </Card>
              ))}
            </div>

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

export default ProvidersPage;
