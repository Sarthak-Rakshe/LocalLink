import clsx from "clsx";

export default function Input({ label, id, className, ...props }) {
  return (
    <label className="label" htmlFor={id}>
      {label}
      <input id={id} className={clsx("input", "mt-1", className)} {...props} />
    </label>
  );
}
