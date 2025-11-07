import { createContext, useContext, useEffect, useMemo, useState } from "react";

// Simplified theme context: only 'light' and 'dark', toggled by user
// resolvedTheme mirrors mode for backward compatibility
const ThemeContext = createContext({
  mode: "light",
  resolvedTheme: "light",
  setMode: () => {},
  toggle: () => {},
});

const STORAGE_KEY = "theme"; // stores 'light' | 'dark'

export function ThemeProvider({ children }) {
  // Initialize from localStorage; fall back to light, or OS preference once
  const [mode, setMode] = useState(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored === "light" || stored === "dark") return stored;
    } catch {}
    // Use a one-time OS preference as initial value (no 'system' mode)
    if (
      typeof window !== "undefined" &&
      window.matchMedia?.("(prefers-color-scheme: dark)").matches
    ) {
      return "dark";
    }
    return "light";
  });

  const resolvedTheme = mode;

  // Apply class to <html> and persist choice
  useEffect(() => {
    const root = document.documentElement;
    if (mode === "dark") root.classList.add("dark");
    else root.classList.remove("dark");
    try {
      localStorage.setItem(STORAGE_KEY, mode);
    } catch {}
  }, [mode]);

  const value = useMemo(
    () => ({
      mode,
      resolvedTheme,
      setMode,
      toggle: () => setMode((m) => (m === "light" ? "dark" : "light")),
    }),
    [mode, resolvedTheme]
  );

  return (
    <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
