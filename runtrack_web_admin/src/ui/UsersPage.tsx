import { useCallback, type FormEvent } from "react";
import { Pencil, Plus, Search, Trash2 } from "lucide-react";
import type { AdminApi } from "../core/apiClient";
import { formatDate, formatKm, formatNumber } from "../core/formatters";
import type { AdminUserCreate, AdminUserItem, AdminUserUpdate } from "../core/types";
import { ConfirmDeleteModal, EmptyState, ErrorBanner, LoadingState } from "./components/StatusViews";
import { useCrudState } from "./useCrudState";
import { UserModal } from "./UserModal";

function UserRow({
  user,
  onEdit,
  onDelete,
}: {
  user: AdminUserItem;
  onEdit: (u: AdminUserItem) => void;
  onDelete: (u: AdminUserItem) => void;
}) {
  return (
    <tr>
      <td>#{user.id}</td>
      <td>
        <div style={{ fontWeight: 600 }}>{user.full_name}</div>
        <div className="muted">{user.email}</div>
      </td>
      <td>
        <span className={`badge ${user.role === "admin" ? "badge-admin" : "badge-user"}`}>
          {user.role}
        </span>
      </td>
      <td>{formatKm(user.weekly_goal_km)}</td>
      <td>{formatNumber(user.activities_count)}</td>
      <td>{formatKm(user.total_distance_km)}</td>
      <td>{formatDate(user.created_at)}</td>
      <td>
        <div className="actions-cell">
          <button type="button" className="btn btn-sm" onClick={() => onEdit(user)}>
            <Pencil size={14} /> Sửa
          </button>
          <button type="button" className="btn btn-sm btn-danger" onClick={() => onDelete(user)}>
            <Trash2 size={14} /> Xóa
          </button>
        </div>
      </td>
    </tr>
  );
}

function UsersTable({
  users,
  onEdit,
  onDelete,
}: {
  users: AdminUserItem[];
  onEdit: (u: AdminUserItem) => void;
  onDelete: (u: AdminUserItem) => void;
}) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Người dùng</th>
            <th>Vai trò</th>
            <th>Mục tiêu tuần</th>
            <th>Buổi chạy</th>
            <th>Tổng km</th>
            <th>Ngày tạo</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <UserRow key={u.id} user={u} onEdit={onEdit} onDelete={onDelete} />
          ))}
        </tbody>
      </table>
    </div>
  );
}

function UsersToolbar({
  query,
  onQuery,
  onSearch,
}: {
  query: string;
  onQuery: (v: string) => void;
  onSearch: (e: FormEvent) => void;
}) {
  return (
    <form className="toolbar" onSubmit={onSearch}>
      <div className="search-box">
        <Search size={16} className="muted" />
        <input
          type="text"
          placeholder="Tìm theo tên hoặc email..."
          value={query}
          onChange={(e) => onQuery(e.target.value)}
        />
      </div>
      <button type="submit" className="btn">Tìm kiếm</button>
    </form>
  );
}

export function UsersPage({ api }: { api: AdminApi }) {
  const fetcher = useCallback((q: string) => api.getUsers(q), [api]);
  const s = useCrudState(fetcher);
  const onSearch = (e: FormEvent) => { e.preventDefault(); void s.load(s.filter.trim()); };

  return (
    <section>
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản lý người dùng</h1>
          <p className="page-subtitle">Tìm kiếm, phân quyền và quản lý tài khoản RunTrack</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={() => { s.setEditing(null); s.setModalOpen(true); }}>
          <Plus size={16} /> Thêm người dùng
        </button>
      </div>
      <UsersToolbar query={s.filter} onQuery={s.setFilter} onSearch={onSearch} />
      {s.error && <ErrorBanner message={s.error} onDismiss={() => s.setError("")} />}
      {s.loading ? (
        <LoadingState message="Đang tải danh sách người dùng..." />
      ) : s.items.length === 0 ? (
        <EmptyState message="Không tìm thấy người dùng nào." />
      ) : (
        <UsersTable
          users={s.items}
          onEdit={(u) => { s.setEditing(u); s.setModalOpen(true); }}
          onDelete={(u) => s.setDeleting(u)}
        />
      )}
      {s.modalOpen && (
        <UserModal
          editing={s.editing} busy={s.busy} onClose={() => s.setModalOpen(false)}
          onCreate={(p: AdminUserCreate) => s.mutate(() => api.createUser(p), () => s.setModalOpen(false), s.filter.trim())}
          onUpdate={(id: number, p: AdminUserUpdate) => s.mutate(() => api.updateUser(id, p), () => s.setModalOpen(false), s.filter.trim())}
        />
      )}
      {s.deleting && (
        <ConfirmDeleteModal
          title="Xóa người dùng"
          description={`Bạn có chắc muốn xóa tài khoản ${s.deleting.full_name} (${s.deleting.email})?`}
          busy={s.busy} onCancel={() => s.setDeleting(null)}
          onConfirm={() => void s.mutate(() => api.deleteUser(s.deleting!.id), () => s.setDeleting(null), s.filter.trim())}
        />
      )}
    </section>
  );
}
