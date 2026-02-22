"use client";

import { DataTable } from "@/components/common/data-table";
import { PageHeader } from "@/components/common/page-header";
import { useRoles } from "@/hooks/useRoles";

const columns = [{ accessorKey: "name", header: "Role" }, { accessorKey: "description", header: "Description" }];

export default function RolesPage() {
  const roles = useRoles();
  return (
    <div className="space-y-6">
      <PageHeader title="Roles" description="Role definitions and assignments." />
      {roles.isLoading ? <div className="rounded-md border p-4 text-sm text-muted-foreground">Loading roles...</div> : null}
      {roles.isError ? <div className="rounded-md border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">Failed to load roles.</div> : null}
      <DataTable columns={columns as never} data={roles.data?.items ?? []} />
    </div>
  );
}
