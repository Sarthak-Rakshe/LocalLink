import clsx from "clsx";

export function Tabs({ value, onChange, children, className }) {
  return (
    <div className={clsx("flex items-center gap-1 border-b", className)}>
      {children({ value, onChange })}
    </div>
  );
}

export function Tab({ label, isActive, onClick }) {
  return (
    <button
      type="button"
      className={clsx(
        "-mb-px rounded-t-md px-3 py-2 text-sm",
        isActive
          ? "border-b-2 border-indigo-600 bg-indigo-50 text-indigo-700"
          : "text-zinc-600 hover:bg-zinc-100"
      )}
      onClick={onClick}
    >
      {label}
    </button>
  );
}
