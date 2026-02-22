"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { DataTable } from "@/components/common/data-table";
import { PageHeader } from "@/components/common/page-header";
import { StatusPill } from "@/components/common/status-pill";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { User } from "@/lib/api/types";
import { useUsers } from "@/hooks/useUsers";

const createUserSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
  roles: z.string().min(1),
});

type CreateUserForm = z.infer<typeof createUserSchema>;

const assignRoleSchema = z.object({
  userId: z.string().min(1),
  roles: z.string().min(1),
});

type AssignRoleForm = z.infer<typeof assignRoleSchema>;

const columns: ColumnDef<User>[] = [
  { accessorKey: "email", header: "Email" },
  { accessorKey: "status", header: "Status", cell: ({ row }) => <StatusPill value={row.original.status} /> },
  { accessorKey: "emailVerified", header: "Email Verified", cell: ({ row }) => <StatusPill value={row.original.emailVerified} /> },
  { accessorKey: "roles", header: "Roles", cell: ({ row }) => row.original.roles.join(", ") },
];

export default function UsersPage() {
  const queryClient = useQueryClient();
  const usersQuery = useUsers();

  const createForm = useForm<CreateUserForm>({ resolver: zodResolver(createUserSchema), defaultValues: { roles: "USER" } });
  const assignForm = useForm<AssignRoleForm>({ resolver: zodResolver(assignRoleSchema) });

  const createMutation = useMutation({
    mutationFn: async (values: CreateUserForm) => {
      await apiClient.post(apiEndpoints.admin.users, {
        email: values.email,
        password: values.password,
        emailVerified: false,
        roles: values.roles.split(/[\s,]+/).filter(Boolean),
      });
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }),
  });

  const assignMutation = useMutation({
    mutationFn: async (values: AssignRoleForm) => {
      await apiClient.post(apiEndpoints.admin.userRoles(values.userId), {
        roles: values.roles.split(/[\s,]+/).filter(Boolean),
      });
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }),
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Users"
        description="Create and manage users."
        action={
          <div className="flex gap-2">
            <Dialog>
              <DialogTrigger asChild><Button>Create User</Button></DialogTrigger>
              <DialogContent>
                <DialogHeader><DialogTitle>Create User</DialogTitle></DialogHeader>
                <form className="space-y-3" onSubmit={createForm.handleSubmit((values) => createMutation.mutate(values))}>
                  <div><Label>Email</Label><Input {...createForm.register("email")} /></div>
                  <div><Label>Password</Label><Input type="password" {...createForm.register("password")} /></div>
                  <div><Label>Roles</Label><Input {...createForm.register("roles")} /></div>
                  <Button type="submit" disabled={createMutation.isPending}>Save</Button>
                </form>
              </DialogContent>
            </Dialog>
            <Dialog>
              <DialogTrigger asChild><Button variant="outline">Assign Roles</Button></DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Assign Roles</DialogTitle>
                  <DialogDescription>Role names comma-separated.</DialogDescription>
                </DialogHeader>
                <form className="space-y-3" onSubmit={assignForm.handleSubmit((values) => assignMutation.mutate(values))}>
                  <div><Label>User ID</Label><Input {...assignForm.register("userId")} /></div>
                  <div><Label>Roles</Label><Input {...assignForm.register("roles")} /></div>
                  <Button type="submit" disabled={assignMutation.isPending}>Apply</Button>
                </form>
              </DialogContent>
            </Dialog>
          </div>
        }
      />
      {usersQuery.isLoading ? <div className="rounded-md border p-4 text-sm text-muted-foreground">Loading users...</div> : null}
      {usersQuery.isError ? <div className="rounded-md border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">Failed to load users.</div> : null}
      <DataTable columns={columns} data={usersQuery.data?.items ?? []} />
    </div>
  );
}
