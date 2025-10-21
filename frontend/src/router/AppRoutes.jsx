import {
  createBrowserRouter,
  RouterProvider,
  Navigate,
} from "react-router-dom";
import Layout from "../layouts/Layout";
import Home from "../pages/Home";
import Login from "../pages/Login";
import Register from "../pages/Register";
import Profile from "../pages/Profile";
import Services from "../pages/Services";
import ServiceDetails from "../pages/ServiceDetails";
import Bookings from "../pages/Bookings";
import Admin from "../pages/Admin";
import Payment from "../pages/Payment";

const router = createBrowserRouter([
  // Login/Register without restriction (optional: still no Layout for clean page)
  { path: "login", element: <Login /> },
  { path: "register", element: <Register /> },

  // All other pages inside Layout
  {
    path: "/",
    element: <Layout />,
    children: [
      { index: true, element: <Home /> },
      { path: "services", element: <Services /> },
      { path: "services/:id", element: <ServiceDetails /> },
      { path: "profile", element: <Profile /> },
      { path: "bookings", element: <Bookings /> },
      { path: "admin", element: <Admin /> },
      { path: "payment", element: <Payment /> },
      { path: "*", element: <Navigate to="/" replace /> },
    ],
  },
]);

export default function AppRoutes() {
  return <RouterProvider router={router} />;
}
