import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authApi } from "../api/auth";
import { Card, CardBody } from "../components/ui/Card.jsx";
import Button from "../components/ui/Button.jsx";
import Input from "../components/ui/Input.jsx";

export default function Login() {
  const [form, setForm] = useState({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const hasToken = localStorage.getItem("authToken");
    if (hasToken) navigate("/home", { replace: true });
  }, [navigate]);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await authApi.login(form);
      // Persist tokens for axios interceptor
      localStorage.setItem("authToken", res.token);
      localStorage.setItem("refreshToken", res.refreshToken);
      localStorage.setItem("tokenType", res.tokenType || "Bearer");
      // Optionally store user profile
      if (res.userResponse) {
        localStorage.setItem("user", JSON.stringify(res.userResponse));
        // Persist userType separately for quick role checks
        if (res.userResponse.userType) {
          localStorage.setItem("userType", String(res.userResponse.userType));
        }
      }
      navigate("/home");
    } catch (err) {
      const msg = err?.response?.data?.message || "Login failed";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid place-items-center">
      <Card className="w-full max-w-sm">
        <CardBody>
          <h2 className="mb-1">Login</h2>
          {error && (
            <div className="text-sm text-red-700 mb-2">{String(error)}</div>
          )}
          <form onSubmit={onSubmit} className="grid gap-3 mt-2">
            <Input
              label="Username"
              name="username"
              id="username"
              value={form.username}
              onChange={onChange}
              required
            />
            <Input
              label="Password"
              id="password"
              type="password"
              name="password"
              value={form.password}
              onChange={onChange}
              required
            />
            <Button type="submit" disabled={loading} className="mt-2">
              {loading ? "Logging in..." : "Login"}
            </Button>
          </form>
          <div className="text-sm mt-3">
            Don’t have an account? <Link to="/register">Register</Link>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
