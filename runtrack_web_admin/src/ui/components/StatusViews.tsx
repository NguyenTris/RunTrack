import { AlertTriangle, Loader2 } from "lucide-react";
import { Modal } from "./Modal";

export function LoadingState({ message = "Đang tải dữ liệu..." }: { message?: string }) {
  return (
    <div className="status-box">
      <Loader2 size={24} style={{ color: "var(--accent)", marginBottom: 8 }} />
      <p>{message}</p>
    </div>
  );
}

export function EmptyState({ message }: { message: string }) {
  return (
    <div className="status-box">
      <p>{message}</p>
    </div>
  );
}

export function ErrorBanner({
  message,
  onDismiss,
}: {
  message: string;
  onDismiss?: () => void;
}) {
  return (
    <div className="alert-error" role="alert">
      <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
        <AlertTriangle size={16} />
        {message}
      </span>
      {onDismiss && (
        <button type="button" className="btn btn-sm" onClick={onDismiss}>
          Đóng
        </button>
      )}
    </div>
  );
}

interface ConfirmDeleteProps {
  title: string;
  description: string;
  busy: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export function ConfirmDeleteModal({
  title,
  description,
  busy,
  onCancel,
  onConfirm,
}: ConfirmDeleteProps) {
  return (
    <Modal title={title} onClose={onCancel}>
      <p className="muted" style={{ fontSize: 14, color: "var(--text-primary)" }}>
        {description}
      </p>
      <div className="modal-footer">
        <button type="button" className="btn" onClick={onCancel} disabled={busy}>
          Hủy
        </button>
        <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={busy}>
          {busy ? "Đang xóa..." : "Xác nhận xóa"}
        </button>
      </div>
    </Modal>
  );
}
