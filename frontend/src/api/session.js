export function getStoredUser() {
  try {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function getUserType() {
  // Prefer dedicated key; fallback to user object
  const t = localStorage.getItem("userType");
  if (t) return t;
  const user = getStoredUser();
  return user?.userType || null;
}

export const isProvider = () =>
  (getUserType() || "").toUpperCase() === "PROVIDER";
export const isCustomer = () =>
  (getUserType() || "").toUpperCase() === "CUSTOMER";
