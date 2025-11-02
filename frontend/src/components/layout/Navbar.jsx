import { Bars3Icon, MagnifyingGlassIcon } from "@heroicons/react/24/outline";
import { useAuth } from "../../context/AuthContext.jsx";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

export default function NavBar({ onMenuToggle }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [query, setQuery] = useState("");

  const handleLogout = async () => {
    await logout();
    navigate("/login", { replace: true });
  };

  const onSearch = (e) => {
    e.preventDefault();
    const q = query.trim();
    if (!q) return navigate("/services");
    navigate(`/services?q=${encodeURIComponent(q)}`);
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
        {/* Search (hidden on very small screens) */}
        <form
          onSubmit={onSearch}
          className="hidden md:block w-full max-w-lg mx-4"
        >
          <label htmlFor="global-search" className="sr-only">
            Search services
          </label>
          <div className="relative">
            <input
              id="global-search"
              className="w-full rounded-md border border-zinc-300 bg-white pl-9 pr-3 py-1.5 text-sm shadow-sm placeholder:text-zinc-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/50"
              placeholder="Search services, categories, providers..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
            <MagnifyingGlassIcon className="pointer-events-none absolute left-2 top-1/2 size-5 -translate-y-1/2 text-zinc-400" />
          </div>
        </form>

        <div className="flex items-center gap-3">
          {/* Mobile search affordance */}
          <button
            type="button"
            onClick={() => navigate("/services")}
            className="md:hidden inline-flex items-center justify-center rounded-md p-2 text-zinc-600 hover:bg-zinc-100"
            aria-label="Search"
          >
            <MagnifyingGlassIcon className="size-5" />
          </button>
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
