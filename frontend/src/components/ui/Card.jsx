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
      className={`rounded-xl border border-zinc-200/60 bg-white/80 shadow-sm backdrop-blur-sm transition-all dark:border-zinc-800/60 dark:bg-zinc-900/80 ${className ?? ""
        }`}
    >
      {(title || action || description) && (
        <div className="flex items-start justify-between gap-4 border-b border-zinc-100 px-6 py-4 dark:border-zinc-800/50">
          <button
            type="button"
            className={`group -m-2 flex min-w-0 flex-1 items-start gap-3 p-2 text-left ${collapsible ? "cursor-pointer" : "cursor-default"
              }`}
            onClick={collapsible ? () => setOpen((o) => !o) : undefined}
            aria-expanded={collapsible ? open : undefined}
          >
            {collapsible && (
              <div className={`mt-0.5 flex h-5 w-5 items-center justify-center rounded-md bg-zinc-100 text-zinc-500 transition-all group-hover:bg-brand-50 group-hover:text-brand-600 dark:bg-zinc-800 dark:text-zinc-400 dark:group-hover:bg-brand-900/20 dark:group-hover:text-brand-400 ${open ? "rotate-90" : "rotate-0"
                }`}>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  className="size-3.5"
                  aria-hidden="true"
                >
                  <path fillRule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clipRule="evenodd" />
                </svg>
              </div>
            )}
            <div className="min-w-0">
              {title && (
                <h3 className="truncate text-base font-semibold text-zinc-900 dark:text-zinc-100">
                  {title}
                </h3>
              )}
              {description && (
                <p className="mt-1 line-clamp-2 text-sm text-zinc-500 dark:text-zinc-400">
                  {description}
                </p>
              )}
            </div>
          </button>
          {action}
        </div>
      )}
      {(!collapsible || open) && <div className="p-6 animate-slide-up">{children}</div>}
    </section>
  );
}
