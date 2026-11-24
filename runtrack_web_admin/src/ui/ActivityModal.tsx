import { useState, type FormEvent } from "react";
import type {
  AdminActivityCreate,
  AdminActivityItem,
  AdminActivityUpdate,
} from "../core/types";
import { Modal } from "./components/Modal";

interface ActivityModalProps {
  editing: AdminActivityItem | null;
  busy: boolean;
  onClose: () => void;
  onCreate: (payload: AdminActivityCreate) => Promise<void>;
  onUpdate: (activityId: string, payload: AdminActivityUpdate) => Promise<void>;
}

interface MetricsProps {
  distanceKm: string;
  durationSec: string;
  calories: string;
  onDistanceKm: (v: string) => void;
  onDurationSec: (v: string) => void;
  onCalories: (v: string) => void;
}

function ActivityMetricFields({ distanceKm, durationSec, calories, onDistanceKm, onDurationSec, onCalories }: MetricsProps) {
  return (
    <>
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="a-dist">Quãng đường (km)</label>
          <input
            id="a-dist" type="number" step="0.01" min="0.1" className="form-input"
            value={distanceKm} onChange={(e) => onDistanceKm(e.target.value)} required
          />
        </div>
        <div className="form-group">
          <label htmlFor="a-dur">Thời lượng (giây)</label>
          <input
            id="a-dur" type="number" min="1" className="form-input"
            value={durationSec} onChange={(e) => onDurationSec(e.target.value)} required
          />
        </div>
      </div>
      <div className="form-group">
        <label htmlFor="a-cal">Calories (tùy chọn)</label>
        <input
          id="a-cal" type="number" min="0" className="form-input"
          value={calories} onChange={(e) => onCalories(e.target.value)}
          placeholder="Tự tính nếu để trống"
        />
      </div>
    </>
  );
}

export function ActivityModal({ editing, busy, onClose, onCreate, onUpdate }: ActivityModalProps) {
  const [userId, setUserId] = useState(String(editing?.user_id ?? "1"));
  const [title, setTitle] = useState(editing?.title ?? "Buổi chạy bộ");
  const [distanceKm, setDistanceKm] = useState(String(editing?.distance_km ?? "5"));
  const [durationSec, setDurationSec] = useState(String(editing?.duration_seconds ?? "1800"));
  const [calories, setCalories] = useState(editing ? String(editing.calories_burned) : "");

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const base = {
      title: title.trim(),
      distance_km: Number(distanceKm),
      duration_seconds: Number(durationSec),
      calories_burned: calories.trim() ? Number(calories) : undefined,
    };
    await (editing ? onUpdate(editing.id, base) : onCreate({ ...base, user_id: Number(userId) }));
  }

  return (
    <Modal title={editing ? "Chỉnh sửa buổi chạy" : "Thêm buổi chạy mới"} onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {!editing && (
          <div className="form-group">
            <label htmlFor="a-uid">ID Người dùng (user_id)</label>
            <input id="a-uid" type="number" min="1" className="form-input" value={userId} onChange={(e) => setUserId(e.target.value)} required />
          </div>
        )}
        <div className="form-group">
          <label htmlFor="a-title">Tiêu đề buổi chạy</label>
          <input id="a-title" type="text" className="form-input" value={title} onChange={(e) => setTitle(e.target.value)} required />
        </div>
        <ActivityMetricFields
          distanceKm={distanceKm} durationSec={durationSec} calories={calories}
          onDistanceKm={setDistanceKm} onDurationSec={setDurationSec} onCalories={setCalories}
        />
        <div className="modal-footer">
          <button type="button" className="btn" onClick={onClose} disabled={busy}>Hủy</button>
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? "Đang lưu..." : editing ? "Cập nhật" : "Tạo buổi chạy"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
