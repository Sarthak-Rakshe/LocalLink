import clsx from "clsx";

export default function Skeleton({ className }) {
  return (
    <div className={clsx("animate-pulse rounded-md bg-zinc-200", className)} />
  );
}
