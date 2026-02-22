"use client";

import Link from "next/link";
import { ShieldCheck, Workflow } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

export default function HomePage() {
  return (
    <main className="mx-auto min-h-screen max-w-6xl px-6 py-14">
      <header className="mb-8">
        <h1 className="text-4xl font-extrabold tracking-tight">Identity Control Center</h1>
        <p className="mt-3 text-muted-foreground">Enterprise-grade frontend for IDP operations and OAuth2/OIDC integration verification.</p>
      </header>
      <section className="grid gap-6 md:grid-cols-2">
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-2xl"><ShieldCheck className="h-6 w-6 text-primary" /> Admin Console</CardTitle>
            <CardDescription>Manage clients, users, permissions, keys, sessions, and audit activity.</CardDescription>
          </CardHeader>
          <CardContent>
            <Link href="/console/dashboard"><Button className="w-full">Open Console</Button></Link>
          </CardContent>
        </Card>
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-2xl"><Workflow className="h-6 w-6 text-accent" /> OIDC Demo Client</CardTitle>
            <CardDescription>Run auth code + PKCE, refresh, revoke, JWKS, and userinfo flows against IDP.</CardDescription>
          </CardHeader>
          <CardContent>
            <Link href="/demo"><Button variant="outline" className="w-full">Open Demo</Button></Link>
          </CardContent>
        </Card>
      </section>
    </main>
  );
}
