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
import { Textarea } from "@/components/ui/textarea";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { Client } from "@/lib/api/types";
import { useClients } from "@/hooks/useClients";

const createClientSchema = z.object({
  clientId: z.string().min(3),
  name: z.string().min(2),
  type: z.enum(["PUBLIC", "CONFIDENTIAL"]),
  redirectUris: z.string(),
  scopes: z.string().min(1),
  grantTypes: z.string().min(1),
  requirePkce: z.boolean(),
});

type CreateClientForm = z.infer<typeof createClientSchema>;

const columns: ColumnDef<Client>[] = [
  { accessorKey: "clientId", header: "Client ID" },
  { accessorKey: "name", header: "Name" },
  { accessorKey: "type", header: "Type" },
  { accessorKey: "enabled", header: "Status", cell: ({ row }) => <StatusPill value={row.original.enabled ? "active" : "disabled"} /> },
  { accessorKey: "requirePkce", header: "PKCE", cell: ({ row }) => <StatusPill value={row.original.requirePkce} /> },
];

export default function ClientsPage() {
  const queryClient = useQueryClient();
  const clientsQuery = useClients();

  const form = useForm<CreateClientForm>({
    resolver: zodResolver(createClientSchema),
    defaultValues: { type: "PUBLIC", requirePkce: true, redirectUris: "", scopes: "openid profile email offline_access", grantTypes: "authorization_code,refresh_token" },
  });

  const createMutation = useMutation({
    mutationFn: async (values: CreateClientForm) => {
      await apiClient.post(apiEndpoints.admin.clients, {
        clientId: values.clientId,
        name: values.name,
        type: values.type,
        redirectUris: values.redirectUris.split("\n").map((s) => s.trim()).filter(Boolean),
        postLogoutRedirectUris: [],
        scopes: values.scopes.split(/[\s,]+/).filter(Boolean),
        grantTypes: values.grantTypes.split(/[\s,]+/).filter(Boolean),
        requirePkce: values.requirePkce,
      });
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["clients"] });
      form.reset();
    },
  });

  const rotateSecretMutation = useMutation({
    mutationFn: async (clientId: string) => {
      await apiClient.patch(apiEndpoints.admin.patchClient(clientId), { rotateSecret: true });
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["clients"] }),
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Clients"
        description="Manage OAuth client registrations."
        action={
          <Dialog>
            <DialogTrigger asChild><Button>Create Client</Button></DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create Client</DialogTitle>
                <DialogDescription>Public clients must keep PKCE enabled.</DialogDescription>
              </DialogHeader>
              <form className="space-y-3" onSubmit={form.handleSubmit((values) => createMutation.mutate(values))}>
                <div><Label>Client ID</Label><Input {...form.register("clientId")} /></div>
                <div><Label>Name</Label><Input {...form.register("name")} /></div>
                <div><Label>Type (PUBLIC|CONFIDENTIAL)</Label><Input {...form.register("type")} /></div>
                <div><Label>Redirect URIs (one per line)</Label><Textarea {...form.register("redirectUris")} /></div>
                <div><Label>Scopes</Label><Input {...form.register("scopes")} /></div>
                <div><Label>Grant Types</Label><Input {...form.register("grantTypes")} /></div>
                <div className="flex justify-end gap-2"><Button type="submit" disabled={createMutation.isPending}>Save</Button></div>
              </form>
            </DialogContent>
          </Dialog>
        }
      />
      {clientsQuery.isLoading ? <div className="rounded-md border p-4 text-sm text-muted-foreground">Loading clients...</div> : null}
      {clientsQuery.isError ? <div className="rounded-md border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">Failed to load clients.</div> : null}

      <DataTable columns={columns} data={clientsQuery.data?.items ?? []} />

      <div className="flex flex-wrap gap-2">
        {(clientsQuery.data?.items ?? []).map((client) => (
          <Button key={client.clientId} variant="outline" onClick={() => rotateSecretMutation.mutate(client.clientId)}>
            Rotate Secret: {client.clientId}
          </Button>
        ))}
      </div>
    </div>
  );
}
