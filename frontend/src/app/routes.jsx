/* eslint-disable react-refresh/only-export-components */
import { createBrowserRouter, Navigate } from "react-router-dom";
import { lazy, Suspense } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import AppShell from "../components/layout/AppShell.jsx";

// Lazy-loaded route components for faster initial loads
const Login = lazy(() => import("../pages/auth/Login.jsx"));
const CustomerDashboard = lazy(() =>
  import("../pages/dashboard/CustomerDashboard.jsx")
);
const ProviderDashboard = lazy(() =>
  import("../pages/dashboard/ProviderDashboard.jsx")
);
const NotFound = lazy(() => import("../pages/errors/NotFound.jsx"));
const BookingsHome = lazy(() => import("../pages/bookings/Index.jsx"));
const BookingsList = lazy(() => import("../pages/bookings/List.jsx"));
const BookingDetails = lazy(() => import("../pages/bookings/Details.jsx"));
const BookingCreate = lazy(() => import("../pages/bookings/Create.jsx"));
const AvailabilityHome = lazy(() => import("../pages/availability/Index.jsx"));
const AvailabilityManage = lazy(() =>
  import("../pages/availability/Manage.jsx")
);
const AvailabilityCalendar = lazy(() =>
  import("../pages/availability/Calendar.jsx")
);
const PaymentsHome = lazy(() => import("../pages/payments/Index.jsx"));

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="p-6 text-center">Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

function RequireUserType({ children, allowed }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!allowed.includes(user.userType)) {
    // redirect to their dashboard
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}

function DashboardRedirect() {
  const { user, loading } = useAuth();
  if (loading) return <div className="p-6 text-center">Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (user.userType === "CUSTOMER")
    return <Navigate to="/dashboard/customer" replace />;
  if (user.userType === "PROVIDER")
    return <Navigate to="/dashboard/provider" replace />;
  return <Navigate to="/login" replace />;
}

function HomeRedirect() {
  const { user, loading } = useAuth();
  if (loading) return <div className="p-6 text-center">Loading...</div>;
  return user ? (
    <Navigate to="/dashboard" replace />
  ) : (
    <Navigate to="/login" replace />
  );
}

export const router = createBrowserRouter([
  {
    path: "/",
    element: (
      <Suspense fallback={<div className="p-6 text-center">Loading...</div>}>
        <HomeRedirect />
      </Suspense>
    ),
  },
  {
    path: "/login",
    element: (
      <Suspense fallback={<div className="p-6 text-center">Loading...</div>}>
        <Login />
      </Suspense>
    ),
  },
  // Protected app layout
  {
    element: (
      <Suspense fallback={<div className="p-6 text-center">Loading...</div>}>
        <ProtectedRoute>
          <AppShell />
        </ProtectedRoute>
      </Suspense>
    ),
    children: [
      {
        path: "/dashboard",
        element: <DashboardRedirect />,
      },
      {
        path: "/dashboard/customer",
        element: (
          <RequireUserType allowed={["CUSTOMER"]}>
            <CustomerDashboard />
          </RequireUserType>
        ),
      },
      {
        path: "/dashboard/provider",
        element: (
          <RequireUserType allowed={["PROVIDER"]}>
            <ProviderDashboard />
          </RequireUserType>
        ),
      },
      // Bookings
      { path: "/bookings", element: <BookingsList /> },
      { path: "/bookings/:id", element: <BookingDetails /> },
      {
        path: "/bookings/create",
        element: (
          <RequireUserType allowed={["CUSTOMER"]}>
            <BookingCreate />
          </RequireUserType>
        ),
      },
      // Availability (provider-only)
      {
        path: "/availability",
        element: (
          <RequireUserType allowed={["PROVIDER"]}>
            <AvailabilityManage />
          </RequireUserType>
        ),
      },
      {
        path: "/availability/calendar",
        element: (
          <RequireUserType allowed={["PROVIDER"]}>
            <AvailabilityCalendar />
          </RequireUserType>
        ),
      },
      { path: "/payments/*", element: <PaymentsHome /> },
    ],
  },
  {
    path: "*",
    element: (
      <Suspense fallback={<div className="p-6 text-center">Loading...</div>}>
        <NotFound />
      </Suspense>
    ),
  },
]);
