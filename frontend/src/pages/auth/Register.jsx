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
    <div className="min-h-screen flex items-center justify-center bg-zinc-50 p-4">
      <div className="w-full max-w-md card p-6">
        <h1 className="text-xl font-semibold mb-1">Create account</h1>
        <p className="text-sm text-zinc-500 mb-6">Sign up to get started</p>
        <form onSubmit={onSubmit} className="space-y-4">
          <div>
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
          <div>
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
          <div>
            <label className="block text-sm font-medium mb-1">Password</label>
            <input
              type="password"
              className="input-base"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="new-password"
            />
            <p className="mt-1 text-xs text-zinc-500">
              Must be 8+ chars, with upper, lower, digit, and special character.
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Contact</label>
            <input
              type="tel"
              placeholder="10-digit phone number"
              className="input-base"
              value={contact}
              onChange={(e) => setContact(e.target.value)}
              required
              autoComplete="tel"
              pattern="\d{10}"
            />
          </div>
          <div>
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
          <div>
            <label className="block text-sm font-medium mb-1">
              Account type
            </label>
            <select
              className="input-base"
              value={userType}
              onChange={(e) => setUserType(e.target.value)}
            >
              <option value="CUSTOMER">Customer</option>
              <option value="PROVIDER">Provider</option>
            </select>
          </div>
          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary w-full py-2.5"
          >
            {loading ? "Creating account…" : "Create account"}
          </button>
        </form>
        <p className="mt-4 text-center text-sm text-zinc-600">
          Already have an account?{" "}
          <Link className="text-blue-600 hover:underline" to="/login">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
