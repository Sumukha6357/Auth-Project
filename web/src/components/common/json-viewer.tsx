import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function JsonViewer({ value, title = "JSON" }: { value: unknown; title?: string }) {
  return (
    <Card>
      <CardHeader><CardTitle className="text-sm">{title}</CardTitle></CardHeader>
      <CardContent>
        <pre className="max-h-[420px] overflow-auto rounded-md bg-slate-950 p-4 text-xs text-slate-100">
          {JSON.stringify(value, null, 2)}
        </pre>
      </CardContent>
    </Card>
  );
}
