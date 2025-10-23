import { Link, NavLink, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import clsx from "clsx";
import { isProvider } from "../../api/session";

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const [provider, setProvider] = useState(false);

  useEffect(() => {
    setProvider(isProvider());
    // Listen for storage changes across tabs
    const onStorage = (e) => {
      if (e.key === "userType" || e.key === "user") setProvider(isProvider());
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  const logout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("tokenType");
    localStorage.removeItem("userType");
    localStorage.removeItem("user");
    navigate("/login", { replace: true });
  };

  return (
    <header className="border-b border-gray-200 bg-white/80 backdrop-blur">
      <div className="container-app flex h-14 items-center justify-between">
        {/* Left: Brand + Nav routes */}
        <div className="flex items-center gap-6">
          <Link to="/home" className="font-semibold tracking-tight">
            LocalLink
          </Link>
          <nav className="flex items-center gap-4 text-sm">
            <NavLink
              to="/home"
              className={({ isActive }) =>
                clsx(
                  "hover:underline transition-colors",
                  isActive
                    ? "text-indigo-600 font-semibold underline"
                    : "text-gray-700"
                )
              }
            >
              Home
            </NavLink>
            <NavLink
              to="/services"
              className={({ isActive }) =>
                clsx(
                  "hover:underline transition-colors",
                  isActive
                    ? "text-indigo-600 font-semibold underline"
                    : "text-gray-700"
                )
              }
            >
              Services
            </NavLink>
            {provider && (
              <NavLink
                to="/my-services"
                className={({ isActive }) =>
                  clsx(
                    "hover:underline transition-colors",
                    isActive
                      ? "text-indigo-600 font-semibold underline"
                      : "text-gray-700"
                  )
                }
              >
                My Services
              </NavLink>
            )}
            <NavLink
              to="/bookings"
              className={({ isActive }) =>
                clsx(
                  "hover:underline transition-colors",
                  isActive
                    ? "text-indigo-600 font-semibold underline"
                    : "text-gray-700"
                )
              }
            >
              Bookings
            </NavLink>
            <NavLink
              to="/providers"
              className={({ isActive }) =>
                clsx(
                  "hover:underline transition-colors",
                  isActive
                    ? "text-indigo-600 font-semibold underline"
                    : "text-gray-700"
                )
              }
            >
              Service Providers
            </NavLink>
          </nav>
        </div>

        {/* Right: Profile dropdown */}
        <div className="relative" onBlur={() => setOpen(false)} tabIndex={0}>
          <button
            type="button"
            className="h-8 w-8 rounded-full bg-gray-900 text-white flex items-center justify-center"
            onClick={() => setOpen((v) => !v)}
            aria-haspopup="menu"
            aria-expanded={open}
            title="Profile"
          >
            {/* simple avatar dot */}
            <span className="sr-only">Open profile menu</span>•
          </button>
          {open && (
            <div
              role="menu"
              className="absolute right-0 mt-2 w-44 rounded-lg border border-gray-200 bg-white shadow-sm text-sm"
            >
              <button
                className="w-full text-left px-3 py-2 hover:bg-gray-50"
                onMouseDown={() => navigate("/profile")}
                role="menuitem"
              >
                My profile
              </button>
              <button
                className="w-full text-left px-3 py-2 text-red-600 hover:bg-red-50"
                onMouseDown={logout}
                role="menuitem"
              >
                Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
