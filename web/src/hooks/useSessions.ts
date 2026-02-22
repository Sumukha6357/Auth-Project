import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { ApiListResponse, SessionItem } from "@/lib/api/types";

type SessionPage = {
  items: SessionItem[];
  total: number;
  page: number;
  size: number;
};

export function useSessions() {
  return useQuery<ApiListResponse<SessionItem>>({
    queryKey: ["sessions"],
    queryFn: async () => {
      const response = await apiClient.get<SessionPage>(apiEndpoints.admin.sessions);
      return response.data;
    },
  });
}
