export default function Card({ title, action, children }) {
  return (
    <section className="rounded-xl border bg-white shadow-sm">
      {(title || action) && (
        <div className="flex items-center justify-between border-b px-4 py-3">
          {title && (
            <h3 className="text-sm font-semibold text-zinc-900">{title}</h3>
          )}
          {action}
        </div>
      )}
      <div className="p-4">{children}</div>
    </section>
  );
}
