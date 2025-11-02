import { useMemo, useState } from "react";
import { Outlet, useLocation, matchPath } from "react-router-dom";
import NavBar from "./Navbar.jsx";
import SideBar from "./SideBar.jsx";
import Footer from "./Footer.jsx";
import TopProgressBar from "../ui/TopProgressBar.jsx";

export default function AppShell({ children }) {
  const [open, setOpen] = useState(false);
  const location = useLocation();

  const showTopbar = useMemo(() => {
    const pathname = location.pathname;
    const patterns = [
      "/dashboard",
      "/dashboard/customer",
      "/dashboard/provider",
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
    <div className={`flex min-h-dvh bg-zinc-50 ${open ? "lg:pl-64" : ""}`}>
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
