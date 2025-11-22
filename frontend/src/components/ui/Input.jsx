import clsx from "clsx";

export function Input({ className, ...props }) {
  return (
    <input
      className={clsx(
        "w-full rounded-lg border border-zinc-200 bg-white px-4 py-2.5 text-sm shadow-sm outline-none transition-all placeholder:text-zinc-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 dark:border-zinc-800 dark:bg-zinc-900/50 dark:text-zinc-100 dark:placeholder:text-zinc-600 dark:focus:border-brand-500",
        className
      )}
      {...props}
    />
  );
}

export function Label({ className, ...props }) {
  return (
    <label
      className={clsx("mb-1.5 block text-sm font-medium text-zinc-700 dark:text-zinc-300", className)}
      {...props}
    />
  );
}

export function HelpText({ className, ...props }) {
  return (
    <p className={clsx("mt-1.5 text-xs text-zinc-500 dark:text-zinc-400", className)} {...props} />
  );
}
