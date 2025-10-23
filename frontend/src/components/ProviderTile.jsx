import { Link } from "react-router-dom";

function initials(name = "") {
  const parts = String(name).trim().split(/\s+/).filter(Boolean);
  const first = parts[0]?.[0] || "P";
  const second = parts[1]?.[0] || "";
  return (first + second).toUpperCase();
}

export default function ProviderTile({ provider }) {
  const name =
    provider?.name ||
    provider?.fullName ||
    provider?.displayName ||
    provider?.username ||
    "Provider";
  const id = provider?.id || provider?.userId || provider?.providerId || null;
  const to = id
    ? `/services?providerId=${encodeURIComponent(id)}`
    : "/providers";

  return (
    <Link
      to={to}
      state={{ from: "home" }}
      className="snap-start block relative rounded-lg overflow-hidden min-w-[11rem] sm:min-w-[13rem] bg-indigo-50 hover:shadow transition-shadow"
    >
      <div className="h-36 w-full flex items-center justify-center">
        <div className="h-16 w-16 rounded-full bg-indigo-200 text-indigo-800 flex items-center justify-center text-xl font-semibold">
          {initials(name)}
        </div>
      </div>
      <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-2">
        <div className="text-white text-sm font-medium truncate">{name}</div>
      </div>
    </Link>
  );
}
