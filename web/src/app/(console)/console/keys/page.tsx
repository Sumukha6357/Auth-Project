"use client";

import { useMutation } from "@tanstack/react-query";
import { DataTable } from "@/components/common/data-table";
import { PageHeader } from "@/components/common/page-header";
import { Button } from "@/components/ui/button";
import { useKeys } from "@/hooks/useKeys";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";

const columns = [{ accessorKey: "kid", header: "KID" }, { accessorKey: "status", header: "Status" }, { accessorKey: "algorithm", header: "Algorithm" }];

export default function KeysPage() {
  const keys = useKeys();
  const rotateMutation = useMutation({ mutationFn: () => apiClient.post(apiEndpoints.admin.rotateKeys) });

  return (
    <div className="space-y-6">
      <PageHeader title="Signing Keys" description="Key lifecycle and rotation." action={<Button onClick={() => rotateMutation.mutate()} disabled={rotateMutation.isPending}>Rotate Keys</Button>} />
      {keys.isLoading ? <div className="rounded-md border p-4 text-sm text-muted-foreground">Loading keys...</div> : null}
      {keys.isError ? <div className="rounded-md border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">Failed to load keys.</div> : null}
      <DataTable columns={columns as never} data={keys.data?.items ?? []} />
    </div>
  );
}
