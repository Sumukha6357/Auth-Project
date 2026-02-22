import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function NotAuthorized() {
  return (
    <div className="mx-auto mt-16 max-w-xl">
      <Card>
        <CardHeader><CardTitle>Not authorized</CardTitle></CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">Your token is missing required administrative permission.</p>
        </CardContent>
      </Card>
    </div>
  );
}
