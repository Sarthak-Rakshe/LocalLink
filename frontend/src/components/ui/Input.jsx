import clsx from "clsx";

export function Input({ className, ...props }) {
  return (
    <input
      className={clsx(
        "input-base",
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
