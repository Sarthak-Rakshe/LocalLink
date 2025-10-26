import React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../../components/ui/card";
import { Users, Package, Calendar, IndianRupee } from "lucide-react";
import { formatINR } from "../../lib/currency";

const AdminDashboard = () => {
  const stats = [
    {
      title: "Total Users",
      value: "0",
      description: "Registered users",
      icon: Users,
      color: "bg-primary",
    },
    {
      title: "Total Services",
      value: "0",
      description: "Listed services",
      icon: Package,
      color: "bg-accent",
    },
    {
      title: "Total Bookings",
      value: "0",
      description: "All time bookings",
      icon: Calendar,
      color: "bg-success",
    },
    {
      title: "Total Revenue",
      value: formatINR(0),
      description: "Platform revenue",
      icon: IndianRupee,
      color: "bg-muted-foreground",
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Admin Dashboard</h1>
        <p className="text-muted-foreground">
          Platform overview and management
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <Card key={stat.title}>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.title}
              </CardTitle>
              <div
                className={`flex h-8 w-8 items-center justify-center rounded-lg ${stat.color}`}
              >
                <stat.icon className="h-4 w-4 text-white" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-xs text-muted-foreground">
                {stat.description}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recent Users</CardTitle>
            <CardDescription>Latest registered users</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <Users className="mb-4 h-12 w-12 text-muted-foreground" />
              <p className="text-muted-foreground">No users yet</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Recent Services</CardTitle>
            <CardDescription>Latest service listings</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <Package className="mb-4 h-12 w-12 text-muted-foreground" />
              <p className="text-muted-foreground">No services yet</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AdminDashboard;
