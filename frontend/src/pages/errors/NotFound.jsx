import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center p-6 text-center">
      <div>
        <h1 className="text-3xl font-bold">404</h1>
        <p className="text-zinc-600 mt-2">
          The page you’re looking for was not found.
        </p>
        <Link
          to="/"
          className="inline-block mt-6 text-indigo-600 hover:underline"
        >
          Go home
        </Link>
      </div>
    </div>
  );
}
