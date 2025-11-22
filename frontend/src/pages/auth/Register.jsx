import { useState } from "react";
import { useAuth } from "../../context/AuthContext.jsx";
import toast from "react-hot-toast";
import { useNavigate, Link } from "react-router-dom";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [contact, setContact] = useState("");
  const [address, setAddress] = useState("");
  const [userType, setUserType] = useState("CUSTOMER");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (!/^\d{10}$/.test(contact)) {
        throw new Error("Contact must be a valid 10-digit number");
      }
      const payload = {
        userName: username,
        userEmail: email,
        userPassword: password,
        userType,
        userContact: contact,
        userAddress: address,
      };
      await register(payload);
      toast.success("Account created. Welcome!");
      navigate("/dashboard", { replace: true });
    } catch (e) {
      const msg =
        e?.response?.data?.message || e?.message || "Registration failed";
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid place-items-center app-bg-gradient p-4">
      <div className="w-full max-w-2xl card glass-panel overflow-hidden animate-fade-in">
        {/* Header */}
        <div className="p-8 text-center border-b border-zinc-100 dark:border-zinc-800/50">
          <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-brand-100 text-brand-600 dark:bg-brand-900/30 dark:text-brand-400">
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-zinc-900 dark:text-white">Create an account</h1>
          <p className="mt-2 text-sm text-zinc-500 dark:text-zinc-400">
            Join our community to book or offer services
          </p>
        </div>

        <form onSubmit={onSubmit} className="p-8 grid gap-6 md:grid-cols-2">
          <div className="md:col-span-1 space-y-2">
            <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Username</label>
            <input
              type="text"
              placeholder="Choose a username"
              className="input-base"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
            />
          </div>
          <div className="md:col-span-1 space-y-2">
            <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Email</label>
            <input
              type="email"
              placeholder="you@example.com"
              className="input-base"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </div>
          <div className="md:col-span-1 space-y-2">
            <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Password</label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                className="input-base pr-10"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="new-password"
              />
              <button
                type="button"
                onClick={() => setShowPassword((s) => !s)}
                className="absolute right-2 top-1/2 -translate-y-1/2 rounded-lg p-1.5 text-zinc-400 hover:bg-zinc-100 hover:text-zinc-600 dark:hover:bg-zinc-800 dark:hover:text-zinc-300 transition-colors"
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? (
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M3 3l18 18" />
                    <path d="M10.58 10.58a3 3 0 1 0 4.24 4.24" />
                    <path d="M9.88 5.08A9.77 9.77 0 0 1 12 5c7 0 10 7 10 7a18.93 18.93 0 0 1-4.23 5.15" />
                    <path d="M6.61 6.61A18.92 18.92 0 0 0 2 12s3 7 10 7a9.88 9.88 0 0 0 3.39-.61" />
                  </svg>
                ) : (
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12Z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                )}
              </button>
            </div>
            <p className="text-xs text-zinc-500 dark:text-zinc-400">
              8+ chars, mix of upper, lower, digit, special.
            </p>
          </div>
          <div className="md:col-span-1 space-y-2">
            <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Contact</label>
            <input
              type="tel"
              placeholder="10-digit phone number"
              className="input-base"
              value={contact}
              onChange={(e) => setContact(e.target.value)}
              required
              autoComplete="tel"
              pattern="[0-9]{10}"
            />
          </div>
          <div className="md:col-span-2 space-y-2">
            <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">Address</label>
            <input
              type="text"
              placeholder="Street, City, State"
              className="input-base"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              required
              autoComplete="street-address"
            />
          </div>

          {/* Modern account type selection */}
          <div className="md:col-span-2 space-y-2">
            <label className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
              Account type
            </label>
            <div role="radiogroup" className="grid grid-cols-2 gap-4">
              {[
                { id: "CUSTOMER", title: "Customer", desc: "I want to book services" },
                { id: "PROVIDER", title: "Provider", desc: "I want to offer services" },
              ].map((opt) => {
                const active = userType === opt.id;
                return (
                  <button
                    key={opt.id}
                    type="button"
                    role="radio"
                    aria-checked={active}
                    onClick={() => setUserType(opt.id)}
                    className={
                      "flex h-full flex-col items-start rounded-xl border p-4 text-left transition-all duration-200 " +
                      (active
                        ? "border-brand-500 bg-brand-50/50 ring-1 ring-brand-500 dark:bg-brand-900/20"
                        : "border-zinc-200 hover:border-brand-300 hover:bg-zinc-50 dark:border-zinc-700 dark:hover:bg-zinc-800/50")
                    }
                  >
                    <div className="flex items-center gap-2 w-full mb-1">
                      <div className={
                        "flex h-4 w-4 items-center justify-center rounded-full border " +
                        (active ? "border-brand-600 bg-brand-600" : "border-zinc-300 bg-transparent")
                      }>
                        {active && <div className="h-1.5 w-1.5 rounded-full bg-white" />}
                      </div>
                      <span className={"font-semibold " + (active ? "text-brand-700 dark:text-brand-300" : "text-zinc-900 dark:text-zinc-100")}>
                        {opt.title}
                      </span>
                    </div>
                    <span className="text-xs text-zinc-500 dark:text-zinc-400 pl-6">
                      {opt.desc}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="md:col-span-2 pt-2">
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary w-full py-3 text-base shadow-lg shadow-brand-500/25"
            >
              {loading ? (
                <>
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Creating account...
                </>
              ) : (
                "Create account"
              )}
            </button>
          </div>
        </form>

        <div className="bg-zinc-50/50 p-6 text-center dark:bg-zinc-900/50 border-t border-zinc-100 dark:border-zinc-800/50">
          <p className="text-sm text-zinc-600 dark:text-zinc-400">
            Already have an account?{" "}
            <Link className="font-medium text-brand-600 hover:text-brand-500 hover:underline dark:text-brand-400" to="/login">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

