import { useCallback, type FormEvent } from "react";
import { Pencil, Plus, Search, Trash2, Users } from "lucide-react";
import type { AdminApi } from "../core/apiClient";
import { formatNumber } from "../core/formatters";
import type { ClubCreate, ClubResponse, ClubUpdate } from "../core/types";
import { ClubModal } from "./ClubModal";
import { ConfirmDeleteModal, EmptyState, ErrorBanner, LoadingState } from "./components/StatusViews";
import { useCrudState } from "./useCrudState";

function ClubsTable({
  clubs,
  onEdit,
  onDelete,
}: {
  clubs: ClubResponse[];
  onEdit: (club: ClubResponse) => void;
  onDelete: (club: ClubResponse) => void;
}) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên câu lạc bộ</th>
            <th>Mô tả</th>
            <th>Thành viên</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          {clubs.map((club) => (
            <tr key={club.club_id}>
              <td>#{club.club_id}</td>
              <td style={{ fontWeight: 600 }}>{club.name}</td>
              <td className="muted" style={{ maxWidth: 420 }}>{club.description || "Chưa có mô tả"}</td>
              <td>
                <span className="badge badge-admin" style={{ gap: 5 }}>
                  <Users size={12} /> {formatNumber(club.member_count)}
                </span>
              </td>
              <td>
                <div className="actions-cell">
                  <button type="button" className="btn btn-sm" onClick={() => onEdit(club)}>
                    <Pencil size={14} /> Sửa
                  </button>
                  <button type="button" className="btn btn-sm btn-danger" onClick={() => onDelete(club)}>
                    <Trash2 size={14} /> Xóa
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ClubsToolbar({
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
          type="text" placeholder="Tìm theo tên câu lạc bộ..."
          value={query} onChange={(e) => onQuery(e.target.value)}
        />
      </div>
      <button type="submit" className="btn">Tìm kiếm</button>
    </form>
  );
}

export function ClubsPage({ api }: { api: AdminApi }) {
  const fetcher = useCallback((q: string) => api.getClubs(q), [api]);
  const s = useCrudState(fetcher);
  const onSearch = (e: FormEvent) => { e.preventDefault(); void s.load(s.filter.trim()); };

  return (
    <section>
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản lý câu lạc bộ</h1>
          <p className="page-subtitle">Danh sách các câu lạc bộ chạy bộ trong cộng đồng</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={() => { s.setEditing(null); s.setModalOpen(true); }}>
          <Plus size={16} /> Thêm CLB
        </button>
      </div>
      <ClubsToolbar query={s.filter} onQuery={s.setFilter} onSearch={onSearch} />
      {s.error && <ErrorBanner message={s.error} onDismiss={() => s.setError("")} />}
      {s.loading ? (
        <LoadingState message="Đang tải danh sách câu lạc bộ..." />
      ) : s.items.length === 0 ? (
        <EmptyState message="Không tìm thấy câu lạc bộ nào." />
      ) : (
        <ClubsTable
          clubs={s.items}
          onEdit={(c) => { s.setEditing(c); s.setModalOpen(true); }}
          onDelete={(c) => s.setDeleting(c)}
        />
      )}
      {s.modalOpen && (
        <ClubModal
          editing={s.editing} busy={s.busy} onClose={() => s.setModalOpen(false)}
          onCreate={(p: ClubCreate) => s.mutate(() => api.createClub(p), () => s.setModalOpen(false), s.filter.trim())}
          onUpdate={(id: number, p: ClubUpdate) => s.mutate(() => api.updateClub(id, p), () => s.setModalOpen(false), s.filter.trim())}
        />
      )}
      {s.deleting && (
        <ConfirmDeleteModal
          title="Xóa câu lạc bộ"
          description={`Bạn có chắc muốn xóa câu lạc bộ "${s.deleting.name}"?`}
          busy={s.busy} onCancel={() => s.setDeleting(null)}
          onConfirm={() => void s.mutate(() => api.deleteClub(s.deleting!.club_id), () => s.setDeleting(null), s.filter.trim())}
        />
      )}
    </section>
  );
}
