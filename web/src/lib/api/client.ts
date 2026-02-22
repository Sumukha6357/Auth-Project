"use client";

import axios from "axios";
import { createCorrelationId } from "@/lib/utils/correlation";
import { getTokens } from "@/lib/auth/storage";

export const apiClient = axios.create();

apiClient.interceptors.request.use((config) => {
  const tokens = getTokens();
  config.headers = config.headers ?? {};
  config.headers.Authorization = tokens?.accessToken ? `Bearer ${tokens.accessToken}` : undefined;
  config.headers["X-Correlation-Id"] = createCorrelationId();
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (typeof window !== "undefined" && error?.response?.status === 401 && !window.location.pathname.startsWith("/login")) {
      window.location.assign("/login");
    }
    return Promise.reject(error);
  }
);
