import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { ApiListResponse, User } from "@/lib/api/types";

export function useUsers() {
  return useQuery<ApiListResponse<User>>({
    queryKey: ["users"],
    queryFn: async () => {
      const response = await apiClient.get<User[]>(apiEndpoints.admin.users);
      return { items: response.data };
    },
  });
}
