import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "../pages/Login.jsx";
import Register from "../pages/Register.jsx";
import Home from "../pages/Home.jsx";
import Services from "../pages/Services.jsx";
import Bookings from "../pages/Bookings.jsx";
import Providers from "../pages/Providers.jsx";
import Profile from "../pages/Profile.jsx";
import MyServices from "../pages/MyServices.jsx";
import ServiceDetails from "../pages/ServiceDetails.jsx";
import AppLayout from "../components/layout/AppLayout.jsx";
import ProtectedRoute from "../components/auth/ProtectedRoute.jsx";

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Default to login on root */}
        <Route path="/" element={<Navigate to="/login" replace />} />

        {/* Auth routes without layout */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Protected app routes with global layout */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/home" element={<Home />} />
            <Route path="/services" element={<Services />} />
            <Route
              path="/serviceDetails/:serviceId"
              element={<ServiceDetails />}
            />
            <Route path="/my-services" element={<MyServices />} />
            <Route path="/bookings" element={<Bookings />} />
            <Route path="/providers" element={<Providers />} />
            <Route path="/profile" element={<Profile />} />
          </Route>
        </Route>

        {/* Fallback: send unknown to login */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
