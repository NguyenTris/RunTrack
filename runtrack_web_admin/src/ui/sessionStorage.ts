import type { AdminSession } from "../core/types";

const SESSION_KEY = "runtrack_admin_session";

export function loadSession(): AdminSession | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw) as Partial<AdminSession>;
    if (typeof parsed.token === "string" && typeof parsed.email === "string") {
      return { token: parsed.token, email: parsed.email };
    }
    return null;
  } catch {
    return null;
  }
}

export function saveSession(session: AdminSession): void {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession(): void {
  localStorage.removeItem(SESSION_KEY);
}
