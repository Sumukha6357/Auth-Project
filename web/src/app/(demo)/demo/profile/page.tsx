"use client";

import { useQuery } from "@tanstack/react-query";
import { JsonViewer } from "@/components/common/json-viewer";
import { PageHeader } from "@/components/common/page-header";
import { useAuth } from "@/providers/auth-provider";

export default function DemoProfilePage() {
  const auth = useAuth();
  const userInfoQuery = useQuery({ queryKey: ["userinfo"], queryFn: () => auth.fetchUserInfo() });

  return (
    <div className="space-y-6">
      <PageHeader title="UserInfo" description="Claims returned by /userinfo endpoint." />
      <JsonViewer title="UserInfo Response" value={userInfoQuery.data ?? { loading: userInfoQuery.isLoading }} />
    </div>
  );
}
