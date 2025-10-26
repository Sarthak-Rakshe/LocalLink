import React from 'react';
import Navbar from '../components/layout/Navbar';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { CreditCard } from 'lucide-react';

const PaymentsPage = () => {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="mb-2 text-3xl font-bold">Payment History</h1>
          <p className="text-muted-foreground">View your transaction history</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Transactions</CardTitle>
            <CardDescription>Your payment history</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <CreditCard className="mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="mb-2 text-lg font-semibold">No transactions yet</h3>
              <p className="text-muted-foreground">
                Your payment history will appear here
              </p>
            </div>
          </CardContent>
        </Card>
      </main>
    </div>
  );
};

export default PaymentsPage;
