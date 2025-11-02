export default function Card({
  title,
  description,
  action,
  children,
  className,
}) {
  return (
    <section
      className={`rounded-xl border border-zinc-200 bg-white shadow-sm ${
        className ?? ""
      }`}
    >
      {(title || action || description) && (
        <div className="flex items-start justify-between gap-3 border-b px-5 py-3.5">
          <div>
            {title && (
              <h3 className="text-sm font-semibold text-zinc-900">{title}</h3>
            )}
            {description && (
              <p className="mt-0.5 text-xs text-zinc-600">{description}</p>
            )}
          </div>
          {action}
        </div>
      )}
      <div className="p-5">{children}</div>
    </section>
  );
}
