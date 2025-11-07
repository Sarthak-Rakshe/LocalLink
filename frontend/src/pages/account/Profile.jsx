import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Card from "../../components/ui/Card.jsx";
import Button from "../../components/ui/Button.jsx";
import { Input, Label, HelpText } from "../../components/ui/Input.jsx";
import { Users } from "../../services/api.js";
import { useAuth } from "../../context/AuthContext.jsx";
import toast from "react-hot-toast";
import { PencilSquareIcon } from "@heroicons/react/24/outline";

export default function ProfilePage() {
  const qc = useQueryClient();
  const { user, setUser } = useAuth();

  const profileQ = useQuery({
    queryKey: ["me-profile"],
    queryFn: Users.getCurrentProfile,
    staleTime: 60_000,
  });

  const me = profileQ.data || user;

  const [form, setForm] = useState({
    username: "",
    userEmail: "",
    userContact: "",
    userAddress: "",
  });
  const [editing, setEditing] = useState(false);

  useEffect(() => {
    if (me) {
      setForm({
        username: me.userName ?? "",
        userEmail: me.userEmail ?? "",
        userContact: me.userContact ?? "",
        userAddress: me.userAddress ?? "",
      });
    }
  }, [me]);

  const updateMutation = useMutation({
    mutationFn: async () => {
      if (!me?.userId) throw new Error("Missing user id");
      return Users.update(me.userId, form);
    },
    onSuccess: (updated) => {
      toast.success("Profile updated");
      qc.invalidateQueries({ queryKey: ["me-profile"] });
      // keep AuthContext in sync
      if (updated) setUser(updated);
      setEditing(false);
    },
    onError: (e) => {
      const msg = e?.response?.data?.message || "Failed to update profile";
      toast.error(msg);
    },
  });

  const canSave = useMemo(() => !!me?.userId, [me]);

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">My Profile</h1>
      <Card
        title="Account"
        description={`User ID #${me?.userId ?? "-"}`}
        collapsible
        action={
          !editing && (
            <button
              type="button"
              title="Edit"
              aria-label="Edit profile"
              className="inline-flex items-center gap-1 rounded-md border border-zinc-200 px-2 py-1 text-xs text-zinc-700 hover:bg-zinc-50"
              onClick={() => setEditing(true)}
            >
              <PencilSquareIcon className="size-4" /> Edit
            </button>
          )
        }
      >
        {editing ? (
          <>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <Label>Username</Label>
                <Input
                  value={form.username}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, username: e.target.value }))
                  }
                  placeholder="yourusername"
                />
                <HelpText>Unique handle used to sign in.</HelpText>
              </div>
              <div>
                <Label>Email</Label>
                <Input
                  type="email"
                  value={form.userEmail}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, userEmail: e.target.value }))
                  }
                  placeholder="you@example.com"
                />
              </div>
              <div>
                <Label>Contact</Label>
                <Input
                  value={form.userContact}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, userContact: e.target.value }))
                  }
                  placeholder="phone or other contact"
                />
              </div>
              <div className="md:col-span-2">
                <Label>Address</Label>
                <Input
                  value={form.userAddress}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, userAddress: e.target.value }))
                  }
                  placeholder="street, city, state"
                />
              </div>
            </div>
            <div className="mt-4 flex items-center justify-end gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  // reset and exit edit mode
                  setForm({
                    username: me?.userName ?? "",
                    userEmail: me?.userEmail ?? "",
                    userContact: me?.userContact ?? "",
                    userAddress: me?.userAddress ?? "",
                  });
                  setEditing(false);
                }}
              >
                Cancel
              </Button>
              <Button
                onClick={() => updateMutation.mutate()}
                disabled={!canSave || updateMutation.isPending}
              >
                {updateMutation.isPending ? "Saving…" : "Save changes"}
              </Button>
            </div>
          </>
        ) : (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <Label>Username</Label>
              <div className="rounded-md border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm dark:border-zinc-800 dark:bg-white/5 dark:text-zinc-200">
                {me?.userName || "-"}
              </div>
              <HelpText>Unique handle used to sign in.</HelpText>
            </div>
            <div>
              <Label>Email</Label>
              <div className="rounded-md border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm dark:border-zinc-800 dark:bg-white/5 dark:text-zinc-200">
                {me?.userEmail || "-"}
              </div>
            </div>
            <div>
              <Label>Contact</Label>
              <div className="rounded-md border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm dark:border-zinc-800 dark:bg-white/5 dark:text-zinc-200">
                {me?.userContact || "-"}
              </div>
            </div>
            <div className="md:col-span-2">
              <Label>Address</Label>
              <div className="rounded-md border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm dark:border-zinc-800 dark:bg-white/5 dark:text-zinc-200">
                {me?.userAddress || "-"}
              </div>
            </div>
          </div>
        )}
      </Card>
      <Card title="About" collapsible defaultOpen={false}>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <Label>User type</Label>
            <div className="rounded-md border border-zinc-200 px-3 py-2 text-sm bg-zinc-50 dark:border-zinc-800 dark:bg-white/5 dark:text-zinc-200">
              {me?.userType || "-"}
            </div>
          </div>
          <div>
            <Label>Status</Label>
            <div className="rounded-md border border-zinc-200 px-3 py-2 text-sm bg-zinc-50 dark:border-zinc-800 dark:bg-white/5 dark:text-zinc-200">
              {me?.isActive ? "Active" : "Inactive"}
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
}
