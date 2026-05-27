import { useAuthStore } from "../stores/useAuthStore";

export function useApi() {
  const token = useAuthStore((state) => state.token);

  const apiFetch = async (path: string, options: RequestInit = {}) => {
    const isBodyRequest = options.method === "POST" || options.method === "PUT" || options.method === "PATCH";

    const res = await fetch(`${import.meta.env.VITE_API_URL}${path}`, {
      ...options,
      headers: {
        ...(isBodyRequest ? { "Content-Type": "application/json" } : {}),
        Authorization: `Bearer ${token}`,
        ...options.headers,
      },
    });
    return res;
  };

  return { apiFetch };
}