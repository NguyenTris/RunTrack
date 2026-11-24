import { useMemo, useState } from "react";
import { createAdminApi, type AdminApi } from "../core/apiClient";
import type { AdminSession, AdminTab } from "../core/types";
import { ActivitiesPage } from "./ActivitiesPage";
import { ClubsPage } from "./ClubsPage";
import { LoginPage } from "./LoginPage";
import { clearSession, loadSession, saveSession } from "./sessionStorage";
import { Sidebar } from "./Sidebar";
import { StatsDashboard } from "./StatsDashboard";
import { UsersPage } from "./UsersPage";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8000";

function ActiveView({ tab, api }: { tab: AdminTab; api: AdminApi }) {
  if (tab === "users") {
    return <UsersPage api={api} />;
  }
  if (tab === "activities") {
    return <ActivitiesPage api={api} />;
  }
  if (tab === "clubs") {
    return <ClubsPage api={api} />;
  }
  return <StatsDashboard api={api} />;
}

export function App() {
  const [session, setSession] = useState<AdminSession | null>(() => loadSession());
  const [activeTab, setActiveTab] = useState<AdminTab>("dashboard");

  const api = useMemo(
    () => createAdminApi(API_BASE_URL, session?.token ?? ""),
    [session?.token]
  );

  function handleLoginSuccess(nextSession: AdminSession) {
    saveSession(nextSession);
    setSession(nextSession);
    setActiveTab("dashboard");
  }

  async function handleLogout() {
    try {
      await api.logout();
    } catch {
      // Luôn đăng xuất phiên local kể cả khi mạng lỗi
    } finally {
      clearSession();
      setSession(null);
    }
  }

  if (!session) {
    return <LoginPage baseUrl={API_BASE_URL} onSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="app-layout">
      <Sidebar
        activeTab={activeTab}
        adminEmail={session.email}
        onSelectTab={setActiveTab}
        onLogout={() => void handleLogout()}
      />
      <main className="main-content">
        <ActiveView tab={activeTab} api={api} />
      </main>
    </div>
  );
}
