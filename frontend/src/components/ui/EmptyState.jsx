export default function EmptyState({ title = "Nothing here yet", message }) {
  return (
    <div className="text-center text-sm text-zinc-600">
      <div className="font-medium text-zinc-800">{title}</div>
      {message && <div className="mt-1 text-zinc-500">{message}</div>}
    </div>
  );
}
