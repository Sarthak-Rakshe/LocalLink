import Navbar from "./Navbar.jsx";
import Footer from "./Footer.jsx";
import { Outlet } from "react-router-dom";

export default function AppLayout({ children }) {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="container-app py-6 flex-1">
        {children ?? <Outlet />}
      </main>
      <Footer />
    </div>
  );
}
