import clsx from "clsx";

const base =
  "inline-flex items-center justify-center gap-2 rounded-lg text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 dark:focus-visible:ring-offset-zinc-950";

const variants = {
  primary:
    "bg-brand-600 text-white shadow-md shadow-brand-500/20 hover:bg-brand-700 hover:shadow-lg hover:shadow-brand-500/30 active:scale-[0.98]",
  secondary:
    "bg-[var(--bg-surface)] text-default border border-[var(--border-base)] hover:bg-[var(--bg-surface-hover)] hover:text-default dark:bg-[var(--bg-surface)] dark:text-default dark:border-[var(--border-base)] dark:hover:bg-[var(--bg-surface-hover)] dark:hover:text-default",
  outline:
    "border border-[var(--border-base)] bg-transparent text-default hover:bg-[var(--bg-surface-hover)] hover:text-default dark:border-[var(--border-base)] dark:text-default dark:hover:bg-[var(--bg-surface-hover)] dark:hover:text-default",
  ghost:
    "text-muted hover:bg-[var(--bg-surface-hover)] hover:text-default dark:text-muted dark:hover:bg-[var(--bg-surface-hover)] dark:hover:text-default",
  danger:
    "bg-red-600 text-white shadow-sm hover:bg-red-700 dark:bg-red-600 dark:hover:bg-red-700",
  success:
    "bg-emerald-600 text-white shadow-sm hover:bg-emerald-700 dark:bg-emerald-600 dark:hover:bg-emerald-700",
};

const sizes = {
  sm: "h-8 px-3 text-xs",
  md: "h-10 px-4 text-sm",
  lg: "h-12 px-6 text-base",
  icon: "h-10 w-10 p-2",
};

export default function Button({
  as = "button",
  variant = "primary",
  size = "md",
  className,
  children,
  leftIcon,
  rightIcon,
  loading = false,
  fullWidth = false,
  ...props
}) {
  const CompTag = as || "button";
  return (
    <CompTag
      className={clsx(
        base,
        variants[variant] || variants.primary,
        sizes[size],
        fullWidth && "w-full",
        className
      )}
      aria-busy={loading}
      disabled={loading || props.disabled}
      {...props}
    >
      {loading ? (
        <span className="mr-1 inline-block size-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
      ) : (
        leftIcon && <span className="-ml-0.5 size-4">{leftIcon}</span>
      )}
      <span>{children}</span>
      {!loading && rightIcon && <span className="ml-0.5 -mr-0.5 size-4">{rightIcon}</span>}
    </CompTag>
  );
}
