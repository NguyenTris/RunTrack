import { useState, type FormEvent } from "react";
import type { AdminUserCreate, AdminUserItem, AdminUserUpdate, UserRole } from "../core/types";
import { Modal } from "./components/Modal";

interface UserModalProps {
  editing: AdminUserItem | null;
  busy: boolean;
  onClose: () => void;
  onCreate: (payload: AdminUserCreate) => Promise<void>;
  onUpdate: (userId: number, payload: AdminUserUpdate) => Promise<void>;
}

interface CredentialsProps {
  email: string;
  password: string;
  onEmail: (v: string) => void;
  onPassword: (v: string) => void;
}

function CredentialsFields({ email, password, onEmail, onPassword }: CredentialsProps) {
  return (
    <>
      <div className="form-group">
        <label htmlFor="u-email">Email</label>
        <input
          id="u-email"
          type="email"
          className="form-input"
          value={email}
          onChange={(e) => onEmail(e.target.value)}
          required
        />
      </div>
      <div className="form-group">
        <label htmlFor="u-pass">Mật khẩu</label>
        <input
          id="u-pass"
          type="password"
          className="form-input"
          value={password}
          onChange={(e) => onPassword(e.target.value)}
          required
        />
      </div>
    </>
  );
}

interface ProfileProps {
  fullName: string;
  role: UserRole;
  weeklyGoal: string;
  onFullName: (v: string) => void;
  onRole: (v: UserRole) => void;
  onWeeklyGoal: (v: string) => void;
}

function ProfileFields({ fullName, role, weeklyGoal, onFullName, onRole, onWeeklyGoal }: ProfileProps) {
  return (
    <>
      <div className="form-group">
        <label htmlFor="u-name">Họ và tên</label>
        <input
          id="u-name"
          type="text"
          className="form-input"
          value={fullName}
          onChange={(e) => onFullName(e.target.value)}
          required
        />
      </div>
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="u-role">Vai trò</label>
          <select
            id="u-role"
            className="form-select"
            value={role}
            onChange={(e) => onRole(e.target.value as UserRole)}
          >
            <option value="user">User</option>
            <option value="admin">Admin</option>
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="u-goal">Mục tiêu tuần (km)</label>
          <input
            id="u-goal"
            type="number"
            step="0.5"
            min="1"
            className="form-input"
            value={weeklyGoal}
            onChange={(e) => onWeeklyGoal(e.target.value)}
            required
          />
        </div>
      </div>
    </>
  );
}

export function UserModal({ editing, busy, onClose, onCreate, onUpdate }: UserModalProps) {
  const [email, setEmail] = useState(editing?.email ?? "");
  const [password, setPassword] = useState("");
  const [fullName, setFullName] = useState(editing?.full_name ?? "");
  const [role, setRole] = useState<UserRole>(editing?.role ?? "user");
  const [weeklyGoal, setWeeklyGoal] = useState(String(editing?.weekly_goal_km ?? 40));

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const goal = Number(weeklyGoal) || 40;
    if (editing) {
      await onUpdate(editing.id, { full_name: fullName.trim(), role, weekly_goal_km: goal });
      return;
    }
    await onCreate({ email: email.trim(), password, full_name: fullName.trim(), role, weekly_goal_km: goal });
  }

  return (
    <Modal title={editing ? `Chỉnh sửa: ${editing.email}` : "Thêm người dùng mới"} onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {!editing && (
          <CredentialsFields email={email} password={password} onEmail={setEmail} onPassword={setPassword} />
        )}
        <ProfileFields
          fullName={fullName} role={role} weeklyGoal={weeklyGoal}
          onFullName={setFullName} onRole={setRole} onWeeklyGoal={setWeeklyGoal}
        />
        <div className="modal-footer">
          <button type="button" className="btn" onClick={onClose} disabled={busy}>Hủy</button>
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? "Đang lưu..." : editing ? "Cập nhật" : "Tạo người dùng"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
