import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { ApiListResponse, KeyItem } from "@/lib/api/types";

export function useKeys() {
  return useQuery<ApiListResponse<KeyItem>>({
    queryKey: ["keys"],
    queryFn: async () => {
      const response = await apiClient.get<KeyItem[]>(apiEndpoints.admin.keys);
      return { items: response.data };
    },
  });
}
