"use client";

import { PageHeader } from "@/components/common/page-header";
import { StatCards } from "@/components/common/stat-cards";
import { useClients } from "@/hooks/useClients";
import { useUsers } from "@/hooks/useUsers";
import { useSessions } from "@/hooks/useSessions";
import { useAuditLogs } from "@/hooks/useAuditLogs";

export default function ConsoleDashboardPage() {
  const clients = useClients();
  const users = useUsers();
  const sessions = useSessions();
  const auditLogs = useAuditLogs();

  const stats = [
    { label: "Clients", value: clients.data?.items.length ?? 0 },
    { label: "Users", value: users.data?.items.length ?? 0 },
    { label: "Active Sessions", value: sessions.data?.items.length ?? 0 },
    { label: "Audit Events", value: auditLogs.data?.items.length ?? 0 },
  ];

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" description="Identity posture at a glance." />
      <StatCards stats={stats} />
    </div>
  );
}
