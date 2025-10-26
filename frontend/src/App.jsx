import React from "react";
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Index from "./pages/Index";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import Dashboard from "./pages/Dashboard";
import ServicesPage from "./pages/ServicesPage";
import ProvidersPage from "./pages/ProvidersPage";
import BookingsPage from "./pages/BookingsPage";
import PaymentsPage from "./pages/PaymentsPage";
import ProfilePage from "./pages/ProfilePage";
import MyServicesPage from "./pages/MyServicesPage";
import ServiceCreatePage from "./pages/ServiceCreatePage";
import ServiceEditPage from "./pages/ServiceEditPage";
import NearbyServicesPage from "./pages/NearbyServicesPage";
import ServiceDetailsPage from "./pages/ServiceDetailsPage";
import AvailabilityPage from "./pages/AvailabilityPage";
import ProviderBookingsPage from "./pages/ProviderBookingsPage";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import PublicOnlyRoute from "./components/auth/PublicOnlyRoute";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            <Route
              path="/"
              element={
                <PublicOnlyRoute>
                  <Index />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/login"
              element={
                <PublicOnlyRoute>
                  <Login />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/register"
              element={
                <PublicOnlyRoute>
                  <Register />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/services"
              element={
                <ProtectedRoute>
                  <ServicesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/services/:serviceId"
              element={
                <ProtectedRoute>
                  <ServiceDetailsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/providers"
              element={
                <ProtectedRoute>
                  <ProvidersPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/nearby"
              element={
                <ProtectedRoute>
                  <NearbyServicesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/bookings"
              element={
                <ProtectedRoute>
                  <BookingsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/provider-bookings"
              element={
                <ProtectedRoute>
                  <ProviderBookingsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/availability"
              element={
                <ProtectedRoute>
                  <AvailabilityPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/payments"
              element={
                <ProtectedRoute>
                  <PaymentsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-services"
              element={
                <ProtectedRoute>
                  <MyServicesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-services/create"
              element={
                <ProtectedRoute>
                  <ServiceCreatePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-services/edit/:serviceId"
              element={
                <ProtectedRoute>
                  <ServiceEditPage />
                </ProtectedRoute>
              }
            />
            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </AuthProvider>
  </QueryClientProvider>
);

export default App;
