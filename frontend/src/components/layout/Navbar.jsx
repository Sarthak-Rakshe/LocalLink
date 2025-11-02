import { Bars3Icon } from "@heroicons/react/24/outline";
import { useAuth } from "../../context/AuthContext.jsx";
import { useNavigate } from "react-router-dom";

export default function NavBar({ onMenuToggle }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="sticky top-0 z-20 border-b bg-white/80 backdrop-blur">
      <div className="container-page flex h-14 items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={onMenuToggle}
            className="inline-flex items-center justify-center rounded-md p-2 text-zinc-600 hover:bg-zinc-100"
            aria-label="Toggle menu"
          >
            <Bars3Icon className="size-6" />
          </button>
          <span className="bg-linear-to-r from-indigo-600 to-violet-600 bg-clip-text text-base font-semibold text-transparent">
            LocalLink
          </span>
        </div>
        <div className="flex items-center gap-3">
          {user && (
            <div className="hidden text-sm text-zinc-600 sm:block">
              <span className="font-medium text-zinc-900">
                {user?.username || user?.name || "User"}
              </span>
              <span className="ml-2 inline-flex items-center rounded-full bg-indigo-50 px-2 py-0.5 text-[11px] uppercase tracking-wide text-indigo-700">
                {user?.userType || ""}
              </span>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="btn btn-outline px-3 py-1.5"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
}
