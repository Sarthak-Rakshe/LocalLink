export default function PageHeader({ title, description, actions }) {
  return (
    <div className="mb-4 flex flex-col gap-3 sm:mb-5 sm:flex-row sm:items-end sm:justify-between">
      <div>
        {title && (
          <h1 className="text-2xl font-semibold tracking-tight text-zinc-900">
            {title}
          </h1>
        )}
        {description && (
          <p className="mt-1 text-sm text-zinc-600">{description}</p>
        )}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  );
}
