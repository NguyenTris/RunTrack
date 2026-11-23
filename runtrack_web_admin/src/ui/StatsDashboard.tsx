import { useCallback, useEffect, useState } from "react";
import { Flame, Flag, Footprints, RefreshCw, Route, Users } from "lucide-react";
import type { AdminApi } from "../core/apiClient";
import { formatKm, formatNumber } from "../core/formatters";
import type { AdminStatsResponse } from "../core/types";
import { ErrorBanner, LoadingState } from "./components/StatusViews";
import { DashboardCharts, TopRunnersTable } from "./DashboardCharts";

interface StatsDashboardProps {
  api: AdminApi;
}

function KpiGrid({ stats }: { stats: AdminStatsResponse }) {
  const items = [
    { label: "Tổng Users", value: formatNumber(stats.total_users), Icon: Users },
    { label: "Tổng Buổi chạy", value: formatNumber(stats.total_activities), Icon: Footprints },
    { label: "Tổng Quãng đường", value: formatKm(stats.total_distance_km), Icon: Route },
    { label: "Tổng Calories", value: `${formatNumber(stats.total_calories)} kcal`, Icon: Flame },
    { label: "Tổng Câu lạc bộ", value: formatNumber(stats.total_clubs), Icon: Flag },
  ];

  return (
    <div className="kpi-grid">
      {items.map(({ label, value, Icon }) => (
        <div key={label} className="kpi-card">
          <div className="kpi-top">
            <span>{label}</span>
            <Icon size={18} className="kpi-icon" />
          </div>
          <div className="kpi-value">{value}</div>
        </div>
      ))}
    </div>
  );
}

export function StatsDashboard({ api }: StatsDashboardProps) {
  const [stats, setStats] = useState<AdminStatsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchStats = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await api.getStats();
      setStats(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Không thể tải thống kê hệ thống");
    } finally {
      setLoading(false);
    }
  }, [api]);

  useEffect(() => {
    void fetchStats();
  }, [fetchStats]);

  return (
    <section>
      <div className="page-header">
        <div>
          <h1 className="page-title">Tổng quan hệ thống</h1>
          <p className="page-subtitle">Thống kê hoạt động chạy bộ và cộng đồng RunTrack</p>
        </div>
        <button type="button" className="btn" onClick={() => void fetchStats()} disabled={loading}>
          <RefreshCw size={15} />
          Làm mới
        </button>
      </div>

      {error && <ErrorBanner message={error} onDismiss={() => setError("")} />}
      {loading && <LoadingState message="Đang tải thống kê tổng quan..." />}
      {!loading && stats && (
        <>
          <KpiGrid stats={stats} />
          <DashboardCharts dailyStats={stats.daily_stats} />
          <TopRunnersTable runners={stats.top_runners} />
        </>
      )}
    </section>
  );
}
