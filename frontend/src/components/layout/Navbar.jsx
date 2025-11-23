import {
  Bars3Icon,
  MagnifyingGlassIcon,
  MoonIcon,
  SunIcon,
} from "@heroicons/react/24/outline";
import { useAuth } from "../../context/AuthContext.jsx";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { Menu, Transition } from "@headlessui/react";
import { useTheme } from "../../context/ThemeContext.jsx";

export default function NavBar({ onMenuToggle }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const { resolvedTheme, toggle } = useTheme();

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

  const displayName = (() => {
    const first =
      user?.firstName || user?.givenName || user?.firstname || user?.first_name;
    const last =
      user?.lastName || user?.surname || user?.lastname || user?.last_name;
    const full =
      user?.fullName ||
      user?.displayName ||
      user?.name ||
      (first && last ? `${first} ${last}` : first || last);
    const handle =
      user?.username ||
      user?.userName ||
      user?.preferredUsername ||
      user?.preferred_username ||
      user?.nickname ||
      user?.sub;
    return full || handle || user?.email || "User";
  })();
  const initials =
    displayName
      .split(/\s+|[_\.]/)
      .filter(Boolean)
      .slice(0, 2)
      .map((s) => s[0]?.toUpperCase())
      .join("") || "U";

  return (
    <header className="sticky top-0 z-20 bg-[var(--bg-surface)]/80 border-b border-[var(--border-subtle)] backdrop-blur-md shadow-sm">
      <div className="container-page flex h-16 items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onMenuToggle}
            className="inline-flex items-center justify-center rounded-lg p-2 text-muted hover:bg-[var(--bg-surface-hover)] hover:text-default transition-colors"
            aria-label="Toggle menu"
            title="Toggle menu"
          >
            <Bars3Icon className="size-6" />
          </button>
          <Link
            to="/dashboard"
            className="bg-gradient-to-r from-brand-600 to-accent-600 bg-clip-text text-xl font-bold text-transparent outline-none focus:outline-none focus:ring-0 focus-visible:ring-2 focus-visible:ring-brand-500/40 rounded-sm"
            title="Go to dashboard"
            aria-label="Go to dashboard"
          >
            LocalLink
          </Link>
        </div>

        {/* Search (hidden on very small screens) */}
        <form
          onSubmit={onSearch}
          className="hidden md:block w-full max-w-md mx-auto"
        >
          <label htmlFor="global-search" className="sr-only">
            Search services
          </label>
          <div className="relative group">
            <input
              id="global-search"
              className="input-base rounded-full pl-10 pr-4 py-2"
              placeholder="Search services..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
            <MagnifyingGlassIcon className="pointer-events-none absolute left-3 top-1/2 size-5 -translate-y-1/2 text-zinc-400 group-focus-within:text-brand-500 transition-colors" />
          </div>
        </form>

        <div className="flex items-center gap-2 sm:gap-3">
          {/* Mobile search affordance */}
          <button
            type="button"
            onClick={() => navigate("/services")}
            className="md:hidden inline-flex items-center justify-center rounded-lg p-2 text-muted hover:bg-[var(--bg-surface-hover)]"
            aria-label="Search"
          >
            <MagnifyingGlassIcon className="size-5" />
          </button>

          {/* Theme toggle */}
          <button
            type="button"
            onClick={toggle}
            className="inline-flex items-center justify-center rounded-lg p-2 text-muted hover:bg-[var(--bg-surface-hover)] hover:text-default transition-colors"
            aria-label="Toggle theme"
            title={
              resolvedTheme === "dark" ? "Switch to light" : "Switch to dark"
            }
          >
            {resolvedTheme === "dark" ? (
              <SunIcon className="size-5" />
            ) : (
              <MoonIcon className="size-5" />
            )}
          </button>

          {user && (
            <Menu as="div" className="relative">
              <Menu.Button className="hidden items-center gap-2 rounded-full p-1 hover:bg-[var(--bg-surface-hover)] transition-colors sm:flex pr-3 border border-transparent hover:border-[var(--border-base)]">
                <div className="grid size-8 place-items-center rounded-full bg-gradient-to-br from-brand-500 to-brand-600 text-white text-sm font-semibold shadow-sm">
                  {initials}
                </div>
                <span className="text-sm font-medium text-default max-w-[100px] truncate">
                  {displayName}
                </span>
              </Menu.Button>
              <Transition
                enter="transition ease-out duration-100"
                enterFrom="transform opacity-0 scale-95"
                enterTo="transform opacity-100 scale-100"
                leave="transition ease-in duration-75"
                leaveFrom="transform opacity-100 scale-100"
                leaveTo="transform opacity-0 scale-95"
              >
                <Menu.Items className="absolute right-0 z-30 mt-2 w-56 origin-top-right overflow-hidden rounded-xl border border-[var(--border-base)] bg-[var(--bg-surface)]/90 backdrop-blur-lg shadow-xl focus:outline-none">
                  <div className="px-4 py-3 border-b border-[var(--border-subtle)]">
                    <p className="text-sm font-medium text-default truncate">
                      {displayName}
                    </p>
                    <p className="text-xs text-muted truncate">
                      {user.email}
                    </p>
                  </div>
                  <div className="p-1">
                    <Menu.Item>
                      {({ active }) => (
                        <button
                          className={`block w-full rounded-lg px-3 py-2 text-left text-sm transition-colors ${active ? "bg-[var(--bg-surface-hover)] text-default" : "text-muted"
                            }`}
                          onClick={() => navigate("/profile")}
                        >
                          My Profile
                        </button>
                      )}
                    </Menu.Item>
                    <Menu.Item>
                      {({ active }) => (
                        <button
                          className={`block w-full rounded-lg px-3 py-2 text-left text-sm transition-colors ${active ? "bg-rose-50 text-rose-700 dark:bg-rose-900/20 dark:text-rose-400" : "text-rose-600 dark:text-rose-400"
                            }`}
                          onClick={handleLogout}
                        >
                          Logout
                        </button>
                      )}
                    </Menu.Item>
                  </div>
                </Menu.Items>
              </Transition>
            </Menu>
          )}
          {!user && (
            <button
              onClick={() => navigate("/login")}
              className="btn btn-primary px-4 py-2 shadow-md shadow-brand-500/20"
            >
              Login
            </button>
          )}
        </div>
      </div>
    </header>
  );
}

