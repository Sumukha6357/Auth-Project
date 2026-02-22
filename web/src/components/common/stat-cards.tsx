import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export type StatCardItem = {
  label: string;
  value: string | number;
  hint?: string;
};

export function StatCards({ stats }: { stats: StatCardItem[] }) {
  return (
    <div className="card-grid">
      {stats.map((stat) => (
        <Card key={stat.label}>
          <CardHeader>
            <CardTitle className="text-sm text-muted-foreground">{stat.label}</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{stat.value}</p>
            {stat.hint ? <p className="mt-1 text-xs text-muted-foreground">{stat.hint}</p> : null}
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
