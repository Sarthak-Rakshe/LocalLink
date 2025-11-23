export function Table({ children }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-[var(--border-base)]">
      <table className="w-full border-collapse text-sm text-left">{children}</table>
    </div>
  );
}

export function THead({ children }) {
  return (
    <thead className="bg-[var(--bg-surface-hover)] text-muted">
      {children}
    </thead>
  );
}

export function TBody({ children }) {
  return (
    <tbody className="divide-y divide-[var(--border-subtle)] bg-[var(--bg-surface)]">
      {children}
    </tbody>
  );
}

export function TR({ children }) {
  return (
    <tr className="transition-colors hover:bg-[var(--bg-surface-hover)]">
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
    <td className={`px-4 py-3 align-middle text-default ${className || ""}`}>
      {children}
    </td>
  );
}
