import { Activity, BarChart3, Flag, Footprints, LogOut, Users } from "lucide-react";
import type { AdminTab } from "../core/types";

interface SidebarProps {
  activeTab: AdminTab;
  adminEmail: string;
  onSelectTab: (tab: AdminTab) => void;
  onLogout: () => void;
}

const NAV_ITEMS: Array<{ id: AdminTab; label: string; Icon: typeof BarChart3 }> = [
  { id: "dashboard", label: "Tổng quan", Icon: BarChart3 },
  { id: "users", label: "Người dùng", Icon: Users },
  { id: "activities", label: "Buổi chạy", Icon: Footprints },
  { id: "clubs", label: "Câu lạc bộ", Icon: Flag },
];

export function Sidebar({ activeTab, adminEmail, onSelectTab, onLogout }: SidebarProps) {
  return (
    <aside className="sidebar">
      <div>
        <div className="brand">
          <div className="brand-icon">
            <Activity size={20} />
          </div>
          <div>
            <div className="brand-title">RunTrack</div>
            <div className="brand-sub">Admin Portal</div>
          </div>
        </div>

        <nav className="nav-list" aria-label="Điều hướng chính">
          {NAV_ITEMS.map(({ id, label, Icon }) => (
            <button
              key={id}
              type="button"
              className={`nav-btn ${activeTab === id ? "active" : ""}`}
              onClick={() => onSelectTab(id)}
            >
              <Icon size={17} />
              <span>{label}</span>
            </button>
          ))}
        </nav>
      </div>

      <div className="sidebar-footer">
        <div className="admin-email" title={adminEmail}>
          {adminEmail}
        </div>
        <button type="button" className="btn btn-danger" onClick={onLogout}>
          <LogOut size={15} />
          Đăng xuất
        </button>
      </div>
    </aside>
  );
}
