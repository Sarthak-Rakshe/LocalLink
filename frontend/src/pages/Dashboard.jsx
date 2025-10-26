import React from 'react';
import { useAuth } from '../context/AuthContext';
import CustomerDashboard from './dashboard/CustomerDashboard';
import ProviderDashboard from './dashboard/ProviderDashboard';
import AdminDashboard from './dashboard/AdminDashboard';
import Navbar from '../components/layout/Navbar';

const Dashboard = () => {
  const { user, isCustomer, isProvider, isAdmin } = useAuth();

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        {isAdmin() ? (
          <AdminDashboard />
        ) : isProvider() ? (
          <ProviderDashboard user={user} />
        ) : isCustomer() ? (
          <CustomerDashboard user={user} />
        ) : (
          <div className="text-center">
            <p className="text-muted-foreground">Invalid user type</p>
          </div>
        )}
      </main>
    </div>
  );
};

export default Dashboard;
