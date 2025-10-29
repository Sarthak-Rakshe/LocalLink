import clsx from "clsx";

export function Input({ className, ...props }) {
  return (
    <input
      className={clsx(
        "w-full rounded-md border border-zinc-300 px-3 py-2 text-sm shadow-sm placeholder:text-zinc-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/50",
        className
      )}
      {...props}
    />
  );
}

export function Label({ className, ...props }) {
  return (
    <label
      className={clsx("mb-1 block text-sm font-medium", className)}
      {...props}
    />
  );
}

export function HelpText({ className, ...props }) {
  return (
    <p className={clsx("mt-1 text-xs text-zinc-500", className)} {...props} />
  );
}
