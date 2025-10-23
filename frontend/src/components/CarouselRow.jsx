import { useState, useMemo } from "react";

export default function CarouselRow({ title, children, itemsPerPage = 4 }) {
  const items = useMemo(
    () => (Array.isArray(children) ? children : [children]).filter(Boolean),
    [children]
  );
  const [start, setStart] = useState(0);
  const canPrev = start > 0;
  const canNext = start + itemsPerPage < items.length;
  const pageItems = items.slice(start, start + itemsPerPage);

  const prev = () => setStart((s) => Math.max(0, s - itemsPerPage));
  const next = () =>
    setStart((s) =>
      Math.min(Math.max(0, items.length - itemsPerPage), s + itemsPerPage)
    );

  return (
    <section className="mb-8">
      <div className="flex items-baseline justify-between mb-3">
        <h2 className="text-lg font-semibold m-0">{title}</h2>
      </div>
      <div className="relative">
        {/* Content */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {pageItems}
        </div>

        {/* Left/Right transparent controls */}
        {items.length > itemsPerPage && (
          <>
            <button
              type="button"
              aria-label="Previous"
              onClick={prev}
              disabled={!canPrev}
              className="absolute left-0 top-1/2 -translate-y-1/2 rounded-full p-2 text-gray-700 hover:bg-black/5 disabled:opacity-30"
              style={{ backgroundColor: "transparent" }}
            >
              ‹
            </button>
            <button
              type="button"
              aria-label="Next"
              onClick={next}
              disabled={!canNext}
              className="absolute right-0 top-1/2 -translate-y-1/2 rounded-full p-2 text-gray-700 hover:bg-black/5 disabled:opacity-30"
              style={{ backgroundColor: "transparent" }}
            >
              ›
            </button>
          </>
        )}
      </div>
    </section>
  );
}
