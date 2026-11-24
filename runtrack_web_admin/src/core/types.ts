export type UserRole = "user" | "admin";

export type AdminTab = "dashboard" | "users" | "activities" | "clubs";

export interface AdminSession {
  token: string;
  email: string;
}

export interface AuthTokenResponse {
  access_token: string;
  token_type: string;
}

export interface MessageResponse {
  message: string;
}

export interface AdminDailyStat {
  date: string;
  activities_count: number;
  distance_km: number;
}

export interface AdminTopRunner {
  user_id: number;
  full_name: string;
  email: string;
  total_distance_km: number;
  activities_count: number;
}

export interface AdminStatsResponse {
  total_users: number;
  total_activities: number;
  total_distance_km: number;
  total_calories: number;
  total_clubs: number;
  daily_stats: AdminDailyStat[];
  top_runners: AdminTopRunner[];
}

export interface AdminUserItem {
  id: number;
  email: string;
  full_name: string;
  role: UserRole;
  height_cm: number | null;
  weight_kg: number | null;
  weekly_goal_km: number;
  activities_count: number;
  total_distance_km: number;
  created_at: string | null;
}

export interface AdminUserCreate {
  email: string;
  password: string;
  full_name: string;
  role?: UserRole;
  weekly_goal_km?: number;
}

export interface AdminUserUpdate {
  full_name?: string;
  role?: UserRole;
  height_cm?: number | null;
  weight_kg?: number | null;
  weekly_goal_km?: number;
}

export interface AdminActivityItem {
  id: string;
  user_id: number;
  user_email: string;
  user_full_name: string;
  title: string;
  distance_km: number;
  duration_seconds: number;
  avg_pace_seconds: number;
  calories_burned: number;
  start_time: string;
}

export interface AdminActivityCreate {
  user_id: number;
  title: string;
  distance_km: number;
  duration_seconds: number;
  avg_pace_seconds?: number | null;
  calories_burned?: number | null;
  start_time?: string | null;
}

export interface AdminActivityUpdate {
  title?: string;
  distance_km?: number;
  duration_seconds?: number;
  avg_pace_seconds?: number | null;
  calories_burned?: number | null;
}

export interface ClubResponse {
  club_id: number;
  name: string;
  description: string;
  member_count: number;
  is_joined: boolean;
}

export interface ClubCreate {
  name: string;
  description?: string;
}

export interface ClubUpdate {
  name?: string;
  description?: string;
}

export interface DailyStatsSummary {
  totalKm: number;
  totalRuns: number;
  maxKm: number;
}
