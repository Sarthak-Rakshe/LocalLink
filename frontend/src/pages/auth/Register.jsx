import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { RadioGroup, RadioGroupItem } from '../../components/ui/radio-group';
import { toast } from 'sonner';
import { Eye, EyeOff } from 'lucide-react';

const Register = () => {
  const [formData, setFormData] = useState({
    userName: '',
    userEmail: '',
    userContact: '',
    userType: 'CUSTOMER',
    userAddress: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleUserTypeChange = (value) => {
    setFormData({ ...formData, userType: value });
  };

  const validateForm = () => {
    if (formData.password !== formData.confirmPassword) {
      toast.error('Passwords do not match');
      return false;
    }
    if (formData.password.length < 6) {
      toast.error('Password must be at least 6 characters');
      return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.userEmail)) {
      toast.error('Please enter a valid email');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);

    try {
      const { confirmPassword, ...registerData } = formData;
      await register(registerData);
      toast.success('Registration successful! Please log in.');
      navigate('/login');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-12">
      <Card className="w-full max-w-2xl shadow-card">
        <CardHeader className="space-y-1 text-center">
          <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-lg bg-primary">
            <span className="text-2xl font-bold text-primary-foreground">LL</span>
          </div>
          <CardTitle className="text-2xl">Create your account</CardTitle>
          <CardDescription>Join LocalLink to connect with local services</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="userName">Full Name</Label>
                <Input
                  id="userName"
                  name="userName"
                  type="text"
                  placeholder="John Doe"
                  value={formData.userName}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="userEmail">Email</Label>
                <Input
                  id="userEmail"
                  name="userEmail"
                  type="email"
                  placeholder="john@example.com"
                  value={formData.userEmail}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="userContact">Contact Number</Label>
                <Input
                  id="userContact"
                  name="userContact"
                  type="tel"
                  placeholder="+1234567890"
                  value={formData.userContact}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="userAddress">Address</Label>
                <Input
                  id="userAddress"
                  name="userAddress"
                  type="text"
                  placeholder="123 Main St"
                  value={formData.userAddress}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label>I want to</Label>
              <RadioGroup value={formData.userType} onValueChange={handleUserTypeChange}>
                <div className="flex items-center space-x-2 rounded-lg border p-4 hover:bg-muted/50">
                  <RadioGroupItem value="CUSTOMER" id="customer" />
                  <Label htmlFor="customer" className="flex-1 cursor-pointer">
                    <div className="font-medium">Find Services</div>
                    <div className="text-sm text-muted-foreground">
                      Browse and book local services
                    </div>
                  </Label>
                </div>
                <div className="flex items-center space-x-2 rounded-lg border p-4 hover:bg-muted/50">
                  <RadioGroupItem value="PROVIDER" id="provider" />
                  <Label htmlFor="provider" className="flex-1 cursor-pointer">
                    <div className="font-medium">Offer Services</div>
                    <div className="text-sm text-muted-foreground">
                      List and manage your services
                    </div>
                  </Label>
                </div>
              </RadioGroup>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <div className="relative">
                  <Input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Create password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    disabled={loading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  >
                    {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirm Password</Label>
                <Input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Confirm password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                  disabled={loading}
                />
              </div>
            </div>

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Creating account...' : 'Create account'}
            </Button>

            <p className="text-center text-sm text-muted-foreground">
              Already have an account?{' '}
              <Link to="/login" className="font-medium text-primary hover:underline">
                Sign in
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default Register;
