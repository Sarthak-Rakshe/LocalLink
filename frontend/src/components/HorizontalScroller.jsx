import { Link } from "react-router-dom";

export default function HorizontalScroller({ title, to = null, children }) {
  return (
    <section className="mb-8">
      <div className="flex items-baseline justify-between mb-3">
        <h2 className="text-lg font-semibold m-0">{title}</h2>
        {to && (
          <Link to={to} className="text-sm text-indigo-600 hover:underline">
            View all
          </Link>
        )}
      </div>
      <div className="overflow-x-auto">
        <div className="flex gap-3 snap-x snap-mandatory pb-2">{children}</div>
      </div>
    </section>
  );
}
