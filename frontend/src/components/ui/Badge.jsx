import clsx from "clsx";

const styles = {
  gray: "bg-zinc-100 text-zinc-700 dark:bg-white/5 dark:text-zinc-300",
  amber: "bg-amber-100 text-amber-800 dark:bg-amber-500/15 dark:text-amber-300",
  green:
    "bg-emerald-100 text-emerald-800 dark:bg-emerald-500/15 dark:text-emerald-300",
  red: "bg-rose-100 text-rose-800 dark:bg-rose-500/15 dark:text-rose-300",
  blue: "bg-blue-100 text-blue-800 dark:bg-blue-500/15 dark:text-blue-300",
};

export default function Badge({ color = "gray", children, className }) {
  return (
    <span
      className={clsx(
        "inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-medium uppercase tracking-wide",
        styles[color] || styles.gray,
        className
      )}
    >
      {children}
    </span>
  );
}
