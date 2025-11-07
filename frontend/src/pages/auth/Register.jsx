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
      // Backend expects UserRegistrationRequest fields
      // {
      //   userName, userEmail, userContact, userType, userPassword, userAddress
      // }
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
    <div className="min-h-screen grid place-items-center bg-linear-to-br from-brand-50 to-white dark:from-zinc-950 dark:to-zinc-900 p-4">
      <div className="w-full max-w-2xl card overflow-hidden">
        {/* Header */}
        <div className="bg-linear-to-r from-indigo-600 to-violet-600 p-6 text-white">
          <h1 className="text-xl font-semibold text-white">Create account</h1>
          <p className="text-sm text-white">Sign up to get started</p>
        </div>

        <form onSubmit={onSubmit} className="p-6 grid gap-6 md:grid-cols-2">
          <div className="md:col-span-1">
            <label className="block text-sm font-medium mb-1">Username</label>
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
          <div className="md:col-span-1">
            <label className="block text-sm font-medium mb-1">Email</label>
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
          <div className="md:col-span-1">
            <label className="block text-sm font-medium mb-1">Password</label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                className="input-base pr-10"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="new-password"
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
            <p className="mt-1 text-xs text-zinc-500">
              Must be 8+ chars, with upper, lower, digit, and special character.
            </p>
          </div>
          <div className="md:col-span-1">
            <label className="block text-sm font-medium mb-1">Contact</label>
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
          <div className="md:col-span-2">
            <label className="block text-sm font-medium mb-1">Address</label>
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
          <div className="md:col-span-2">
            <label className="block text-sm font-medium mb-2">
              Account type
            </label>
            <div role="radiogroup" className="grid grid-cols-2 gap-3">
              {[
                { id: "CUSTOMER", title: "Customer", desc: "Book services" },
                { id: "PROVIDER", title: "Provider", desc: "Offer services" },
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
                      "flex h-full flex-col items-start rounded-lg border p-4 text-left transition-colors " +
                      (active
                        ? "border-indigo-600 bg-indigo-50 ring-2 ring-indigo-600"
                        : "border-zinc-300 hover:bg-zinc-50")
                    }
                  >
                    <div className="flex items-center gap-2">
                      <span
                        className={
                          "inline-flex h-2.5 w-2.5 rounded-full " +
                          (active ? "bg-indigo-600" : "bg-zinc-300")
                        }
                        aria-hidden
                      />
                      <span className="font-medium">{opt.title}</span>
                    </div>
                    <span className="mt-1 text-xs text-zinc-500">
                      {opt.desc}
                    </span>
                  </button>
                );
              })}
            </div>
            {/* Hidden input to keep form semantics if needed */}
            <input type="hidden" name="userType" value={userType} />
          </div>

          <div className="md:col-span-2">
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary w-full py-2.5"
            >
              {loading ? "Creating account…" : "Create account"}
            </button>
          </div>
        </form>

        <div className="border-t bg-white p-6 dark:border-zinc-800 dark:bg-zinc-900">
          <p className="text-center text-sm text-zinc-600 dark:text-zinc-400">
            Already have an account?{" "}
            <Link className="text-blue-600 hover:underline" to="/login">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
