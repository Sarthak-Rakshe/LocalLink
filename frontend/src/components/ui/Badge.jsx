import clsx from "clsx";

const styles = {
  gray: "bg-zinc-100 text-zinc-700",
  amber: "bg-amber-100 text-amber-800",
  green: "bg-emerald-100 text-emerald-800",
  red: "bg-rose-100 text-rose-800",
  blue: "bg-blue-100 text-blue-800",
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
