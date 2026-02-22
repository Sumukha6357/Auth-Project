import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { ApiListResponse, Role } from "@/lib/api/types";

export function useRoles() {
  return useQuery<ApiListResponse<Role>>({
    queryKey: ["roles"],
    queryFn: async () => {
      const response = await apiClient.get<Role[]>(apiEndpoints.admin.roles);
      return { items: response.data };
    },
  });
}
