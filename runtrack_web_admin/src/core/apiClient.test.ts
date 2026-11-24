import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError, createAdminApi } from "./apiClient";

const BASE_URL = "https://api.runtrack.local/";
const TOKEN = "jwt-admin-token-123";
const originalFetch = globalThis.fetch;

type MockFetch = ReturnType<typeof vi.fn>;
let fetchMock: MockFetch;

function mockJsonResponse(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: vi.fn().mockResolvedValue(body),
  } as unknown as Response;
}

function mockInvalidJsonResponse(status: number): Response {
  return {
    ok: false,
    status,
    json: vi.fn().mockRejectedValue(new SyntaxError("Unexpected token")),
  } as unknown as Response;
}

beforeEach(() => {
  fetchMock = vi.fn();
  globalThis.fetch = fetchMock as unknown as typeof fetch;
});

afterEach(() => {
  globalThis.fetch = originalFetch;
  vi.resetAllMocks();
});

describe("apiClient auth: login và logout", () => {
  it("login trả về token khi 200 và chuẩn hóa baseUrl", async () => {
    const tokenRes = { access_token: TOKEN, token_type: "bearer" };
    fetchMock.mockResolvedValueOnce(mockJsonResponse(200, tokenRes));
    const api = createAdminApi("https://api.runtrack.local///");

    const result = await api.login("admin@runtrack.app", "Admin@123");

    expect(result).toEqual(tokenRes);
    expect(fetchMock).toHaveBeenCalledWith("https://api.runtrack.local/auth/login", {
      method: "POST",
      headers: { Accept: "application/json", "Content-Type": "application/json" },
      body: JSON.stringify({ email: "admin@runtrack.app", password: "Admin@123" }),
    });
  });

  it("login ném ApiError kèm status 401 và detail khi sai thông tin", async () => {
    fetchMock.mockResolvedValueOnce(
      mockJsonResponse(401, { detail: "Email hoặc mật khẩu không đúng" })
    );
    const api = createAdminApi(BASE_URL);

    await expect(api.login("bad@runtrack.app", "wrong")).rejects.toMatchObject({
      name: "ApiError",
      status: 401,
      detail: "Email hoặc mật khẩu không đúng",
    });
  });

  it("logout gửi đúng Header Authorization Bearer <token>", async () => {
    fetchMock.mockResolvedValueOnce(mockJsonResponse(200, { message: "Đã đăng xuất" }));
    const api = createAdminApi(BASE_URL, TOKEN);

    const res = await api.logout();

    expect(res).toEqual({ message: "Đã đăng xuất" });
    expect(fetchMock).toHaveBeenCalledWith("https://api.runtrack.local/auth/logout", {
      method: "POST",
      headers: { Accept: "application/json", Authorization: `Bearer ${TOKEN}` },
      body: undefined,
    });
  });
});

describe("apiClient errors và getStats", () => {
  it("getStats ném ApiError status 403 khi tài khoản không có quyền admin", async () => {
    fetchMock.mockResolvedValueOnce(
      mockJsonResponse(403, { detail: "Không có quyền quản trị" })
    );
    const api = createAdminApi(BASE_URL, TOKEN);

    try {
      await api.getStats();
      expect.unreachable("Phải ném ApiError 403");
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      expect((err as ApiError).status).toBe(403);
      expect((err as ApiError).detail).toBe("Không có quyền quản trị");
    }
  });

  it("parse lỗi 422 dạng mảng detail và fallback 500 khi response không phải JSON", async () => {
    fetchMock.mockResolvedValueOnce(
      mockJsonResponse(422, { detail: [{ msg: "email không hợp lệ" }, {}] })
    );
    const api = createAdminApi(BASE_URL, TOKEN);

    await expect(api.getStats()).rejects.toMatchObject({
      status: 422,
      detail: "email không hợp lệ, Dữ liệu không hợp lệ",
    });

    fetchMock.mockResolvedValueOnce(mockInvalidJsonResponse(500));
    await expect(api.getStats()).rejects.toMatchObject({
      status: 500,
      detail: "Lỗi máy chủ (HTTP 500)",
    });
  });
});

describe("apiClient CRUD users", () => {
  it("getUsers xây dựng đúng query string q, page, limit", async () => {
    fetchMock.mockResolvedValueOnce(mockJsonResponse(200, []));
    fetchMock.mockResolvedValueOnce(mockJsonResponse(200, []));
    const api = createAdminApi(BASE_URL, TOKEN);

    await api.getUsers();
    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      "https://api.runtrack.local/admin/users?page=1&limit=50",
      expect.objectContaining({ method: "GET" })
    );

    await api.getUsers("alice", 2, 20);
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "https://api.runtrack.local/admin/users?q=alice&page=2&limit=20",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("createUser, updateUser và deleteUser gọi đúng method, URL và body JSON", async () => {
    fetchMock.mockResolvedValue(mockJsonResponse(200, { id: 7 }));
    const api = createAdminApi(BASE_URL, TOKEN);
    const createPayload = { email: "u@test.com", password: "123", full_name: "U" };
    const updatePayload = { full_name: "U2", weekly_goal_km: 30 };

    await api.createUser(createPayload);
    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      "https://api.runtrack.local/admin/users",
      expect.objectContaining({ method: "POST", body: JSON.stringify(createPayload) })
    );

    await api.updateUser(7, updatePayload);
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "https://api.runtrack.local/admin/users/7",
      expect.objectContaining({ method: "PUT", body: JSON.stringify(updatePayload) })
    );

    await api.deleteUser(7);
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      "https://api.runtrack.local/admin/users/7",
      expect.objectContaining({ method: "DELETE", body: undefined })
    );
  });
});

describe("apiClient CRUD activities", () => {
  it("getActivities xây dựng đúng query string user_id, page, limit", async () => {
    fetchMock.mockResolvedValueOnce(mockJsonResponse(200, []));
    fetchMock.mockResolvedValueOnce(mockJsonResponse(200, []));
    const api = createAdminApi(BASE_URL, TOKEN);

    await api.getActivities();
    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      "https://api.runtrack.local/admin/activities?page=1&limit=50",
      expect.objectContaining({ method: "GET" })
    );

    await api.getActivities(9, 3, 15);
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "https://api.runtrack.local/admin/activities?user_id=9&page=3&limit=15",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("createActivity, updateActivity và deleteActivity gọi đúng method, URL và body", async () => {
    fetchMock.mockResolvedValue(mockJsonResponse(200, { id: "act-1" }));
    const api = createAdminApi(BASE_URL, TOKEN);
    const createPayload = { user_id: 9, title: "Morning Run", distance_km: 5, duration_seconds: 1500 };
    const updatePayload = { title: "Evening Run", distance_km: 6.5 };

    await api.createActivity(createPayload);
    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      "https://api.runtrack.local/admin/activities",
      expect.objectContaining({ method: "POST", body: JSON.stringify(createPayload) })
    );

    await api.updateActivity("act-1", updatePayload);
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "https://api.runtrack.local/admin/activities/act-1",
      expect.objectContaining({ method: "PUT", body: JSON.stringify(updatePayload) })
    );

    await api.deleteActivity("act-1");
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      "https://api.runtrack.local/admin/activities/act-1",
      expect.objectContaining({ method: "DELETE", body: undefined })
    );
  });
});

describe("apiClient CRUD clubs", () => {
  it("getClubs xây dựng đúng query string q, page, limit", async () => {
    fetchMock.mockResolvedValueOnce(mockJsonResponse(200, []));
    fetchMock.mockResolvedValueOnce(mockJsonResponse(200, []));
    const api = createAdminApi(BASE_URL, TOKEN);

    await api.getClubs();
    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      "https://api.runtrack.local/admin/clubs?page=1&limit=50",
      expect.objectContaining({ method: "GET" })
    );

    await api.getClubs("saigon", 2, 10);
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "https://api.runtrack.local/admin/clubs?q=saigon&page=2&limit=10",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("createClub, updateClub và deleteClub gọi đúng method, URL và body JSON", async () => {
    fetchMock.mockResolvedValue(mockJsonResponse(200, { club_id: 5 }));
    const api = createAdminApi(BASE_URL, TOKEN);
    const createPayload = { name: "Saigon Runners", description: "Chạy bộ cuối tuần" };
    const updatePayload = { name: "Saigon Pro Runners" };

    await api.createClub(createPayload);
    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      "https://api.runtrack.local/admin/clubs",
      expect.objectContaining({ method: "POST", body: JSON.stringify(createPayload) })
    );

    await api.updateClub(5, updatePayload);
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "https://api.runtrack.local/admin/clubs/5",
      expect.objectContaining({ method: "PUT", body: JSON.stringify(updatePayload) })
    );

    await api.deleteClub(5);
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      "https://api.runtrack.local/admin/clubs/5",
      expect.objectContaining({ method: "DELETE", body: undefined })
    );
  });
});
