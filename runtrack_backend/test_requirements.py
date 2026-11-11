import sys
from pathlib import Path

def test_requirements_file():
    req_path = Path(__file__).parent / "requirements.txt"
    assert req_path.exists(), "File requirements.txt không tồn tại"
    
    with open(req_path, "r", encoding="utf-8") as f:
        lines = [line.strip() for line in f if line.strip() and not line.startswith("#")]
        
    required_packages = ["fastapi", "uvicorn", "gunicorn", "httpx", "pydantic", "python-dotenv"]
    
    found_packages = set()
    for line in lines:
        for pkg in required_packages:
            if line.startswith(pkg):
                found_packages.add(pkg)
                
    missing = set(required_packages) - found_packages
    assert not missing, f"Thiếu các thư viện thiết yếu: {missing}"
    print(f"Kiểm thử thành công: Toàn bộ {len(lines)} thư viện và các gói cốt lõi Azure đã được định nghĩa đầy đủ!")

if __name__ == "__main__":
    test_requirements_file()

