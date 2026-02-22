"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, KeyRound, Logs, ShieldUser, Users, UserCog, Fingerprint, Boxes, Binary, RefreshCw } from "lucide-react";
import { cn } from "@/lib/utils/cn";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/providers/auth-provider";

const navItems = [
  { href: "/console/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/console/clients", label: "Clients", icon: Boxes },
  { href: "/console/users", label: "Users", icon: Users },
  { href: "/console/roles", label: "Roles", icon: UserCog },
  { href: "/console/permissions", label: "Permissions", icon: ShieldUser },
  { href: "/console/keys", label: "Keys", icon: KeyRound },
  { href: "/console/sessions", label: "Sessions", icon: Fingerprint },
  { href: "/console/audit-logs", label: "Audit Logs", icon: Logs },
  { href: "/demo", label: "Demo", icon: Binary },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { logout, user } = useAuth();

  return (
    <div className="grid min-h-screen grid-cols-1 lg:grid-cols-[250px_1fr]">
      <aside className="border-r border-border bg-card px-4 py-6">
        <div className="mb-6 rounded-md bg-primary px-4 py-3 text-primary-foreground">
          <p className="text-xs uppercase tracking-[0.2em]">IDP</p>
          <p className="text-lg font-bold">Admin Console</p>
        </div>
        <nav className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = pathname.startsWith(item.href);
            return (
              <Link key={item.href} href={item.href} className={cn("flex items-center gap-3 rounded-md px-3 py-2 text-sm", active ? "bg-accent text-accent-foreground" : "hover:bg-muted") }>
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </aside>
      <div className="min-w-0">
        <header className="flex items-center justify-between border-b border-border bg-card px-6 py-3">
          <div>
            <p className="text-xs uppercase text-muted-foreground">Signed in as</p>
            <p className="text-sm font-semibold">{user?.email ?? "Guest"}</p>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" asChild><Link href="/login"><RefreshCw className="mr-2 h-4 w-4" />Re-Auth</Link></Button>
            <Button variant="ghost" size="sm" onClick={logout}>Logout</Button>
          </div>
        </header>
        <main className="p-6">{children}</main>
      </div>
    </div>
  );
}
