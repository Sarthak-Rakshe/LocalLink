import { NavLink } from "react-router-dom";
import {
  CalendarIcon,
  CreditCardIcon,
  HomeIcon,
  ListBulletIcon,
} from "@heroicons/react/24/outline";
import { useAuth } from "../../context/AuthContext.jsx";

function NavItem({ to, icon: Icon, children }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium hover:bg-zinc-100 ${
          isActive ? "bg-zinc-100 text-zinc-900" : "text-zinc-700"
        }`
      }
      end
    >
      <Icon className="size-5" />
      <span>{children}</span>
    </NavLink>
  );
}

export default function SideBar({ open, onClose }) {
  const { user } = useAuth();
  const isProvider = user?.userType === "PROVIDER";

  const content = (
    <div className="flex h-full flex-col gap-2 p-3">
      <div className="px-2 py-3 text-xs font-semibold uppercase text-zinc-500">
        Menu
      </div>
      {/* Dashboard (varies by user type) */}
      {isProvider ? (
        <NavItem to="/dashboard/provider" icon={HomeIcon}>
          Dashboard
        </NavItem>
      ) : (
        <NavItem to="/dashboard/customer" icon={HomeIcon}>
          Dashboard
        </NavItem>
      )}
      <NavItem to="/bookings" icon={ListBulletIcon}>
        Bookings
      </NavItem>
      {isProvider && (
        <NavItem to="/availability" icon={CalendarIcon}>
          Availability
        </NavItem>
      )}
      <NavItem to="/payments" icon={CreditCardIcon}>
        Payments
      </NavItem>
    </div>
  );

  return (
    <>
      {/* Mobile overlay */}
      <div
        className={`fixed inset-0 z-30 bg-black/30 transition-opacity md:hidden ${
          open ? "opacity-100" : "pointer-events-none opacity-0"
        }`}
        onClick={onClose}
      />

      {/* Drawer */}
      <aside
        className={`fixed inset-y-0 left-0 z-40 w-64 border-r bg-white transition-transform ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
        aria-label="Sidebar"
        aria-hidden={!open}
      >
        {content}
      </aside>
    </>
  );
}
