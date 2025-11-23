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
        `group flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200 ${isActive
          ? "bg-brand-50 text-brand-700 shadow-sm dark:bg-brand-900/20 dark:text-brand-300"
          : "text-muted hover:bg-[var(--bg-surface-hover)] hover:text-default dark:text-muted dark:hover:bg-[var(--bg-surface-hover)] dark:hover:text-default"
        }`
      }
      end
    >
      <span className={`size-5 transition-colors ${({ isActive }) => isActive ? "text-brand-600 dark:text-brand-400" : "text-zinc-400 group-hover:text-zinc-600 dark:text-zinc-500 dark:group-hover:text-zinc-300"
        }`}>
        {icon}
      </span>
      <span>{children}</span>
    </NavLink>
  );
}

export default function SideBar({ open, onClose }) {
  const { user } = useAuth();
  const isProvider = user?.userType === "PROVIDER";

  const content = (
    <div className="flex h-full flex-col gap-1 p-4">
      <div className="px-3 py-4 mb-2">
        <h2 className="text-xs font-bold uppercase tracking-wider text-muted">
          Menu
        </h2>
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
        <>
          <div className="my-2 mx-3 h-px bg-[var(--border-subtle)]" />
          <div className="px-3 py-2">
            <h2 className="text-xs font-bold uppercase tracking-wider text-muted">
              Provider
            </h2>
          </div>
          <NavItem to="/availability" icon={<CalendarIcon className="size-5" />}>
            Availability
          </NavItem>
          <NavItem
            to="/services/manage"
            icon={<WrenchScrewdriverIcon className="size-5" />}
          >
            My Services
          </NavItem>
        </>
      )}

      <div className="mt-auto">
        <NavItem to="/payments" icon={<CreditCardIcon className="size-5" />}>
          Payments
        </NavItem>
      </div>
    </div>
  );

  return (
    <>
      {/* Mobile overlay */}
      <div
        className={`fixed inset-0 z-30 bg-zinc-900/20 backdrop-blur-sm transition-opacity md:hidden ${open ? "opacity-100" : "pointer-events-none opacity-0"
          }`}
        onClick={onClose}
      />

      {/* Drawer */}
      <aside
        className={`fixed inset-y-0 left-0 z-40 w-64 bg-[var(--bg-surface)]/80 backdrop-blur-md border-r border-[var(--border-subtle)] shadow-xl transition-transform duration-300 ease-in-out ${open ? "translate-x-0" : "-translate-x-full"
          }`}
        aria-label="Sidebar"
        aria-hidden={!open}
      >
        {content}
      </aside>
    </>
  );
}
