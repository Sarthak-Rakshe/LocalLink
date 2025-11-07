export default function Footer() {
  return (
    <footer className="bg-white/60 backdrop-blur border-t border-white/60 dark:bg-zinc-900/60 dark:border-zinc-800">
      <div className="container-page h-12 flex items-center justify-between text-xs text-zinc-500">
        <p>
          © {new Date().getFullYear()} {""}
          <span className="font-medium">LocalLink</span>. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
