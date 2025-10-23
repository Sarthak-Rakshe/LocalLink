import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authApi } from "../api/auth";
import { Card, CardBody } from "../components/ui/Card.jsx";
import Button from "../components/ui/Button.jsx";
import Input from "../components/ui/Input.jsx";

const initial = {
  userName: "",
  userEmail: "",
  userContact: "",
  userType: "CUSTOMER", // default selection
  userPassword: "",
  userAddress: "",
  isActive: true,
};

export default function Register() {
  const [form, setForm] = useState(initial);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const hasToken = localStorage.getItem("authToken");
    if (hasToken) navigate("/home", { replace: true });
  }, [navigate]);

  const onChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((f) => ({ ...f, [name]: type === "checkbox" ? checked : value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      await authApi.register(form);
      setSuccess("Registration successful. You can now log in.");
      // Optionally auto-login or redirect after a short delay
      setTimeout(() => navigate("/login"), 800);
    } catch (err) {
      const msg = err?.response?.data?.message || "Registration failed";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid place-items-center">
      <Card className="w-full max-w-lg">
        <CardBody>
          <h2 className="mb-1">Create your account</h2>
          <p className="text-gray-600 mt-0 mb-3">
            Choose your role, then provide details.
          </p>
          {error && (
            <div className="text-sm text-red-700 mb-2">{String(error)}</div>
          )}
          {success && (
            <div className="text-sm text-emerald-700 mb-2">{success}</div>
          )}

          <form onSubmit={onSubmit} className="grid gap-3 mt-2">
            <label className="label" htmlFor="userType">
              I am a
              <select
                id="userType"
                name="userType"
                value={form.userType}
                onChange={onChange}
                className="input mt-1"
              >
                <option value="CUSTOMER">Customer</option>
                <option value="PROVIDER">Service Provider</option>
              </select>
            </label>

            <Input
              label="Username"
              id="userName"
              name="userName"
              value={form.userName}
              onChange={onChange}
              required
              pattern="^[a-zA-Z0-9_]+$"
              title="Only letters, numbers, underscores. No spaces."
            />
            <Input
              label="Email"
              id="userEmail"
              type="email"
              name="userEmail"
              value={form.userEmail}
              onChange={onChange}
              required
            />
            <Input
              label="Contact (10 digits)"
              id="userContact"
              name="userContact"
              value={form.userContact}
              onChange={onChange}
              required
              pattern="^[0-9]{10}$"
              title="Enter a valid 10-digit number"
            />
            <Input
              label="Password"
              id="userPassword"
              type="password"
              name="userPassword"
              value={form.userPassword}
              onChange={onChange}
              required
              pattern="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?!.*\\s).{8,}$"
              title="Min 8 chars, include uppercase, lowercase, digit, special character"
            />
            <Input
              label="Address"
              id="userAddress"
              name="userAddress"
              value={form.userAddress}
              onChange={onChange}
              required
            />

            <label className="label flex items-center gap-2">
              <input
                type="checkbox"
                name="isActive"
                checked={form.isActive}
                onChange={onChange}
              />
              Active account
            </label>

            <Button type="submit" disabled={loading} className="mt-2">
              {loading ? "Registering..." : "Register"}
            </Button>
            <div className="text-sm">
              Already have an account? <Link to="/login">Login</Link>
            </div>
          </form>
        </CardBody>
      </Card>
    </div>
  );
}
