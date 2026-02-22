import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { ApiListResponse, Client } from "@/lib/api/types";

export function useClients() {
  return useQuery<ApiListResponse<Client>>({
    queryKey: ["clients"],
    queryFn: async () => {
      const response = await apiClient.get<Client[]>(apiEndpoints.admin.clients);
      return { items: response.data };
    },
  });
}
