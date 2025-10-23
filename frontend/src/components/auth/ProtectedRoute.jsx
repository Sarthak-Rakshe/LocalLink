import { Navigate, Outlet, useLocation } from "react-router-dom";

export default function ProtectedRoute() {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("authToken") : null;
  const location = useLocation();

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return <Outlet />;
}
