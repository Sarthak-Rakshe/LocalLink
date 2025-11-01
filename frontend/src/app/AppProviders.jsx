import { PayPalScriptProvider } from "@paypal/react-paypal-js";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "react-hot-toast";
import { AuthProvider } from "../context/AuthContext.jsx";

const queryClient = new QueryClient();

export function AppProviders({ children }) {
  const paypalClientId = import.meta.env.VITE_PAYPAL_CLIENT_ID || "";
  const paypalCurrency = import.meta.env.VITE_PAYPAL_CURRENCY || "USD";

  return (
    <PayPalScriptProvider
      options={{
        clientId: paypalClientId,
        currency: paypalCurrency,
        intent: "capture",
        components: "buttons",
      }}
    >
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          {children}
          <Toaster position="top-right" />
        </AuthProvider>
      </QueryClientProvider>
    </PayPalScriptProvider>
  );
}
