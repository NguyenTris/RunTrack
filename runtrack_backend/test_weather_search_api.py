import httpx


def _auth_headers(client, email, full_name):
    client.post(
        "/auth/register",
        json={"email": email, "password": "Password123", "full_name": full_name},
    )
    token = client.post(
        "/auth/login",
        json={"email": email, "password": "Password123"},
    ).json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def test_search_friends_from_db_excludes_self(client):
    me_headers = _auth_headers(client, "me@runtrack.vn", "Người Dùng Hiện Tại")
    _auth_headers(client, "ngocmai@runtrack.vn", "Ngọc Mai")
    _auth_headers(client, "hoangnam@runtrack.vn", "Hoàng Nam")

    all_friends = client.get("/search/friends", headers=me_headers)
    assert all_friends.status_code == 200
    names = [item["full_name"] for item in all_friends.json()]
    assert "Người Dùng Hiện Tại" not in names
    assert names == ["Ngọc Mai", "Hoàng Nam"]

    filtered = client.get("/search/friends?q=mai", headers=me_headers)
    assert filtered.status_code == 200
    assert len(filtered.json()) == 1
    assert filtered.json()[0]["full_name"] == "Ngọc Mai"


def test_search_clubs_filter_and_pagination(client):
    me_headers = _auth_headers(client, "club@runtrack.vn", "Club Runner")

    res = client.get("/search/clubs?q=UIT", headers=me_headers)
    assert res.status_code == 200
    assert len(res.json()) == 1
    assert res.json()[0]["name"] == "UIT Runners"

    paged = client.get("/search/clubs?page=2&limit=2", headers=me_headers)
    assert paged.status_code == 200
    assert len(paged.json()) == 1


def test_weather_returns_all_7_fields_and_handles_errors(client, monkeypatch):
    class FakeResponse:
        def __init__(self, status_code, payload):
            self.status_code = status_code
            self._payload = payload

        def json(self):
            return self._payload

    class FakeAsyncClient:
        def __init__(self, *args, **kwargs):
            pass

        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):
            return False

        async def get(self, url):
            if "air_pollution" in url:
                return FakeResponse(200, {"list": [{"components": {"pm2_5": 7.8}}]})
            return FakeResponse(
                200,
                {
                    "name": "Biên Hòa",
                    "main": {"temp": 29.4, "humidity": 75},
                    "weather": [{"description": "mưa nhẹ", "main": "Rain", "icon": "10n"}],
                },
            )

    monkeypatch.setenv("OPENWEATHER_API_KEY", "dummy_test_key")
    monkeypatch.setattr(httpx, "AsyncClient", FakeAsyncClient)
    res = client.get("/weather?lat=10.87&lon=106.80")
    assert res.status_code == 200
    data = res.json()
    assert data == {
        "city": "Biên Hòa",
        "temperature": 29.4,
        "humidity": 75,
        "condition": "mưa nhẹ",
        "weather_main": "Rain",
        "icon": "10n",
        "aqi": 32,
    }


def test_weather_returns_502_when_upstream_fails(client, monkeypatch):
    class FailingAsyncClient:
        def __init__(self, *args, **kwargs):
            pass

        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):
            return False

        async def get(self, url):
            raise httpx.ConnectTimeout("timeout")

    monkeypatch.setenv("OPENWEATHER_API_KEY", "dummy_test_key")
    monkeypatch.setattr(httpx, "AsyncClient", FailingAsyncClient)
    res = client.get("/weather?lat=10.87&lon=106.80")
    assert res.status_code == 502


def test_weather_returns_503_when_api_key_missing(client, monkeypatch):
    monkeypatch.delenv("OPENWEATHER_API_KEY", raising=False)
    res = client.get("/weather?lat=10.87&lon=106.80")
    assert res.status_code == 503
