import type {
  AdminActivityCreate,
  AdminActivityItem,
  AdminActivityUpdate,
  AdminStatsResponse,
  AdminUserCreate,
  AdminUserItem,
  AdminUserUpdate,
  AuthTokenResponse,
  ClubCreate,
  ClubResponse,
  ClubUpdate,
  MessageResponse,
} from "./types";

export class ApiError extends Error {
  status: number;
  detail: string;

  constructor(status: number, detail: string) {
    super(detail);
    this.name = "ApiError";
    this.status = status;
    this.detail = detail;
  }
}

function normalizeBaseUrl(baseUrl: string): string {
  return baseUrl.replace(/\/+$/, "");
}

function buildQuery(params: Record<string, string | number | undefined>): string {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") {
      query.set(key, String(value));
    }
  }
  const encoded = query.toString();
  return encoded ? `?${encoded}` : "";
}

async function parseErrorDetail(res: Response): Promise<string> {
  try {
    const data = await res.json();
    if (typeof data?.detail === "string") {
      return data.detail;
    }
    if (Array.isArray(data?.detail) && data.detail.length > 0) {
      return data.detail.map((item: { msg?: string }) => item.msg ?? "Dữ liệu không hợp lệ").join(", ");
    }
  } catch {
    return `Lỗi máy chủ (HTTP ${res.status})`;
  }
  return `Lỗi máy chủ (HTTP ${res.status})`;
}

async function requestJson<T>(
  url: string,
  method: string,
  token: string,
  body?: unknown
): Promise<T> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  const res = await fetch(url, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const detail = await parseErrorDetail(res);
    throw new ApiError(res.status, detail);
  }
  return (await res.json()) as T;
}

export interface AdminApi {
  login: (email: string, password: string) => Promise<AuthTokenResponse>;
  logout: () => Promise<MessageResponse>;
  getStats: () => Promise<AdminStatsResponse>;
  getUsers: (q?: string, page?: number, limit?: number) => Promise<AdminUserItem[]>;
  createUser: (payload: AdminUserCreate) => Promise<AdminUserItem>;
  updateUser: (userId: number, payload: AdminUserUpdate) => Promise<AdminUserItem>;
  deleteUser: (userId: number) => Promise<MessageResponse>;
  getActivities: (userId?: number, page?: number, limit?: number) => Promise<AdminActivityItem[]>;
  createActivity: (payload: AdminActivityCreate) => Promise<AdminActivityItem>;
  updateActivity: (activityId: string, payload: AdminActivityUpdate) => Promise<AdminActivityItem>;
  deleteActivity: (activityId: string) => Promise<MessageResponse>;
  getClubs: (q?: string, page?: number, limit?: number) => Promise<ClubResponse[]>;
  createClub: (payload: ClubCreate) => Promise<ClubResponse>;
  updateClub: (clubId: number, payload: ClubUpdate) => Promise<ClubResponse>;
  deleteClub: (clubId: number) => Promise<MessageResponse>;
}

export function createAdminApi(baseUrl: string, token = ""): AdminApi {
  const root = normalizeBaseUrl(baseUrl);
  const call = <T>(path: string, method = "GET", body?: unknown) =>
    requestJson<T>(`${root}${path}`, method, token, body);

  return {
    login: (email, password) => call("/auth/login", "POST", { email, password }),
    logout: () => call("/auth/logout", "POST"),
    getStats: () => call("/admin/stats"),
    getUsers: (q = "", page = 1, limit = 50) =>
      call(`/admin/users${buildQuery({ q, page, limit })}`),
    createUser: (payload) => call("/admin/users", "POST", payload),
    updateUser: (userId, payload) => call(`/admin/users/${userId}`, "PUT", payload),
    deleteUser: (userId) => call(`/admin/users/${userId}`, "DELETE"),
    getActivities: (userId, page = 1, limit = 50) =>
      call(`/admin/activities${buildQuery({ user_id: userId, page, limit })}`),
    createActivity: (payload) => call("/admin/activities", "POST", payload),
    updateActivity: (activityId, payload) =>
      call(`/admin/activities/${activityId}`, "PUT", payload),
    deleteActivity: (activityId) => call(`/admin/activities/${activityId}`, "DELETE"),
    getClubs: (q = "", page = 1, limit = 50) =>
      call(`/admin/clubs${buildQuery({ q, page, limit })}`),
    createClub: (payload) => call("/admin/clubs", "POST", payload),
    updateClub: (clubId, payload) => call(`/admin/clubs/${clubId}`, "PUT", payload),
    deleteClub: (clubId) => call(`/admin/clubs/${clubId}`, "DELETE"),
  };
}
