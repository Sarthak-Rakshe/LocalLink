import { useState } from "react";
import { useAuth } from "../../context/AuthContext.jsx";
import toast from "react-hot-toast";
import { Link, useNavigate } from "react-router-dom";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Backend LoginRequest expects { username, password }
      await login({ username, password });
      toast.success("Welcome back!");
      // Router will redirect by userType when hitting /dashboard
      navigate("/dashboard", { replace: true });
    } catch (e) {
      toast.error(e?.response?.data?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid place-items-center bg-linear-to-br from-brand-50 to-white dark:from-zinc-950 dark:to-zinc-900 app-bg p-4">
      <div className="w-full max-w-md card overflow-hidden">
        {/* Header */}
        <div className="bg-linear-to-r from-indigo-600 to-violet-600 p-6 text-white">
          <h1 className="text-xl font-semibold text-white">Sign in</h1>
          <p className="text-sm text-white">Access your dashboard</p>
        </div>

        {/* Form */}
        <form onSubmit={onSubmit} className="p-6 space-y-5">
          <div>
            <label className="block text-sm font-medium mb-1">Username</label>
            <div className="relative">
              <input
                type="text"
                placeholder="Enter username"
                className="input-base pl-10"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                autoComplete="username"
              />
              {/* user icon */}
              <svg
                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-400"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
              >
                <path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Z" />
                <path d="M20 21a8 8 0 1 0-16 0" />
              </svg>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Password</label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                className="input-base pr-10"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPassword((s) => !s)}
                className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md p-1 text-zinc-500 hover:bg-zinc-100"
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? (
                  <svg
                    className="h-5 w-5"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                  >
                    <path d="M3 3l18 18" />
                    <path d="M10.58 10.58a3 3 0 1 0 4.24 4.24" />
                    <path d="M9.88 5.08A9.77 9.77 0 0 1 12 5c7 0 10 7 10 7a18.93 18.93 0 0 1-4.23 5.15" />
                    <path d="M6.61 6.61A18.92 18.92 0 0 0 2 12s3 7 10 7a9.88 9.88 0 0 0 3.39-.61" />
                  </svg>
                ) : (
                  <svg
                    className="h-5 w-5"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                  >
                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12Z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                )}
              </button>
            </div>
          </div>
          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary w-full py-2.5"
          >
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>

        <div className="border-t bg-white p-6 dark:border-zinc-800 dark:bg-zinc-900">
          <p className="text-center text-sm text-zinc-600 dark:text-zinc-400">
            Don't have an account?{" "}
            <Link className="text-blue-600 hover:underline" to="/register">
              Create one
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
