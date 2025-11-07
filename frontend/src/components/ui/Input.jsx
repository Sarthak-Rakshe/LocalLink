import clsx from "clsx";

export function Input({ className, ...props }) {
  return <input className={clsx("input-base", className)} {...props} />;
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
