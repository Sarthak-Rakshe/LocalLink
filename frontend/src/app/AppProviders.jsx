import { PayPalScriptProvider } from "@paypal/react-paypal-js";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "react-hot-toast";
import { AuthProvider } from "../context/AuthContext.jsx";

const queryClient = new QueryClient();

export function AppProviders({ children }) {
  const paypalClientId = import.meta.env.VITE_PAYPAL_CLIENT_ID || "";

  return (
    <PayPalScriptProvider options={{ clientId: paypalClientId }}>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          {children}
          <Toaster position="top-right" />
        </AuthProvider>
      </QueryClientProvider>
    </PayPalScriptProvider>
  );
}
