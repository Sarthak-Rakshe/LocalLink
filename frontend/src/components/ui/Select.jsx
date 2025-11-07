import clsx from "clsx";

export default function Select({ className, children, ...props }) {
  return (
    <select
      className={clsx(
        // Reuse input base so two stay visually in sync
        "input-base appearance-none bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100",
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
