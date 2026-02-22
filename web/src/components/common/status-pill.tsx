import { Badge } from "@/components/ui/badge";

export function StatusPill({ value }: { value: string | boolean | null | undefined }) {
  const normalized = String(value ?? "unknown").toLowerCase();
  if (normalized === "active" || normalized === "true" || normalized === "success") {
    return <Badge variant="success">{String(value)}</Badge>;
  }
  if (normalized.includes("lock") || normalized === "false" || normalized === "disabled") {
    return <Badge variant="danger">{String(value)}</Badge>;
  }
  if (normalized.includes("warn") || normalized.includes("pending") || normalized.includes("retired")) {
    return <Badge variant="warning">{String(value)}</Badge>;
  }
  return <Badge variant="outline">{String(value)}</Badge>;
}
