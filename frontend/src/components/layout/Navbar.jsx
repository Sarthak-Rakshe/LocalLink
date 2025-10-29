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
    <header className="sticky top-0 z-20 bg-white/80 backdrop-blur border-b">
      <div className="h-14 px-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={onMenuToggle}
            className="inline-flex items-center justify-center rounded-md p-2 text-zinc-600 hover:bg-zinc-100"
            aria-label="Toggle menu"
          >
            <Bars3Icon className="size-6" />
          </button>
          <span className="font-semibold">LocalLink</span>
        </div>
        <div className="flex items-center gap-3">
          {user && (
            <div className="text-sm text-zinc-600">
              <span className="font-medium text-zinc-900">
                {user?.username || user?.name || "User"}
              </span>
              <span className="ml-2 inline-flex items-center rounded-full bg-zinc-100 px-2 py-0.5 text-[11px] uppercase tracking-wide text-zinc-700">
                {user?.userType || ""}
              </span>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="inline-flex items-center rounded-md border px-3 py-1.5 text-sm font-medium hover:bg-zinc-50"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
}
