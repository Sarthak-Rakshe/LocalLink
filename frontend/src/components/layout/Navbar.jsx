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
    <header className="sticky top-0 z-20 bg-white/70 border-b border-white/60 backdrop-blur shadow-sm dark:bg-zinc-900/70 dark:border-zinc-800">
      <div className="container-page flex h-14 items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={onMenuToggle}
            className="inline-flex items-center justify-center rounded-md p-2.5 text-zinc-600 hover:bg-zinc-100"
            aria-label="Toggle menu"
            title="Toggle menu"
          >
            <Bars3Icon className="size-6" />
          </button>
          <Link
            to="/dashboard"
            className="bg-linear-to-r from-indigo-600 to-violet-600 bg-clip-text text-base font-semibold text-transparent outline-none focus:outline-none focus:ring-0 focus-visible:ring-2 focus-visible:ring-indigo-500/40 rounded-sm"
            title="Go to dashboard"
            aria-label="Go to dashboard"
          >
            LocalLink
          </Link>
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
              className="input-base pl-9 pr-3 py-1.5"
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
          {/* Theme toggle: simple light <-> dark */}
          <button
            type="button"
            onClick={toggle}
            className="inline-flex items-center justify-center rounded-md p-2 text-zinc-600 hover:bg-zinc-100 dark:text-zinc-300 dark:hover:bg-white/5"
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
              <Menu.Button className="hidden items-center gap-2 rounded-md p-1.5 hover:bg-zinc-100 dark:hover:bg-white/5 sm:flex">
                <div className="grid size-8 place-items-center rounded-full bg-indigo-600 text-white text-sm font-semibold">
                  {initials}
                </div>
                <span className="text-sm font-medium text-zinc-900 dark:text-zinc-100">
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
                <Menu.Items className="absolute right-0 z-30 mt-2 w-48 overflow-hidden rounded-md border border-zinc-200 bg-white py-1 shadow-lg focus:outline-none dark:border-zinc-800 dark:bg-zinc-900">
                  <Menu.Item>
                    {({ active }) => (
                      <button
                        className={`block w-full px-3 py-2 text-left text-sm ${
                          active ? "bg-zinc-50 dark:bg-white/5" : ""
                        }`}
                        onClick={() => navigate("/profile")}
                      >
                        My Profile
                      </button>
                    )}
                  </Menu.Item>
                  <div className="my-1 h-px bg-zinc-200 dark:bg-zinc-800" />
                  <Menu.Item>
                    {({ active }) => (
                      <button
                        className={`block w-full px-3 py-2 text-left text-sm text-rose-600 ${
                          active ? "bg-rose-50 dark:bg-rose-500/10" : ""
                        }`}
                        onClick={handleLogout}
                      >
                        Logout
                      </button>
                    )}
                  </Menu.Item>
                </Menu.Items>
              </Transition>
            </Menu>
          )}
          {!user && (
            <button
              onClick={() => navigate("/login")}
              className="btn btn-outline px-3 py-1.5"
            >
              Login
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
