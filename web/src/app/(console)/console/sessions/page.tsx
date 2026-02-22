"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { DataTable } from "@/components/common/data-table";
import { PageHeader } from "@/components/common/page-header";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useSessions } from "@/hooks/useSessions";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";

const revokeSchema = z.object({ sessionId: z.string().uuid() });
type RevokeForm = z.infer<typeof revokeSchema>;
const columns = [{ accessorKey: "clientId", header: "Client" }, { accessorKey: "deviceId", header: "Device" }, { accessorKey: "expiresAt", header: "Expires" }, { accessorKey: "ip", header: "IP" }];

export default function SessionsPage() {
  const queryClient = useQueryClient();
  const sessions = useSessions();
  const form = useForm<RevokeForm>({ resolver: zodResolver(revokeSchema) });

  const revokeMutation = useMutation({
    mutationFn: async (values: RevokeForm) => apiClient.post(apiEndpoints.admin.revokeSession, values),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["sessions"] }),
  });

  const revokeAllMutation = useMutation({
    mutationFn: async () => apiClient.post(apiEndpoints.sessions.revokeAll),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["sessions"] }),
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Sessions"
        description="Track and revoke active refresh token sessions."
        action={
          <div className="flex gap-2">
            <Dialog>
              <DialogTrigger asChild><Button variant="outline">Revoke Device</Button></DialogTrigger>
              <DialogContent>
                <DialogHeader><DialogTitle>Revoke Session</DialogTitle></DialogHeader>
                <form className="space-y-3" onSubmit={form.handleSubmit((values) => revokeMutation.mutate(values))}>
                  <div><Label>Session ID</Label><Input {...form.register("sessionId")} /></div>
                  <Button type="submit">Revoke</Button>
                </form>
              </DialogContent>
            </Dialog>
            <Button variant="destructive" onClick={() => revokeAllMutation.mutate()}>Revoke All</Button>
          </div>
        }
      />
      {sessions.isLoading ? <div className="rounded-md border p-4 text-sm text-muted-foreground">Loading sessions...</div> : null}
      {sessions.isError ? <div className="rounded-md border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">Failed to load sessions.</div> : null}
      <DataTable columns={columns as never} data={sessions.data?.items ?? []} />
    </div>
  );
}
