import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { ApiListResponse, AuditLog } from "@/lib/api/types";

type AuditPage = {
  items: AuditLog[];
  total: number;
  page: number;
  size: number;
};

export function useAuditLogs() {
  return useQuery<ApiListResponse<AuditLog>>({
    queryKey: ["auditLogs"],
    queryFn: async () => {
      const response = await apiClient.get<AuditPage>(apiEndpoints.admin.auditLogs);
      return response.data;
    },
  });
}
