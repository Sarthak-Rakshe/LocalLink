import { useState } from "react";
import { Outlet } from "react-router-dom";
import NavBar from "./Navbar.jsx";
import SideBar from "./SideBar.jsx";
import Footer from "./Footer.jsx";

export default function AppShell({ children }) {
  const [open, setOpen] = useState(false);

  return (
    <div className={`flex min-h-dvh bg-zinc-50 ${open ? "lg:pl-64" : ""}`}>
      <SideBar open={open} onClose={() => setOpen(false)} />
      <div className="flex min-w-0 flex-1 flex-col">
        <NavBar onMenuToggle={() => setOpen((v) => !v)} />
        <main className="container-page flex-1 py-4 md:py-6">
          {children ?? <Outlet />}
        </main>
        <Footer />
      </div>
    </div>
  );
}
