export default function Footer() {
  return (
    <footer className="border-t bg-white">
      <div className="mx-auto max-w-6xl px-4 py-4 text-sm text-gray-600">
        © {new Date().getFullYear()} LocalLink. All rights reserved.
      </div>
    </footer>
  );
}
