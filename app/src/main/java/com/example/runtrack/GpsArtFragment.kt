package com.example.runtrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

class GpsArtFragment : Fragment(), OnMapReadyCallback {

    private lateinit var rvSuggestions: RecyclerView
    private var googleMap: GoogleMap? = null
    private var routes: List<GpsArtRouteDto> = emptyList()
    private var selectedRoute: GpsArtRouteDto? = null
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gps_art, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        attachGoogleMap()
        setupBottomNavigation(view)
        loadGpsArtRoutes()
    }

    private fun initViews(view: View) {
        rvSuggestions = view.findViewById(R.id.rv_art_suggestions)
        rvSuggestions.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        view.findViewById<TextView>(R.id.tv_btn_home)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        view.findViewById<ImageView>(R.id.btn_close_gps_art)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        view.findViewById<MaterialButton>(R.id.btn_start_run_art)?.setOnClickListener {
            startTrackingWithSelectedRoute()
        }
    }

    private fun attachGoogleMap() {
        val existing = childFragmentManager.findFragmentById(R.id.gps_art_map_container) as? SupportMapFragment
        val mapFragment = existing ?: SupportMapFragment.newInstance().also {
            childFragmentManager.beginTransaction()
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
        viewLifecycleOwner.lifecycleScope.launch {
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
            if (!isAdded) return@launch
            bindRoutes(loaded)
        }
    }

    private fun bindRoutes(loadedRoutes: List<GpsArtRouteDto>) {
        routes = loadedRoutes
        val suggestions = loadedRoutes.map { GpsArtMapHelper.toSuggestion(it) }
        rvSuggestions.adapter = ArtSuggestionsAdapter(suggestions) { item ->
            val route = routes.firstOrNull { it.id == item.id } ?: return@ArtSuggestionsAdapter
            selectRoute(route)
            Toast.makeText(requireContext(), "Đã chọn: ${route.name}", Toast.LENGTH_SHORT).show()
        }
        loadedRoutes.firstOrNull()?.let { selectRoute(it) }
    }

    private fun selectRoute(route: GpsArtRouteDto) {
        selectedRoute = route
        googleMap?.let { GpsArtMapHelper.drawRouteOnMap(it, route) }
    }

    private fun startTrackingWithSelectedRoute() {
        val intent = Intent(requireContext(), TrackingActivity::class.java)
        selectedRoute?.let { route ->
            intent.putExtra(TrackingActivity.EXTRA_ROUTE_NAME, route.name)
            intent.putExtra(TrackingActivity.EXTRA_ROUTE_POINTS_JSON, gson.toJson(route.points))
        }
        startActivity(intent)
    }

    private fun setupBottomNavigation(view: View) {
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.nav_tracking
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_activity -> {
                    startActivity(Intent(requireContext(), DashboardActivity::class.java))
                    true
                }
                R.id.nav_tracking -> true
                R.id.nav_leaderboard -> {
                    startActivity(Intent(requireContext(), LeaderboardActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
