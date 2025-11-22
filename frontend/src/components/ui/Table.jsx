export function Table({ children }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-zinc-200 dark:border-zinc-800">
      <table className="w-full border-collapse text-sm text-left">{children}</table>
    </div>
  );
}

export function THead({ children }) {
  return (
    <thead className="bg-zinc-50 text-zinc-500 dark:bg-zinc-900/50 dark:text-zinc-400">
      {children}
    </thead>
  );
}

export function TBody({ children }) {
  return (
    <tbody className="divide-y divide-zinc-200 bg-white dark:divide-zinc-800 dark:bg-transparent">
      {children}
    </tbody>
  );
}

export function TR({ children }) {
  return (
    <tr className="transition-colors hover:bg-zinc-50/80 dark:hover:bg-zinc-800/50">
      {children}
    </tr>
  );
}

export function TH({ children, className }) {
  return (
    <th
      className={`px-4 py-3 font-medium whitespace-nowrap ${className || ""}`}
    >
      {children}
    </th>
  );
}

export function TD({ children, className }) {
  return (
    <td className={`px-4 py-3 align-middle text-zinc-700 dark:text-zinc-300 ${className || ""}`}>
      {children}
    </td>
  );
}
