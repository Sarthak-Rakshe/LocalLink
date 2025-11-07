import { useEffect, useMemo, useState } from "react";
import { Outlet, useLocation, matchPath } from "react-router-dom";
import NavBar from "./Navbar.jsx";
import SideBar from "./SideBar.jsx";
import Footer from "./Footer.jsx";
import TopProgressBar from "../ui/TopProgressBar.jsx";

export default function AppShell({ children }) {
  // Sidebar: persistent on desktop, drawer on mobile
  const [open, setOpen] = useState(() => {
    try {
      const stored = localStorage.getItem("sidebar-open");
      if (stored != null) return stored === "true";
    } catch {}
    return (
      window.matchMedia && window.matchMedia("(min-width: 1024px)").matches
    ); // lg
  });

  useEffect(() => {
    try {
      localStorage.setItem("sidebar-open", String(open));
    } catch {}
  }, [open]);
  const location = useLocation();

  const showTopbar = useMemo(() => {
    const pathname = location.pathname;
    const patterns = [
      "/dashboard",
      "/dashboard/customer",
      "/dashboard/provider",
      "/profile",
      "/bookings",
      "/bookings/*",
      "/services",
      "/services/*",
      "/payments",
      "/payments/*",
      "/reviews",
      "/reviews/*",
      "/availability",
      "/availability/*",
    ];
    return patterns.some((p) =>
      p.endsWith("/*")
        ? matchPath({ path: p, end: false }, pathname)
        : matchPath({ path: p, end: true }, pathname)
    );
  }, [location.pathname]);

  return (
    <div
      className={`flex min-h-dvh bg-linear-to-b from-brand-50 to-white dark:from-zinc-950 dark:to-zinc-900 app-bg ${
        open ? "lg:pl-64" : ""
      }`}
    >
      <TopProgressBar />
      <SideBar open={open} onClose={() => setOpen(false)} />
      <div className="flex min-w-0 flex-1 flex-col">
        {showTopbar && <NavBar onMenuToggle={() => setOpen((v) => !v)} />}
        <main className="container-page flex-1 py-4 md:py-6">
          {children ?? <Outlet />}
        </main>
        <Footer />
      </div>
    </div>
  );
}
