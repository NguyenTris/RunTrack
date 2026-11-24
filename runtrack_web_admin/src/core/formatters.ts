import type { AdminDailyStat, DailyStatsSummary } from "./types";

function pad2(num: number): string {
  return String(Math.floor(Math.max(0, num))).padStart(2, "0");
}

export function formatKm(km: number): string {
  const safe = Number.isFinite(km) && km >= 0 ? km : 0;
  return `${safe.toFixed(2)} km`;
}

export function formatDuration(seconds: number): string {
  const total = Math.max(0, Math.floor(Number.isFinite(seconds) ? seconds : 0));
  const hours = Math.floor(total / 3600);
  const mins = Math.floor((total % 3600) / 60);
  const secs = total % 60;
  if (hours > 0) {
    return `${pad2(hours)}:${pad2(mins)}:${pad2(secs)}`;
  }
  return `${pad2(mins)}:${pad2(secs)}`;
}

export function formatPace(paceSeconds: number): string {
  if (!Number.isFinite(paceSeconds) || paceSeconds <= 0) {
    return "--";
  }
  const mins = Math.floor(paceSeconds / 60);
  const secs = Math.floor(paceSeconds % 60);
  return `${mins}'${pad2(secs)}"/km`;
}

export function formatDate(iso: string | null | undefined): string {
  if (!iso) {
    return "—";
  }
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return iso;
  }
  return `${pad2(date.getDate())}/${pad2(date.getMonth() + 1)}/${date.getFullYear()}`;
}

export function formatShortDate(dateStr: string): string {
  const parts = dateStr.split("-");
  if (parts.length === 3) {
    return `${parts[2]}/${parts[1]}`;
  }
  return dateStr;
}

export function formatNumber(value: number): string {
  const safe = Number.isFinite(value) ? value : 0;
  return new Intl.NumberFormat("vi-VN").format(safe);
}

export function summarizeDailyStats(stats: AdminDailyStat[]): DailyStatsSummary {
  let totalKm = 0;
  let totalRuns = 0;
  let maxKm = 0;

  for (const item of stats) {
    const km = Number.isFinite(item.distance_km) ? item.distance_km : 0;
    const runs = Number.isFinite(item.activities_count) ? item.activities_count : 0;
    totalKm += km;
    totalRuns += runs;
    if (km > maxKm) {
      maxKm = km;
    }
  }

  return {
    totalKm: Math.round(totalKm * 100) / 100,
    totalRuns,
    maxKm: Math.round(maxKm * 100) / 100,
  };
}
