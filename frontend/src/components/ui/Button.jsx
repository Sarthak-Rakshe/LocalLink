import clsx from "clsx";

const base =
  "inline-flex items-center justify-center gap-1.5 rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 disabled:pointer-events-none disabled:opacity-50";

const variants = {
  primary:
    "bg-linear-to-r from-indigo-600 to-violet-600 text-white hover:brightness-95 shadow-sm",
  secondary:
    "bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border border-indigo-200 dark:bg-indigo-500/15 dark:text-indigo-300 dark:border-indigo-500/20 dark:hover:bg-indigo-500/20",
  outline:
    "border border-zinc-300 bg-white hover:bg-zinc-50 dark:border-zinc-700 dark:bg-transparent dark:hover:bg-white/5",
  ghost: "hover:bg-zinc-100 dark:hover:bg-white/5",
  danger: "bg-rose-600 text-white hover:bg-rose-700",
};

const sizes = {
  sm: "h-8 px-2.5",
  md: "h-9 px-3",
  lg: "h-10 px-4",
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
      {...props}
    >
      {leftIcon && <span className="-ml-0.5 mr-0.5 size-4">{leftIcon}</span>}
      {loading && (
        <span className="mr-1 inline-block size-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
      )}
      <span>{children}</span>
      {rightIcon && <span className="ml-0.5 -mr-0.5 size-4">{rightIcon}</span>}
    </CompTag>
  );
}
