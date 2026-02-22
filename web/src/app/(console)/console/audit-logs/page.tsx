"use client";

import { DataTable } from "@/components/common/data-table";
import { PageHeader } from "@/components/common/page-header";
import { StatusPill } from "@/components/common/status-pill";
import { useAuditLogs } from "@/hooks/useAuditLogs";

const columns = [
  { accessorKey: "eventType", header: "Event" },
  { accessorKey: "entityType", header: "Entity" },
  { accessorKey: "createdAt", header: "Created" },
  { accessorKey: "success", header: "Success", cell: ({ row }: { row: { original: { success: boolean } } }) => <StatusPill value={row.original.success} /> },
];

export default function AuditLogsPage() {
  const auditLogs = useAuditLogs();

  return (
    <div className="space-y-6">
      <PageHeader title="Audit Logs" description="Security and administrative event timeline." />
      {auditLogs.isLoading ? <div className="rounded-md border p-4 text-sm text-muted-foreground">Loading audit logs...</div> : null}
      {auditLogs.isError ? <div className="rounded-md border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">Failed to load audit logs.</div> : null}
      <DataTable columns={columns as never} data={auditLogs.data?.items ?? []} />
    </div>
  );
}
