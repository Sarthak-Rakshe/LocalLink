export const formatINR = (amount) => {
  const n = Number(amount);
  if (Number.isNaN(n)) return "₹0.00";
  try {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(n);
  } catch {
    return `₹${n.toFixed(2)}`;
  }
};
