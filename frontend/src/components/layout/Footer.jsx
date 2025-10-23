export default function Footer() {
  return (
    <footer className="mt-auto border-t border-gray-200 bg-white/60">
      <div className="container-app py-4 text-xs text-gray-500">
        © {new Date().getFullYear()} LocalLink. All rights reserved.
      </div>
    </footer>
  );
}
