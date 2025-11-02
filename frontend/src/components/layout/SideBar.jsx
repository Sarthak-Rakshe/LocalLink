import { NavLink } from "react-router-dom";
import {
  CalendarIcon,
  CreditCardIcon,
  HomeIcon,
  ListBulletIcon,
  StarIcon,
  Squares2X2Icon,
  WrenchScrewdriverIcon,
} from "@heroicons/react/24/outline";
import { useAuth } from "../../context/AuthContext.jsx";

function NavItem({ to, icon, children }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
          isActive
            ? "bg-indigo-50 text-indigo-700"
            : "text-zinc-700 hover:bg-zinc-100"
        }`
      }
      end
    >
      <span className="size-5">{icon}</span>
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
        <NavItem
          to="/dashboard/provider"
          icon={<HomeIcon className="size-5" />}
        >
          Dashboard
        </NavItem>
      ) : (
        <NavItem
          to="/dashboard/customer"
          icon={<HomeIcon className="size-5" />}
        >
          Dashboard
        </NavItem>
      )}
      <NavItem to="/bookings" icon={<ListBulletIcon className="size-5" />}>
        Bookings
      </NavItem>
      <NavItem to="/services" icon={<Squares2X2Icon className="size-5" />}>
        Services
      </NavItem>
      <NavItem to="/reviews" icon={<StarIcon className="size-5" />}>
        Reviews
      </NavItem>
      {isProvider && (
        <NavItem to="/availability" icon={<CalendarIcon className="size-5" />}>
          Availability
        </NavItem>
      )}
      {isProvider && (
        <NavItem
          to="/services/manage"
          icon={<WrenchScrewdriverIcon className="size-5" />}
        >
          My Services
        </NavItem>
      )}
      <NavItem to="/payments" icon={<CreditCardIcon className="size-5" />}>
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
