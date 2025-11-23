import clsx from "clsx";

export default function Select({ className, children, ...props }) {
  return (
    <select
      className={clsx(
        // Reuse input base so two stay visually in sync
        "input-base appearance-none bg-[var(--bg-surface)] text-default",
        // Help some browsers pick dark popover styling
        "[color-scheme:light] dark:[color-scheme:dark]",
        className
      )}
      {...props}
    >
      {children}
    </select>
  );
}
