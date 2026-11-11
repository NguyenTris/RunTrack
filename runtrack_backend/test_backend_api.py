import datetime

def calculate_recovery(last_workout_hours_ago: float):
    hours_passed = last_workout_hours_ago
    recovery_hours_remaining = max(0.0, 24.0 - hours_passed)
    recovery_completed = recovery_hours_remaining <= 0
    recovery_percentage = min(100, int((hours_passed / 24.0) * 100))

    if recovery_percentage > 85:
        recovery_status_color = "green"
        recovery_status_label = "SẴN SÀNG"
        recovery_display_text = "Tối ưu (24h)"
    elif recovery_percentage > 60:
        recovery_status_color = "yellow"
        recovery_status_label = "TRUNG BÌNH"
        recovery_display_text = f"Còn {recovery_hours_remaining:.1f}h ({recovery_percentage}%)"
    else:
        recovery_status_color = "red"
        recovery_status_label = "CẦN NGHỈ"
        recovery_display_text = f"Còn {recovery_hours_remaining:.1f}h ({recovery_percentage}%)"

    return recovery_percentage, recovery_status_color, recovery_status_label, recovery_display_text

def calculate_us_aqi_pm25(pm25: float) -> int:
    breakpoints = [
        (0.0, 12.0, 0, 50),
        (12.1, 35.4, 51, 100),
        (35.5, 55.4, 101, 150),
        (55.5, 150.4, 151, 200),
        (150.5, 250.4, 201, 300),
        (250.5, 500.4, 301, 500)
    ]
    for c_low, c_high, i_low, i_high in breakpoints:
        if c_low <= pm25 <= c_high:
            return round(((i_high - i_low) / (c_high - c_low)) * (pm25 - c_low) + i_low)
    return 300 if pm25 > 500.4 else 0

def test_recovery_cases():
    # Case 1: 22.1 hours ago -> 92% -> Green SẴN SÀNG
    pct, color, label, text = calculate_recovery(22.1)
    assert pct == 92, f"Expected 92, got {pct}"
    assert color == "green", f"Expected green, got {color}"
    assert label == "SẴN SÀNG", f"Expected SẴN SÀNG, got {label}"
    assert text == "Tối ưu (24h)"

    # Case 2: 17 hours ago -> 70% -> Yellow TRUNG BÌNH
    pct, color, label, text = calculate_recovery(17.0)
    assert pct == 70, f"Expected 70, got {pct}"
    assert color == "yellow", f"Expected yellow, got {color}"
    assert label == "TRUNG BÌNH", f"Expected TRUNG BÌNH, got {label}"

    # Case 3: 10 hours ago -> 41% -> Red CẦN NGHỈ
    pct, color, label, text = calculate_recovery(10.0)
    assert pct == 41, f"Expected 41, got {pct}"
    assert color == "red", f"Expected red, got {color}"
    assert label == "CẦN NGHỈ", f"Expected CẦN NGHỈ, got {label}"

    print("Test Recovery cases PASS!")

def test_aqi():
    # PM2.5 = 7.8 ug/m3 -> AQI 32
    aqi = calculate_us_aqi_pm25(7.8)
    assert aqi == 33 or aqi == 32, f"Expected 32 or 33, got {aqi}"
    print(f"Test AQI PM2.5=7.8 -> AQI={aqi} PASS!")

if __name__ == "__main__":
    test_recovery_cases()
    test_aqi()
    print("All unit tests passed successfully!")
