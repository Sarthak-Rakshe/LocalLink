import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="border-b bg-white">
      <nav className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link to="/" className="text-xl font-semibold">
          LocalLink
        </Link>
        <div className="flex items-center gap-4">
          <NavLink to="/services" className="text-gray-700 hover:text-black">
            Services
          </NavLink>
          {isAuthenticated ? (
            <>
              <NavLink
                to="/bookings"
                className="text-gray-700 hover:text-black"
              >
                Bookings
              </NavLink>
              <NavLink to="/profile" className="text-gray-700 hover:text-black">
                {user?.userName || user?.name || "Profile"}
              </NavLink>
              <button
                onClick={handleLogout}
                className="rounded bg-gray-900 px-3 py-1.5 text-white hover:bg-black"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="text-gray-700 hover:text-black">
                Login
              </NavLink>
              <NavLink
                to="/register"
                className="rounded bg-gray-900 px-3 py-1.5 text-white hover:bg-black"
              >
                Sign up
              </NavLink>
            </>
          )}
        </div>
      </nav>
    </header>
  );
}
