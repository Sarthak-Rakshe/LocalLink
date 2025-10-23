import clsx from "clsx";

export default function Button({
  as: asProp = "button",
  variant = "primary",
  className,
  ...props
}) {
  const Component = asProp;
  return (
    <Component
      className={clsx(
        "btn",
        variant === "primary" && "btn-primary",
        variant === "ghost" && "btn-ghost",
        className
      )}
      {...props}
    />
  );
}
