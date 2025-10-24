import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Navbar } from "@/components/Navbar";
import { MapPin, Star, Calendar, Shield, Search, Zap } from "lucide-react";

export default function Home() {
  const features = [
    {
      icon: Search,
      title: "Discover Services",
      description: "Browse through hundreds of local service providers in your area",
    },
    {
      icon: Star,
      title: "Verified Reviews",
      description: "Read genuine reviews from real customers to make informed decisions",
    },
    {
      icon: Calendar,
      title: "Easy Booking",
      description: "Check availability and book appointments in just a few clicks",
    },
    {
      icon: Shield,
      title: "Secure Payments",
      description: "Pay safely with our secure payment processing system",
    },
    {
      icon: MapPin,
      title: "Location-Based",
      description: "Find service providers near you with our smart location features",
    },
    {
      icon: Zap,
      title: "Quick Response",
      description: "Get instant confirmations and updates on your bookings",
    },
  ];

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary via-primary-glow to-secondary py-20 text-primary-foreground">
        <div className="container relative z-10">
          <div className="mx-auto max-w-3xl text-center">
            <h1 className="mb-6 text-5xl font-bold leading-tight md:text-6xl">
              Connect with Local Service Providers
            </h1>
            <p className="mb-8 text-lg text-primary-foreground/90 md:text-xl">
              Find, book, and pay for local services all in one place. From plumbing to tutoring, we've got you covered.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link to="/explore">
                <Button size="lg" variant="secondary" className="text-lg px-8">
                  Browse Services
                </Button>
              </Link>
              <Link to="/register">
                <Button size="lg" variant="outline" className="text-lg px-8 bg-white/10 border-white/30 hover:bg-white/20 text-white">
                  Sign Up Free
                </Button>
              </Link>
            </div>
          </div>
        </div>
        <div className="absolute inset-0 bg-gradient-to-b from-transparent to-background/10"></div>
      </section>

      {/* Features Section */}
      <section className="py-20">
        <div className="container">
          <div className="mb-12 text-center">
            <h2 className="mb-4 text-3xl font-bold md:text-4xl">Why Choose LocalLink?</h2>
            <p className="text-lg text-muted-foreground">
              Everything you need to find and book local services
            </p>
          </div>

          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {features.map((feature, index) => (
              <Card key={index} className="group transition-smooth hover:shadow-lg">
                <CardContent className="p-6">
                  <div className="mb-4 inline-flex rounded-lg bg-primary/10 p-3 text-primary">
                    <feature.icon className="h-6 w-6" />
                  </div>
                  <h3 className="mb-2 text-xl font-semibold">{feature.title}</h3>
                  <p className="text-muted-foreground">{feature.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-muted py-20">
        <div className="container">
          <div className="mx-auto max-w-3xl text-center">
            <h2 className="mb-4 text-3xl font-bold md:text-4xl">Ready to Get Started?</h2>
            <p className="mb-8 text-lg text-muted-foreground">
              Join thousands of customers and providers already using LocalLink
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link to="/register">
                <Button size="lg" className="text-lg px-8">
                  Create Account
                </Button>
              </Link>
              <Link to="/explore">
                <Button size="lg" variant="outline" className="text-lg px-8">
                  Explore Services
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t py-8">
        <div className="container text-center text-sm text-muted-foreground">
          <p>© 2025 LocalLink. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
