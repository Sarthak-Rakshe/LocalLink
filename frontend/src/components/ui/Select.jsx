import clsx from "clsx";

export default function Select({ className, children, ...props }) {
  return (
    <select
      className={clsx(
        "w-full rounded-md border border-zinc-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/50",
        className
      )}
      {...props}
    >
      {children}
    </select>
  );
}
