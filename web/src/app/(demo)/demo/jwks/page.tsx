"use client";

import { useQuery } from "@tanstack/react-query";
import { JsonViewer } from "@/components/common/json-viewer";
import { PageHeader } from "@/components/common/page-header";
import { apiEndpoints } from "@/lib/api/endpoints";

export default function DemoJwksPage() {
  const jwksQuery = useQuery({
    queryKey: ["jwks"],
    queryFn: async () => {
      const response = await fetch(apiEndpoints.oauth.jwks);
      if (!response.ok) throw new Error("Unable to fetch JWKS");
      return response.json();
    },
  });

  return (
    <div className="space-y-6">
      <PageHeader title="JWKS" description="Public signing keys from IDP." />
      <JsonViewer title="JWKS" value={jwksQuery.data ?? { loading: jwksQuery.isLoading }} />
    </div>
  );
}
