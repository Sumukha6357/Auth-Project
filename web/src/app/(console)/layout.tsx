"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/layout/app-shell";
import { NotAuthorized } from "@/components/common/not-authorized";
import { useAuth } from "@/providers/auth-provider";

export default function ConsoleLayout({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, hasAdminPermission } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isAuthenticated) router.replace("/login");
  }, [isAuthenticated, router]);

  if (!isAuthenticated) {
    return null;
  }

  if (!hasAdminPermission) {
    return <AppShell><NotAuthorized /></AppShell>;
  }

  return <AppShell>{children}</AppShell>;
}
