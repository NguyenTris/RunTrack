import { useState, type FormEvent } from "react";
import { Activity, LogIn } from "lucide-react";
import { ApiError, createAdminApi } from "../core/apiClient";
import type { AdminSession } from "../core/types";
import { ErrorBanner } from "./components/StatusViews";

interface LoginPageProps {
  baseUrl: string;
  onSuccess: (session: AdminSession) => void;
}

function mapLoginError(err: unknown): string {
  if (err instanceof ApiError) {
    if (err.status === 403) {
      return "Tài khoản không có quyền quản trị viên";
    }
    if (err.status === 401) {
      return "Email hoặc mật khẩu không chính xác";
    }
    return err.detail;
  }
  return "Không thể kết nối tới máy chủ RunTrack";
}

function DemoAccountBox({ onFill }: { onFill: () => void }) {
  return (
    <div className="demo-hint">
      <div>
        <div style={{ color: "var(--text-primary)", fontWeight: 600 }}>Tài khoản Admin demo</div>
        <div>admin@runtrack.app / Admin@123</div>
      </div>
      <button type="button" className="btn btn-sm" onClick={onFill}>
        Điền nhanh
      </button>
    </div>
  );
}

interface LoginFormProps {
  email: string;
  password: string;
  loading: boolean;
  onEmail: (v: string) => void;
  onPassword: (v: string) => void;
  onSubmit: (e: FormEvent) => void;
}

function LoginForm({ email, password, loading, onEmail, onPassword, onSubmit }: LoginFormProps) {
  return (
    <form onSubmit={onSubmit}>
      <div className="form-group">
        <label htmlFor="login-email">Email quản trị</label>
        <input
          id="login-email"
          type="email"
          className="form-input"
          placeholder="admin@runtrack.app"
          value={email}
          onChange={(e) => onEmail(e.target.value)}
          required
        />
      </div>
      <div className="form-group">
        <label htmlFor="login-password">Mật khẩu</label>
        <input
          id="login-password"
          type="password"
          className="form-input"
          placeholder="••••••••"
          value={password}
          onChange={(e) => onPassword(e.target.value)}
          required
        />
      </div>
      <button
        type="submit"
        className="btn btn-primary"
        style={{ width: "100%", marginTop: 6 }}
        disabled={loading}
      >
        <LogIn size={16} />
        {loading ? "Đang xác thực..." : "Đăng nhập"}
      </button>
    </form>
  );
}

export function LoginPage({ baseUrl, onSuccess }: LoginPageProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const auth = await createAdminApi(baseUrl).login(email.trim(), password);
      await createAdminApi(baseUrl, auth.access_token).getStats();
      onSuccess({ token: auth.access_token, email: email.trim() });
    } catch (err) {
      setError(mapLoginError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-shell">
      <div className="login-card">
        <div className="brand" style={{ borderBottom: "none", paddingBottom: 14 }}>
          <div className="brand-icon"><Activity size={20} /></div>
          <div>
            <div className="brand-title">RunTrack Admin</div>
            <div className="brand-sub">Cổng quản trị hệ thống</div>
          </div>
        </div>
        {error && <ErrorBanner message={error} onDismiss={() => setError("")} />}
        <LoginForm
          email={email}
          password={password}
          loading={loading}
          onEmail={setEmail}
          onPassword={setPassword}
          onSubmit={handleSubmit}
        />
        <DemoAccountBox
          onFill={() => {
            setEmail("admin@runtrack.app");
            setPassword("Admin@123");
          }}
        />
      </div>
    </div>
  );
}
