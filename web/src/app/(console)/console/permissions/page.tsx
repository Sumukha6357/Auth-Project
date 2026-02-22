"use client";

import { DataTable } from "@/components/common/data-table";
import { PageHeader } from "@/components/common/page-header";
import { usePermissions } from "@/hooks/usePermissions";

const columns = [{ accessorKey: "name", header: "Permission" }, { accessorKey: "description", header: "Description" }];

export default function PermissionsPage() {
  const permissions = usePermissions();
  return (
    <div className="space-y-6">
      <PageHeader title="Permissions" description="Permission catalog and mappings." />
      {permissions.isLoading ? <div className="rounded-md border p-4 text-sm text-muted-foreground">Loading permissions...</div> : null}
      {permissions.isError ? <div className="rounded-md border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">Failed to load permissions.</div> : null}
      <DataTable columns={columns as never} data={permissions.data?.items ?? []} />
    </div>
  );
}
