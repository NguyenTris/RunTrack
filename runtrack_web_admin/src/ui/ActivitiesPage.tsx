import { useCallback, type FormEvent } from "react";
import { Filter, Pencil, Plus, Trash2 } from "lucide-react";
import type { AdminApi } from "../core/apiClient";
import { formatDate, formatDuration, formatKm, formatNumber, formatPace } from "../core/formatters";
import type { AdminActivityCreate, AdminActivityItem, AdminActivityUpdate } from "../core/types";
import { ActivityModal } from "./ActivityModal";
import { ConfirmDeleteModal, EmptyState, ErrorBanner, LoadingState } from "./components/StatusViews";
import { useCrudState } from "./useCrudState";

function ActivityRow({
  item,
  onEdit,
  onDelete,
}: {
  item: AdminActivityItem;
  onEdit: (item: AdminActivityItem) => void;
  onDelete: (item: AdminActivityItem) => void;
}) {
  return (
    <tr>
      <td>
        <div style={{ fontWeight: 600 }}>{item.user_full_name}</div>
        <div className="muted">ID #{item.user_id} • {item.user_email}</div>
      </td>
      <td>{item.title}</td>
      <td style={{ fontWeight: 600, color: "var(--accent)" }}>{formatKm(item.distance_km)}</td>
      <td>{formatDuration(item.duration_seconds)}</td>
      <td>{formatPace(item.avg_pace_seconds)}</td>
      <td>{formatNumber(item.calories_burned)} kcal</td>
      <td>{formatDate(item.start_time)}</td>
      <td>
        <div className="actions-cell">
          <button type="button" className="btn btn-sm" onClick={() => onEdit(item)}>
            <Pencil size={14} /> Sửa
          </button>
          <button type="button" className="btn btn-sm btn-danger" onClick={() => onDelete(item)}>
            <Trash2 size={14} /> Xóa
          </button>
        </div>
      </td>
    </tr>
  );
}

function ActivitiesTable({
  items,
  onEdit,
  onDelete,
}: {
  items: AdminActivityItem[];
  onEdit: (item: AdminActivityItem) => void;
  onDelete: (item: AdminActivityItem) => void;
}) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Vận động viên</th>
            <th>Tiêu đề</th>
            <th>Quãng đường</th>
            <th>Thời lượng</th>
            <th>Pace TB</th>
            <th>Calories</th>
            <th>Ngày chạy</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <ActivityRow key={item.id} item={item} onEdit={onEdit} onDelete={onDelete} />
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ActivitiesToolbar({
  value,
  onChange,
  onSubmit,
  onClear,
}: {
  value: string;
  onChange: (v: string) => void;
  onSubmit: (e: FormEvent) => void;
  onClear: () => void;
}) {
  return (
    <form className="toolbar" onSubmit={onSubmit}>
      <div className="search-box">
        <Filter size={16} className="muted" />
        <input
          type="number" min="1" placeholder="Lọc theo user_id..."
          value={value} onChange={(e) => onChange(e.target.value)}
        />
      </div>
      <button type="submit" className="btn">Lọc</button>
      {value && (
        <button type="button" className="btn" onClick={onClear}>Bỏ lọc</button>
      )}
    </form>
  );
}

export function ActivitiesPage({ api }: { api: AdminApi }) {
  const fetcher = useCallback(
    (uid: string) => api.getActivities(uid.trim() ? Number(uid.trim()) : undefined),
    [api]
  );
  const s = useCrudState(fetcher);
  const onFilter = (e: FormEvent) => { e.preventDefault(); void s.load(s.filter); };

  return (
    <section>
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản lý buổi chạy</h1>
          <p className="page-subtitle">Theo dõi, lọc theo người dùng và quản lý lịch sử chạy bộ</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={() => { s.setEditing(null); s.setModalOpen(true); }}>
          <Plus size={16} /> Thêm buổi chạy
        </button>
      </div>
      <ActivitiesToolbar
        value={s.filter} onChange={s.setFilter} onSubmit={onFilter}
        onClear={() => { s.setFilter(""); void s.load(""); }}
      />
      {s.error && <ErrorBanner message={s.error} onDismiss={() => s.setError("")} />}
      {s.loading ? <LoadingState message="Đang tải danh sách buổi chạy..." /> : s.items.length === 0 ? (
        <EmptyState message="Không có buổi chạy nào." />
      ) : (
        <ActivitiesTable items={s.items} onEdit={(i) => { s.setEditing(i); s.setModalOpen(true); }} onDelete={s.setDeleting} />
      )}
      {s.modalOpen && (
        <ActivityModal
          editing={s.editing} busy={s.busy} onClose={() => s.setModalOpen(false)}
          onCreate={(p: AdminActivityCreate) => s.mutate(() => api.createActivity(p), () => s.setModalOpen(false), s.filter)}
          onUpdate={(id: string, p: AdminActivityUpdate) => s.mutate(() => api.updateActivity(id, p), () => s.setModalOpen(false), s.filter)}
        />
      )}
      {s.deleting && (
        <ConfirmDeleteModal
          title="Xóa buổi chạy"
          description={`Xác nhận xóa buổi chạy "${s.deleting.title}" của ${s.deleting.user_full_name}?`}
          busy={s.busy} onCancel={() => s.setDeleting(null)}
          onConfirm={() => void s.mutate(() => api.deleteActivity(s.deleting!.id), () => s.setDeleting(null), s.filter)}
        />
      )}
    </section>
  );
}
