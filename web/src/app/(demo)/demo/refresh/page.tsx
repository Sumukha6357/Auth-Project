"use client";

import { useMutation } from "@tanstack/react-query";
import { PageHeader } from "@/components/common/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/providers/auth-provider";

export default function DemoRefreshPage() {
  const { refresh } = useAuth();
  const refreshMutation = useMutation({ mutationFn: refresh });

  return (
    <div className="space-y-6">
      <PageHeader title="Refresh Tokens" description="Manual refresh token flow and rotation." />
      <Card>
        <CardHeader><CardTitle>Refresh Access Token</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          <p className="text-sm text-muted-foreground">This calls /oauth2/token with grant_type=refresh_token.</p>
          <Button onClick={() => refreshMutation.mutate()} disabled={refreshMutation.isPending}>Run Refresh</Button>
          {refreshMutation.isError ? <p className="text-sm text-danger">Refresh failed</p> : null}
        </CardContent>
      </Card>
    </div>
  );
}
