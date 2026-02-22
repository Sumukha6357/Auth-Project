"use client";

import Link from "next/link";
import { useMutation } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/common/page-header";
import { useAuth } from "@/providers/auth-provider";

export default function DemoHomePage() {
  const { logoutEverywhere } = useAuth();
  const logoutEverywhereMutation = useMutation({ mutationFn: logoutEverywhere });

  const cards = [
    { href: "/demo/profile", title: "UserInfo", desc: "Fetch and inspect /userinfo payload." },
    { href: "/demo/tokens", title: "Tokens", desc: "Inspect local tokens and decoded claims." },
    { href: "/demo/jwks", title: "JWKS", desc: "Display signing public keys." },
    { href: "/demo/refresh", title: "Refresh", desc: "Manually rotate refresh token." },
    { href: "/demo/revoke", title: "Revoke", desc: "Revoke current refresh token." },
  ];

  return (
    <div className="space-y-6">
      <PageHeader title="OIDC Demo Client" description="Validate authorization code + PKCE integration end to end." action={<Button variant="destructive" onClick={() => logoutEverywhereMutation.mutate()}>Logout Everywhere</Button>} />
      <div className="card-grid">
        {cards.map((item) => (
          <Card key={item.href}>
            <CardHeader>
              <CardTitle>{item.title}</CardTitle>
              <CardDescription>{item.desc}</CardDescription>
            </CardHeader>
            <CardContent>
              <Button asChild variant="outline"><Link href={item.href}>Open</Link></Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
