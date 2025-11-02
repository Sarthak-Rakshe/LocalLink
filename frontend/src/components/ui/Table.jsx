export function Table({ children }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-sm">{children}</table>
    </div>
  );
}

export function THead({ children }) {
  return <thead className="bg-zinc-50 text-zinc-600">{children}</thead>;
}

export function TBody({ children }) {
  return <tbody className="divide-y">{children}</tbody>;
}

export function TR({ children }) {
  return <tr className="hover:bg-zinc-50/50">{children}</tr>;
}

export function TH({ children, className }) {
  return (
    <th
      className={`border-b px-3 py-2 text-left font-medium ${className || ""}`}
    >
      {children}
    </th>
  );
}

export function TD({ children, className }) {
  return <td className={`px-3 py-2 ${className || ""}`}>{children}</td>;
}
