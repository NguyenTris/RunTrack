import { describe, expect, it } from "vitest";
import {
  formatDate,
  formatDuration,
  formatKm,
  formatNumber,
  formatPace,
  formatShortDate,
  summarizeDailyStats,
} from "./formatters";
import type { AdminDailyStat } from "./types";

function seededRandom(seed: number): () => number {
  let state = seed >>> 0;
  return () => {
    state = (1664525 * state + 1013904223) >>> 0;
    return state / 0x100000000;
  };
}

describe("formatKm", () => {
  it("định dạng quãng đường hợp lệ với 2 chữ số thập phân", () => {
    expect(formatKm(0)).toBe("0.00 km");
    expect(formatKm(5.126)).toBe("5.13 km");
    expect(formatKm(42.195)).toBe("42.20 km");
  });

  it("trả về 0.00 km cho số âm, NaN và Infinity", () => {
    expect(formatKm(-0.5)).toBe("0.00 km");
    expect(formatKm(-100)).toBe("0.00 km");
    expect(formatKm(Number.NaN)).toBe("0.00 km");
    expect(formatKm(Number.POSITIVE_INFINITY)).toBe("0.00 km");
    expect(formatKm(Number.NEGATIVE_INFINITY)).toBe("0.00 km");
  });

  it("property-based: luôn kết thúc bằng ' km' và có đúng 2 chữ số thập phân", () => {
    const rand = seededRandom(42);
    for (let i = 0; i < 50; i += 1) {
      const val = (rand() - 0.3) * 1000;
      const formatted = formatKm(val);
      expect(formatted).toMatch(/^\d+\.\d{2} km$/);
    }
  });
});

describe("formatDuration", () => {
  it("định dạng thời lượng dưới 1 giờ thành mm:ss", () => {
    expect(formatDuration(0)).toBe("00:00");
    expect(formatDuration(59)).toBe("00:59");
    expect(formatDuration(65.9)).toBe("01:05");
    expect(formatDuration(3599)).toBe("59:59");
  });

  it("định dạng thời lượng từ 1 giờ trở lên thành hh:mm:ss", () => {
    expect(formatDuration(3600)).toBe("01:00:00");
    expect(formatDuration(3661)).toBe("01:01:01");
    expect(formatDuration(360000)).toBe("100:00:00");
  });

  it("trả về 00:00 cho số âm, NaN và Infinity", () => {
    expect(formatDuration(-10)).toBe("00:00");
    expect(formatDuration(Number.NaN)).toBe("00:00");
    expect(formatDuration(Number.POSITIVE_INFINITY)).toBe("00:00");
    expect(formatDuration(Number.NEGATIVE_INFINITY)).toBe("00:00");
  });

  it("property-based: khớp mẫu mm:ss hoặc hh:mm:ss cho mọi giá trị biên", () => {
    const rand = seededRandom(101);
    for (let i = 0; i < 50; i += 1) {
      const secs = Math.floor(rand() * 20000);
      const result = formatDuration(secs);
      const pattern = secs >= 3600 ? /^\d{2,}:\d{2}:\d{2}$/ : /^\d{2}:\d{2}$/;
      expect(result).toMatch(pattern);
    }
  });
});

describe("formatPace", () => {
  it("định dạng tốc độ hợp lệ thành m'ss\"/km", () => {
    expect(formatPace(300)).toBe("5'00\"/km");
    expect(formatPace(335.8)).toBe("5'35\"/km");
    expect(formatPace(59)).toBe("0'59\"/km");
  });

  it("trả về '--' cho 0, số âm, NaN và Infinity", () => {
    expect(formatPace(0)).toBe("--");
    expect(formatPace(-300)).toBe("--");
    expect(formatPace(Number.NaN)).toBe("--");
    expect(formatPace(Number.POSITIVE_INFINITY)).toBe("--");
    expect(formatPace(Number.NEGATIVE_INFINITY)).toBe("--");
  });

  it("property-based: pace dương luôn có giây trong khoảng 00..59", () => {
    const rand = seededRandom(77);
    for (let i = 0; i < 50; i += 1) {
      const pace = 1 + rand() * 1200;
      expect(formatPace(pace)).toMatch(/^\d+'[0-5]\d"\/km$/);
    }
  });
});

describe("formatDate và formatShortDate", () => {
  it("định dạng chuỗi ISO hợp lệ thành dd/mm/yyyy", () => {
    expect(formatDate("2026-05-09T12:00:00")).toBe("09/05/2026");
    expect(formatDate("2025-12-25T12:00:00")).toBe("25/12/2025");
  });

  it("xử lý chuỗi rỗng, null, undefined và ngày không hợp lệ", () => {
    expect(formatDate("")).toBe("—");
    expect(formatDate(null)).toBe("—");
    expect(formatDate(undefined)).toBe("—");
    expect(formatDate("not-a-date")).toBe("not-a-date");
  });

  it("định dạng ngắn yyyy-mm-dd thành dd/mm và giữ nguyên chuỗi lỗi", () => {
    expect(formatShortDate("2026-05-09")).toBe("09/05");
    expect(formatShortDate("")).toBe("");
    expect(formatShortDate("invalid")).toBe("invalid");
    expect(formatShortDate("2026/05/09")).toBe("2026/05/09");
  });
});

describe("formatNumber", () => {
  it("định dạng số theo chuẩn vi-VN và fallback về 0 khi không hữu hạn", () => {
    const expectedMillion = new Intl.NumberFormat("vi-VN").format(1234567);
    expect(formatNumber(0)).toBe("0");
    expect(formatNumber(1234567)).toBe(expectedMillion);
    expect(formatNumber(-42)).toBe("-42");
    expect(formatNumber(Number.NaN)).toBe("0");
    expect(formatNumber(Number.POSITIVE_INFINITY)).toBe("0");
    expect(formatNumber(Number.NEGATIVE_INFINITY)).toBe("0");
  });
});

describe("summarizeDailyStats", () => {
  it("trả về các giá trị 0 khi mảng rỗng", () => {
    expect(summarizeDailyStats([])).toEqual({
      totalKm: 0,
      totalRuns: 0,
      maxKm: 0,
    });
  });

  it("tổng hợp chính xác tổng km, số buổi chạy và maxKm làm tròn 2 chữ số", () => {
    const stats: AdminDailyStat[] = [
      { date: "2026-05-01", activities_count: 2, distance_km: 0.1 },
      { date: "2026-05-02", activities_count: 3, distance_km: 0.2 },
      { date: "2026-05-03", activities_count: 1, distance_km: 12.345 },
    ];
    expect(summarizeDailyStats(stats)).toEqual({
      totalKm: 12.65,
      totalRuns: 6,
      maxKm: 12.35,
    });
  });

  it("bỏ qua giá trị NaN và Infinity trong từng bản ghi", () => {
    const stats: AdminDailyStat[] = [
      { date: "2026-05-01", activities_count: Number.NaN, distance_km: Number.POSITIVE_INFINITY },
      { date: "2026-05-02", activities_count: 4, distance_km: 8.5 },
    ];
    expect(summarizeDailyStats(stats)).toEqual({
      totalKm: 8.5,
      totalRuns: 4,
      maxKm: 8.5,
    });
  });

  it("property-based: bảo toàn bất biến totalKm >= maxKm >= 0 với dữ liệu không âm", () => {
    const rand = seededRandom(2026);
    for (let trial = 0; trial < 30; trial += 1) {
      const length = Math.floor(rand() * 15);
      const stats: AdminDailyStat[] = Array.from({ length }, (_, idx) => ({
        date: `2026-05-${String(idx + 1).padStart(2, "0")}`,
        activities_count: Math.floor(rand() * 10),
        distance_km: Math.round(rand() * 5000) / 100,
      }));
      const summary = summarizeDailyStats(stats);
      expect(summary.maxKm).toBeGreaterThanOrEqual(0);
      expect(summary.totalKm).toBeGreaterThanOrEqual(summary.maxKm);
      expect(summary.totalRuns).toBeGreaterThanOrEqual(0);
    }
  });
});
