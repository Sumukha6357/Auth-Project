"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/providers/auth-provider";

export default function LoginPage() {
  const { login } = useAuth();
  const router = useRouter();
  const [busy, setBusy] = useState(false);

  return (
    <main className="mx-auto min-h-screen max-w-lg px-6 py-24">
      <Card>
        <CardHeader>
          <CardTitle>OIDC Login</CardTitle>
          <CardDescription>Authorization Code + PKCE flow to IDP.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <p className="text-sm text-muted-foreground">This demo stores tokens in sessionStorage for convenience. Do not use this model in production SPAs.</p>
          <Button disabled={busy} onClick={async () => { setBusy(true); await login(); }}>{busy ? "Redirecting..." : "Login with IDP"}</Button>
          <Button variant="ghost" onClick={() => router.push("/")}>Back</Button>
        </CardContent>
      </Card>
    </main>
  );
}
