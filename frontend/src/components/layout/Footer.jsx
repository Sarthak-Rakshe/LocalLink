export default function Footer() {
  return (
    <footer className="border-t bg-white/60 backdrop-blur">
      <div className="container-page h-12 flex items-center justify-between text-xs text-zinc-500">
        <p>
          © {new Date().getFullYear()}{" "}
          <span className="font-medium">LocalLink</span>. All rights reserved.
        </p>
        <p className="hidden sm:block">
          Built with React, Vite, and Tailwind CSS.
        </p>
      </div>
    </footer>
  );
}
