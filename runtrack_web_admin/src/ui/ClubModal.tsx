import { useState, type FormEvent } from "react";
import type { ClubCreate, ClubResponse, ClubUpdate } from "../core/types";
import { Modal } from "./components/Modal";

interface ClubModalProps {
  editing: ClubResponse | null;
  busy: boolean;
  onClose: () => void;
  onCreate: (payload: ClubCreate) => Promise<void>;
  onUpdate: (clubId: number, payload: ClubUpdate) => Promise<void>;
}

export function ClubModal({ editing, busy, onClose, onCreate, onUpdate }: ClubModalProps) {
  const [name, setName] = useState(editing?.name ?? "");
  const [description, setDescription] = useState(editing?.description ?? "");

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const payload = { name: name.trim(), description: description.trim() };
    await (editing ? onUpdate(editing.club_id, payload) : onCreate(payload));
  }

  return (
    <Modal title={editing ? "Chỉnh sửa câu lạc bộ" : "Tạo câu lạc bộ mới"} onClose={onClose}>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="c-name">Tên câu lạc bộ</label>
          <input
            id="c-name"
            type="text"
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="c-desc">Mô tả</label>
          <textarea
            id="c-desc"
            rows={4}
            className="form-textarea"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Giới thiệu hoạt động câu lạc bộ..."
          />
        </div>
        <div className="modal-footer">
          <button type="button" className="btn" onClick={onClose} disabled={busy}>Hủy</button>
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? "Đang lưu..." : editing ? "Cập nhật" : "Tạo CLB"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
