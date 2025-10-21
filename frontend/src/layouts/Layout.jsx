import { Outlet } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";

export default function Layout() {
  return (
    <div className="flex flex-col min-h-screen bg-gray-50 text-gray-900">
      <Navbar />
      <main className="flex-grow py-8">
        <div className="mx-auto w-full max-w-6xl px-4">
          <Outlet />
        </div>
      </main>
      <Footer />
    </div>
  );
}
