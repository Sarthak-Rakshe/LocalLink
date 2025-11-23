import { Fragment } from "react";

export default function Modal({ open, onClose, title, children, footer }) {
  // Don't render anything when closed to avoid stray headers or layout glitches.
  if (!open) return null;
  return (
    <Fragment>
      <div
        className="fixed inset-0 z-40 bg-black/30 transition-opacity opacity-100"
        onClick={onClose}
      />
      <div
        className="fixed inset-0 z-50 flex items-center justify-center p-4"
        aria-hidden={!open}
      >
        <div className="w-full max-w-lg rounded-xl border border-[var(--border-base)] bg-[var(--bg-surface)] shadow-xl">
          {(title || onClose) && (
            <div className="flex items-center justify-between border-b border-[var(--border-subtle)] px-4 py-3">
              <h3 className="text-sm font-semibold">{title}</h3>
              {onClose && (
                <button
                  type="button"
                  className="rounded p-1 text-muted hover:bg-[var(--bg-surface-hover)]"
                  onClick={onClose}
                  aria-label="Close"
                >
                  ✕
                </button>
              )}
            </div>
          )}
          <div className="p-4">{children}</div>
          {footer && <div className="border-t border-[var(--border-subtle)] px-4 py-3">{footer}</div>}
        </div>
      </div>
    </Fragment>
  );
}
