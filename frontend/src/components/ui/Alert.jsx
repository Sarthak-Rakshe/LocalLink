import clsx from "clsx";

const styles = {
  info: "border-blue-200 bg-blue-50 text-blue-700",
  success: "border-emerald-200 bg-emerald-50 text-emerald-700",
  warning: "border-amber-200 bg-amber-50 text-amber-800",
  error: "border-rose-200 bg-rose-50 text-rose-700",
};

export default function Alert({
  variant = "info",
  title,
  children,
  className,
}) {
  return (
    <div
      className={clsx(
        "rounded-md border px-3 py-2 text-sm",
        styles[variant] || styles.info,
        className
      )}
      role="alert"
    >
      {title && <div className="font-medium">{title}</div>}
      {children && <div className={title ? "mt-1" : undefined}>{children}</div>}
    </div>
  );
}
