"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/providers/auth-provider";

function OidcCallbackInner() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { completeLogin } = useAuth();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const code = searchParams.get("code");
    const state = searchParams.get("state");
    if (!code) {
      setError("Missing authorization code.");
      return;
    }

    completeLogin(code, state)
      .then(() => router.replace("/console/dashboard"))
      .catch((e: unknown) => {
        setError(e instanceof Error ? e.message : "OIDC callback failed");
      });
  }, [completeLogin, router, searchParams]);

  return (
    <Card>
      <CardHeader><CardTitle>Completing Login</CardTitle></CardHeader>
      <CardContent>
        {error ? <p className="text-sm text-danger">{error}</p> : <p className="text-sm text-muted-foreground">Exchanging authorization code for tokens...</p>}
      </CardContent>
    </Card>
  );
}

export default function OidcCallbackPage() {
  return (
    <main className="mx-auto min-h-screen max-w-lg px-6 py-24">
      <Suspense fallback={<Card><CardHeader><CardTitle>Completing Login</CardTitle></CardHeader><CardContent><p className="text-sm text-muted-foreground">Loading callback parameters...</p></CardContent></Card>}>
        <OidcCallbackInner />
      </Suspense>
    </main>
  );
}
