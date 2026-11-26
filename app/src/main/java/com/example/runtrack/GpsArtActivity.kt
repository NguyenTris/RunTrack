package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.network.ApiClient
import com.example.runtrack.network.model.community.GpsArtRouteDto
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GpsArtActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var rvSuggestions: RecyclerView
    private var googleMap: GoogleMap? = null
    private var routes: List<GpsArtRouteDto> = emptyList()
    private var selectedRoute: GpsArtRouteDto? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_gps_art)

        val root = findViewById<android.view.View>(R.id.root_layout_gps_art)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        attachGoogleMap()
        setupBottomNavigation()
        loadGpsArtRoutes()
    }

    private fun initViews() {
        rvSuggestions = findViewById(R.id.rv_art_suggestions)
        rvSuggestions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        findViewById<TextView>(R.id.tv_btn_home)?.setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btn_close_gps_art)?.setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btn_start_run_art)?.setOnClickListener {
            startTrackingWithSelectedRoute()
        }
    }

    private fun attachGoogleMap() {
        val existing = supportFragmentManager.findFragmentById(R.id.gps_art_map_container) as? SupportMapFragment
        val mapFragment = existing ?: SupportMapFragment.newInstance().also {
            supportFragmentManager.beginTransaction()
                .replace(R.id.gps_art_map_container, it)
                .commit()
        }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = false
        selectedRoute?.let { GpsArtMapHelper.drawRouteOnMap(map, it) }
    }

    private fun loadGpsArtRoutes() {
        lifecycleScope.launch {
            val loaded = try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.instance.getGpsArtRoutes()
                }
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    response.body()!!
                } else {
                    GpsArtMapHelper.fallbackRoutes()
                }
            } catch (_: Exception) {
                GpsArtMapHelper.fallbackRoutes()
            }
            bindRoutes(loaded)
        }
    }

    private fun bindRoutes(loadedRoutes: List<GpsArtRouteDto>) {
        routes = loadedRoutes
        val suggestions = loadedRoutes.map { GpsArtMapHelper.toSuggestion(it) }
        rvSuggestions.adapter = ArtSuggestionsAdapter(suggestions) { item ->
            val route = routes.firstOrNull { it.id == item.id } ?: return@ArtSuggestionsAdapter
            selectRoute(route)
            Toast.makeText(this, "Đã chọn: ${route.name}", Toast.LENGTH_SHORT).show()
        }
        loadedRoutes.firstOrNull()?.let { selectRoute(it) }
    }

    private fun selectRoute(route: GpsArtRouteDto) {
        selectedRoute = route
        googleMap?.let { GpsArtMapHelper.drawRouteOnMap(it, route) }
    }

    private fun startTrackingWithSelectedRoute() {
        val intent = Intent(this, TrackingActivity::class.java)
        selectedRoute?.let { route ->
            intent.putExtra(TrackingActivity.EXTRA_ROUTE_NAME, route.name)
            intent.putExtra(TrackingActivity.EXTRA_ROUTE_POINTS_JSON, gson.toJson(route.points))
        }
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.nav_tracking
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_activity -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tracking -> true
                R.id.nav_leaderboard -> {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
