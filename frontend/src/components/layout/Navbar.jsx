import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { Button } from "../ui/button";
import { LogOut, User, Menu } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "../ui/dropdown-menu";

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  return (
    <nav className="sticky top-0 z-50 w-full border-b bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/80">
      <div className="container flex h-16 items-center justify-between px-4">
        <Link
          to={isAuthenticated ? "/dashboard" : "/"}
          className="flex items-center space-x-2"
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
            <span className="text-lg font-bold text-primary-foreground">
              LL
            </span>
          </div>
          <span className="text-xl font-bold">LocalLink</span>
        </Link>

        <div className="flex items-center gap-4">
          {isAuthenticated ? (
            <>
              <Link to="/dashboard" className="hidden md:block">
                <Button variant="ghost">Dashboard</Button>
              </Link>
              <Link to="/services" className="hidden md:block">
                <Button variant="ghost">Browse Services</Button>
              </Link>
              <Link to="/nearby" className="hidden md:block">
                <Button variant="ghost">Nearby</Button>
              </Link>
              <Link to="/providers" className="hidden md:block">
                <Button variant="ghost">Find Providers</Button>
              </Link>

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="icon" className="relative">
                    <User className="h-5 w-5" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56 bg-popover">
                  <div className="px-2 py-1.5">
                    <p className="text-sm font-medium">{user?.userName}</p>
                    <p className="text-xs text-muted-foreground">
                      {user?.userEmail}
                    </p>
                    <p className="mt-1 text-xs">
                      <span className="rounded-md bg-primary-light px-2 py-0.5 text-primary">
                        {user?.userType}
                      </span>
                    </p>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => navigate("/profile")}>
                    Profile
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={handleLogout}
                    className="text-destructive"
                  >
                    <LogOut className="mr-2 h-4 w-4" />
                    Logout
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>

              <DropdownMenu>
                <DropdownMenuTrigger asChild className="md:hidden">
                  <Button variant="ghost" size="icon">
                    <Menu className="h-5 w-5" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56 bg-popover">
                  <DropdownMenuItem onClick={() => navigate("/dashboard")}>
                    Dashboard
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => navigate("/services")}>
                    Browse Services
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => navigate("/nearby")}>
                    Nearby
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => navigate("/providers")}>
                    Find Providers
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <>
              <Link to="/login">
                <Button variant="ghost">Login</Button>
              </Link>
              <Link to="/register">
                <Button>Get Started</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
