import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { ApiListResponse, Permission } from "@/lib/api/types";

export function usePermissions() {
  return useQuery<ApiListResponse<Permission>>({
    queryKey: ["permissions"],
    queryFn: async () => {
      const response = await apiClient.get<Permission[]>(apiEndpoints.admin.permissions);
      return { items: response.data };
    },
  });
}
