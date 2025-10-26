import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Search, Users, Calendar, Shield } from 'lucide-react';
import Navbar from '../components/layout/Navbar';
import { useAuth } from '../context/AuthContext';

const Index = () => {
  const { isAuthenticated } = useAuth();

  const features = [
    {
      icon: Search,
      title: 'Discover Services',
      description: 'Find trusted local service providers in your area',
    },
    {
      icon: Users,
      title: 'Connect Locally',
      description: 'Build relationships with professionals in your community',
    },
    {
      icon: Calendar,
      title: 'Easy Booking',
      description: 'Schedule services with just a few clicks',
    },
    {
      icon: Shield,
      title: 'Verified Providers',
      description: 'All providers are verified with ratings and reviews',
    },
  ];

  return (
    <div className="min-h-screen bg-background">
      <Navbar />

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-20 md:py-32">
        <div className="mx-auto max-w-4xl text-center">
          <h1 className="mb-6 text-4xl font-bold tracking-tight md:text-6xl">
            Connect with Local Service Providers
          </h1>
          <p className="mb-8 text-xl text-muted-foreground">
            LocalLink brings together customers and trusted local professionals. Find services, book
            appointments, and build your community.
          </p>
          <div className="flex flex-col justify-center gap-4 sm:flex-row">
            {isAuthenticated ? (
              <>
                <Link to="/services">
                  <Button size="lg" className="w-full sm:w-auto">
                    Browse Services
                  </Button>
                </Link>
                <Link to="/dashboard">
                  <Button size="lg" variant="outline" className="w-full sm:w-auto">
                    Go to Dashboard
                  </Button>
                </Link>
              </>
            ) : (
              <>
                <Link to="/register">
                  <Button size="lg" className="w-full sm:w-auto">
                    Get Started
                  </Button>
                </Link>
                <Link to="/login">
                  <Button size="lg" variant="outline" className="w-full sm:w-auto">
                    Sign In
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="border-t bg-muted/50 py-20">
        <div className="container mx-auto px-4">
          <h2 className="mb-12 text-center text-3xl font-bold">Why Choose LocalLink?</h2>
          <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-4">
            {features.map((feature) => (
              <div
                key={feature.title}
                className="rounded-lg border bg-card p-6 text-center shadow-card transition-all hover:shadow-lg"
              >
                <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-lg bg-primary">
                  <feature.icon className="h-6 w-6 text-primary-foreground" />
                </div>
                <h3 className="mb-2 text-lg font-semibold">{feature.title}</h3>
                <p className="text-sm text-muted-foreground">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="container mx-auto px-4 py-20">
        <div className="mx-auto max-w-3xl rounded-xl border bg-card p-8 text-center shadow-card md:p-12">
          <h2 className="mb-4 text-3xl font-bold">Ready to Get Started?</h2>
          <p className="mb-8 text-lg text-muted-foreground">
            Join LocalLink today and start connecting with your local community
          </p>
          <div className="flex flex-col justify-center gap-4 sm:flex-row">
            <Link to="/register">
              <Button size="lg" className="w-full sm:w-auto">
                Create Account
              </Button>
            </Link>
            <Link to="/login">
              <Button size="lg" variant="outline" className="w-full sm:w-auto">
                Sign In
              </Button>
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Index;
