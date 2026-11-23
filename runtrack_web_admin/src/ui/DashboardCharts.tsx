import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Filler,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
  type ChartOptions,
} from "chart.js";
import { Bar, Line } from "react-chartjs-2";
import { formatKm, formatNumber, formatShortDate, summarizeDailyStats } from "../core/formatters";
import type { AdminDailyStat, AdminTopRunner } from "../core/types";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
  Filler
);

const BASE_OPTIONS: ChartOptions<"bar"> = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: { ticks: { color: "#9CA3AF" }, grid: { color: "rgba(31,41,55,0.45)" } },
    y: {
      beginAtZero: true,
      ticks: { color: "#9CA3AF" },
      grid: { color: "rgba(31,41,55,0.45)" },
    },
  },
};

function buildDistanceData(dailyStats: AdminDailyStat[]) {
  return {
    labels: dailyStats.map((d) => formatShortDate(d.date)),
    datasets: [
      {
        label: "Quãng đường (km)",
        data: dailyStats.map((d) => d.distance_km),
        backgroundColor: "rgba(57, 255, 20, 0.72)",
        borderColor: "#39FF14",
        borderWidth: 1,
        borderRadius: 6,
      },
    ],
  };
}

function buildRunsData(dailyStats: AdminDailyStat[]) {
  return {
    labels: dailyStats.map((d) => formatShortDate(d.date)),
    datasets: [
      {
        label: "Số buổi chạy",
        data: dailyStats.map((d) => d.activities_count),
        borderColor: "#38BDF8",
        backgroundColor: "rgba(56, 189, 248, 0.16)",
        fill: true,
        tension: 0.35,
        pointRadius: 4,
      },
    ],
  };
}

export function DashboardCharts({ dailyStats }: { dailyStats: AdminDailyStat[] }) {
  const summary = summarizeDailyStats(dailyStats);
  return (
    <div className="charts-grid">
      <div className="card">
        <div className="section-title">
          <span>Quãng đường 7 ngày gần nhất (km)</span>
          <span className="muted">Tổng: {formatKm(summary.totalKm)}</span>
        </div>
        <div className="chart-box">
          <Bar data={buildDistanceData(dailyStats)} options={BASE_OPTIONS} />
        </div>
      </div>
      <div className="card">
        <div className="section-title">
          <span>Số buổi chạy 7 ngày gần nhất</span>
          <span className="muted">Tổng: {formatNumber(summary.totalRuns)} buổi</span>
        </div>
        <div className="chart-box">
          <Line data={buildRunsData(dailyStats)} options={BASE_OPTIONS as ChartOptions<"line">} />
        </div>
      </div>
    </div>
  );
}

function RunnerRow({
  runner,
  rank,
  topDistance,
}: {
  runner: AdminTopRunner;
  rank: number;
  topDistance: number;
}) {
  const pct = Math.round((runner.total_distance_km / topDistance) * 100);
  return (
    <tr>
      <td style={{ fontWeight: 700, color: rank === 1 ? "var(--accent)" : undefined }}>#{rank}</td>
      <td>
        <div style={{ fontWeight: 600 }}>{runner.full_name}</div>
        <div className="muted">{runner.email}</div>
      </td>
      <td>{formatNumber(runner.activities_count)}</td>
      <td style={{ fontWeight: 600 }}>{formatKm(runner.total_distance_km)}</td>
      <td>
        <div className="progress-track">
          <div className="progress-fill" style={{ width: `${pct}%` }} />
        </div>
      </td>
    </tr>
  );
}

export function TopRunnersTable({ runners }: { runners: AdminTopRunner[] }) {
  const topDistance = Math.max(1, ...runners.map((r) => r.total_distance_km));
  return (
    <div className="card">
      <div className="section-title" style={{ marginBottom: 14 }}>
        <span>Bảng xếp hạng Top 5 Runners</span>
      </div>
      {runners.length === 0 ? (
        <p className="muted">Chưa có dữ liệu buổi chạy.</p>
      ) : (
        <div className="table-wrap" style={{ border: "none" }}>
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Vận động viên</th>
                <th>Số buổi</th>
                <th>Tổng quãng đường</th>
                <th style={{ width: "30%" }}>Tiến độ tương đối</th>
              </tr>
            </thead>
            <tbody>
              {runners.map((r, i) => (
                <RunnerRow key={r.user_id} runner={r} rank={i + 1} topDistance={topDistance} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
