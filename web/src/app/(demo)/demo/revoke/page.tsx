"use client";

import { useMutation } from "@tanstack/react-query";
import { PageHeader } from "@/components/common/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/providers/auth-provider";

export default function DemoRevokePage() {
  const { logout } = useAuth();
  const revokeMutation = useMutation({ mutationFn: logout });

  return (
    <div className="space-y-6">
      <PageHeader title="Revoke" description="Revoke refresh token and clear local session." />
      <Card>
        <CardHeader><CardTitle>Revoke Current Refresh Token</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          <p className="text-sm text-muted-foreground">Calls /oauth2/revoke if refresh token exists, then logs out locally.</p>
          <Button variant="destructive" onClick={() => revokeMutation.mutate()} disabled={revokeMutation.isPending}>Revoke + Logout</Button>
        </CardContent>
      </Card>
    </div>
  );
}
