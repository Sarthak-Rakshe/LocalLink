import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Search, Calendar, CreditCard, Star } from 'lucide-react';

const CustomerDashboard = ({ user }) => {
  const navigate = useNavigate();

  const quickActions = [
    {
      title: 'Browse Services',
      description: 'Find local services near you',
      icon: Search,
      action: () => navigate('/services'),
      color: 'bg-primary',
    },
    {
      title: 'My Bookings',
      description: 'View and manage your bookings',
      icon: Calendar,
      action: () => navigate('/bookings'),
      color: 'bg-accent',
    },
    {
      title: 'Payment History',
      description: 'View transaction history',
      icon: CreditCard,
      action: () => navigate('/payments'),
      color: 'bg-success',
    },
    {
      title: 'My Reviews',
      description: "Reviews you've written",
      icon: Star,
      action: () => navigate('/reviews'),
      color: 'bg-muted-foreground',
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Welcome back, {user?.userName}!</h1>
        <p className="text-muted-foreground">Here's what's happening with your account</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {quickActions.map((action) => (
          <Card
            key={action.title}
            className="cursor-pointer transition-all hover:scale-105 hover:shadow-lg"
            onClick={action.action}
          >
            <CardHeader className="pb-3">
              <div className={`mb-2 flex h-12 w-12 items-center justify-center rounded-lg ${action.color}`}>
                <action.icon className="h-6 w-6 text-white" />
              </div>
              <CardTitle className="text-lg">{action.title}</CardTitle>
              <CardDescription>{action.description}</CardDescription>
            </CardHeader>
          </Card>
        ))}
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>Your recent bookings and reviews</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <Calendar className="mb-4 h-12 w-12 text-muted-foreground" />
              <p className="text-muted-foreground">No recent activity</p>
              <Button onClick={() => navigate('/services')} className="mt-4">
                Browse Services
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Favorite Providers</CardTitle>
            <CardDescription>Providers you've bookmarked</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <Star className="mb-4 h-12 w-12 text-muted-foreground" />
              <p className="text-muted-foreground">No favorites yet</p>
              <Button onClick={() => navigate('/providers')} className="mt-4">
                Find Providers
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default CustomerDashboard;
