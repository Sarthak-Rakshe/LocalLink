import { useState } from "react";

export default function Card({
  title,
  description,
  action,
  children,
  className,
  collapsible = false,
  defaultOpen = true,
}) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <section
      className={`rounded-xl border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-900 ${
        className ?? ""
      }`}
    >
      {(title || action || description) && (
        <div className="flex items-start justify-between gap-3 border-b border-zinc-200 px-5 py-3.5 dark:border-zinc-800">
          <button
            type="button"
            className={`group -m-2 flex min-w-0 flex-1 items-start gap-2 p-2 text-left ${
              collapsible ? "cursor-pointer" : "cursor-default"
            }`}
            onClick={collapsible ? () => setOpen((o) => !o) : undefined}
            aria-expanded={collapsible ? open : undefined}
          >
            {collapsible && (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                className={`mt-0.5 size-4 flex-none text-zinc-500 transition-transform duration-200 ${
                  open ? "rotate-90" : "rotate-0"
                }`}
                aria-hidden="true"
              >
                <path d="M9 5l7 7-7 7" />
              </svg>
            )}
            <div className="min-w-0">
              {title && (
                <h3 className="truncate text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                  {title}
                </h3>
              )}
              {description && (
                <p className="mt-0.5 line-clamp-2 text-xs text-zinc-600 dark:text-zinc-400">
                  {description}
                </p>
              )}
            </div>
          </button>
          {action}
        </div>
      )}
      {(!collapsible || open) && <div className="p-5">{children}</div>}
    </section>
  );
}
