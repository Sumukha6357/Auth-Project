"use client";

import { CodeBlock } from "@/components/common/code-block";
import { JsonViewer } from "@/components/common/json-viewer";
import { PageHeader } from "@/components/common/page-header";
import { decodeJwt } from "@/lib/auth/jwt";
import { useAuth } from "@/providers/auth-provider";

export default function DemoTokensPage() {
  const { tokens } = useAuth();

  return (
    <div className="space-y-6">
      <PageHeader title="Tokens" description="Raw token values and decoded JWT payloads." />
      <CodeBlock code={JSON.stringify(tokens, null, 2)} />
      <div className="grid gap-6 lg:grid-cols-2">
        <JsonViewer title="Access Token Claims" value={decodeJwt(tokens?.accessToken)} />
        <JsonViewer title="ID Token Claims" value={decodeJwt(tokens?.idToken)} />
      </div>
    </div>
  );
}
