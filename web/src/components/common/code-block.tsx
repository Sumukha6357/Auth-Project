"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function CodeBlock({ code }: { code: string }) {
  const [copied, setCopied] = useState(false);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm">Code</CardTitle>
      </CardHeader>
      <CardContent>
        <pre className="overflow-auto rounded-md bg-slate-950 p-3 text-xs text-slate-100"><code>{code}</code></pre>
        <Button
          className="mt-3"
          variant="outline"
          onClick={async () => {
            await navigator.clipboard.writeText(code);
            setCopied(true);
            setTimeout(() => setCopied(false), 1200);
          }}
        >
          {copied ? "Copied" : "Copy"}
        </Button>
      </CardContent>
    </Card>
  );
}
