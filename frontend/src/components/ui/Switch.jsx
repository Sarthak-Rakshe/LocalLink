import { Switch as HSwitch } from "@headlessui/react";
import clsx from "clsx";

export default function Switch({ checked, onChange, label, className }) {
  return (
    <HSwitch.Group
      as="div"
      className={clsx("flex items-center gap-2", className)}
    >
      {label && (
        <HSwitch.Label className="text-sm font-medium text-zinc-800">
          {label}
        </HSwitch.Label>
      )}
      <HSwitch
        checked={checked}
        onChange={onChange}
        className={clsx(
          checked ? "bg-indigo-600" : "bg-zinc-300",
          "relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-600"
        )}
      >
        <span
          className={clsx(
            checked ? "translate-x-6" : "translate-x-1",
            "inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
          )}
        />
      </HSwitch>
    </HSwitch.Group>
  );
}
