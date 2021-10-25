package com.mobileapplike.teamtrack.ui.fragments.map

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mobileapplike.teamtrack.Person
import com.mobileapplike.teamtrack.R
import com.mobileapplike.teamtrack.databinding.MapFragmentBinding
import com.mobileapplike.teamtrack.service.TrackingService
import com.mobileapplike.teamtrack.ui.MainActivity
import com.mobileapplike.teamtrack.ui.fragments.BaseFragment
import com.mobileapplike.teamtrack.ui.fragments.login.hideKeyboard
import com.mobileapplike.teamtrack.utils.Constant.ACTION_START_OR_RESUME_SERVICE
import com.mobileapplike.teamtrack.utils.Constant.ACTION_STOP_SERVICE
import com.mobileapplike.teamtrack.utils.Constant.REQUEST_CODE_LOCATION_PERMISSION
import com.mobileapplike.teamtrack.utils.FirebaseDB
import com.mobileapplike.teamtrack.utils.FirebaseDB.getToken
import com.mobileapplike.teamtrack.utils.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.map_fragment.*
import kotlinx.android.synthetic.main.my_group_foremans_fragment.view.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


var mapView: MapView? = null

@AndroidEntryPoint
class MapFragment : BaseFragment() , EasyPermissions.PermissionCallbacks, NavigationView.OnNavigationItemSelectedListener{

    companion object {
        fun newInstance() = MapFragment()
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.my_team -> {
               FirebaseDB.person.let {
                    if (it.masterId.isEmpty()) {
                        findNavController().navigate(
                            MapFragmentDirections.actionMapFragmentToFollowersFragment()
                        )
                    } else {
                        findNavController().navigate(
                            MapFragmentDirections.actionMapFragmentToMyGroupForemansFragment()
                        )
                    }
                }
            }
            R.id.join_the_master -> {

                    viewModel.checkMasterForJoining({
                        viewModel.checkFollowers({
                            findNavController().navigate(MapFragmentDirections.actionMapFragmentToJoinTheMaster())
                        }, {question, questionLeave->
                            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                            builder.setMessage(question)
                                    .setPositiveButton("Yes", dialogJoinClickListener)
                                    .setNegativeButton("No", dialogJoinClickListener)
                                    .show()
                        })
                    }, {question, nickName ->
                        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                        builder.setMessage(question)
                                .setPositiveButton("Yes", dialogJoinLeaveClickListener)
                                .setNegativeButton("No", dialogJoinLeaveClickListener)
                                .show()
                    })

            }
            R.id.leave_the_foreman -> {
               viewModel.checkMasterForJoining({
                     //viewModel.message.postValue("You haven't joined a foreman's group yet")
                },
                        {question, nickName->
                            val questionNew =  "Do you want to leave foreman's group " + nickName + " " +
                                    FirebaseDB.person.masterId + "?"
                            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                            builder.setMessage(questionNew)
                                    .setPositiveButton("Yes", dialogJoinLeaveClickListener)
                                    .setNegativeButton("No", dialogJoinLeaveClickListener)
                                    .show()
                        })
            }
            R.id.setNickName -> {
                viewModel.retrievePersons() {

                    it?.let {
                        findNavController().navigate(
                            MapFragmentDirections.actionMapFragmentToLoginFragment(
                                it.token,
                                it.nickName
                            )
                        )
                    }
                    if (it == null) {
                        findNavController().navigate(MapFragmentDirections.actionMapFragmentToLoginFragment())
                    }

                }
            }
            R.id.share_my_grpup -> {
                viewModel.checkMaster({
                    findNavController().navigate(MapFragmentDirections.actionMapFragmentToShareGroupIDFragment())},
                        {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                            builder.setMessage(it)
                                    .setPositiveButton("Yes", dialogShareClickListener)
                                    .setNegativeButton("No", dialogShareClickListener)
                                    .show()
                        })
            }
            R.id.close_my_own_group ->{
                viewModel.checkFollowers({}, {question, questionLeave->
                    val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    builder.setMessage(questionLeave)
                            .setPositiveButton("Yes", dialogJoinClickListener)
                            .setNegativeButton("No", dialogJoinClickListener)
                            .show()
                })
            }
            R.id.signOut -> {
                activity?.finish()
                sendCommandToService(ACTION_STOP_SERVICE)
            }

        }

        (activity as MainActivity).drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }



    var mainHandler: Handler =  Handler(Looper.getMainLooper())
    var myRunnable: Runnable = object : Runnable {
        override fun run() {

            lifecycleScope.launchWhenResumed {
                showPersons()
            }
            mainHandler.postDelayed(this, 2000)
        }
    }

    private var isStarted: Boolean = false
    private var firstTime: Boolean = true
    private lateinit var viewModel: MapViewModel
    private lateinit var mapboxMap: MapboxMap
    private  val ONE_SECOND = 1000




    var first = true
    private lateinit var lastPosition : com.mapbox.geojson.Point
    private  var coordinate: ArrayList<Point> = ArrayList<Point> ()
    private var futures: ArrayList<Feature> = ArrayList()
    internal var sourceId = "marker-source"
    private val layerId: String = "marker-layer"
    private lateinit var navView: NavigationView
    var personArrayList: kotlin.collections.ArrayList<Person> = arrayListOf()

    //private lateinit var locationPermissionHelper: LocationPermissionHelper
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        // Jump to the current indicator position
        //mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(it).build())
        if (first) {
            mapView?.getMapboxMap()?.setCamera(
                CameraOptions.Builder()
                    .center(it)
                    .zoom(16.0)
                    .build()
            )
            first = false
        }

        lastPosition = it

        // Set the gestures plugin's focal point to the current indicator location.
        //mapView?.gestures?.focalPoint = mapView?.getMapboxMap()?.pixelForCoordinate(it)

    }

    private fun updateMarkers() {

      futures.clear()

      Log.d("personArrayList", personArrayList.toString())

      personArrayList.forEach { point ->
                val bitmap = createStoreMarker(point.id, point.nickName)

                    mapboxMap.getStyle()?.removeStyleImage(point.id)
                    mapboxMap.getStyle()?.addImage(point.id, bitmap)

                val feature: Feature = Feature.fromGeometry(
                    Point.fromLngLat(
                        point.longitude.toDouble(),
                        point.latitude.toDouble()
                    )
                )
                feature.addStringProperty("id", point.id)
                futures.add(feature)
        }




        GeoJsonSource.Builder(sourceId){

            val symbolLayer = SymbolLayer(layerId, sourceId)
                    .iconImage("{id}")
                    .iconOffset(listOf(20.0, -18.0))
                    .iconAllowOverlap(true)

            mapboxMap.getStyle()?.removeStyleLayer(layerId)
            mapboxMap.getStyle()?.removeStyleSource(sourceId)


            mapboxMap.getStyle()?.addLayer(symbolLayer)
            mapboxMap.getStyle()?.addSource(it)

        }
            .featureCollection(FeatureCollection.fromFeatures(futures))
            .build()


  }


    private fun createStoreMarker(uid: String, price: String): Bitmap {
        val markerLayout = layoutInflater.inflate(R.layout.marker_observables, null)


        val textView = markerLayout.findViewById(R.id.textView) as TextView
        textView.text = price

        markerLayout.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight())

        val bitmap = Bitmap.createBitmap(
            markerLayout.getMeasuredWidth(),
            markerLayout.getMeasuredHeight(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        markerLayout.draw(canvas)
        return bitmap
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        val binding = MapFragmentBinding.bind(view)

        mapboxMap = binding.mapView.getMapboxMap()

        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS,
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {

                    requestPermissions() {
                        addListeners()
                    }
                    mapView?.scalebar?.enabled = false

                }
            })





        viewModel.retrievePersons() {
            if (it == null) {
                findNavController().navigate(MapFragmentDirections.actionMapFragmentToLoginFragment())
            } else {
                //viewModel.subscribeToRealtimeUpdates()
            }
          }

        viewModel.message.observe(viewLifecycleOwner, {
            hideKeyboard()
            Snackbar.make(view, it, ONE_SECOND).show()
        })

        viewModel.person.observe(viewLifecycleOwner, { person ->
            val headerView = navView.getHeaderView(0);
            headerView.headerName.setText(person.nickName)
            headerView.headerPhone.setText(person.id)
            //headerView.header_masterId.setText(person.masterId)
            person.masterId.let {
                if (it.isEmpty()) {
                    //(activity as MainActivity).supportActionBar?.title = ""
                    textForemnId.text = ""
                } else {
                    // (activity as MainActivity).supportActionBar?.title = "Foreman ID: " + person.masterId
                    textForemnId.text = "Foreman ID: " + person.masterId
                }
            }




        })

        binding.navigationFab.setOnClickListener {
            if (isStarted){
                if (personArrayList.size > 0) {
                    updateMapCameraBound(ONE_SECOND, doubleArrayOf(50.0, 150.0, 150.0, 50.0))
                } else {
                    lastPosition.let {
                        updateMapCamera()
                    }
                }
            }
        }

        binding.locationFab.setOnClickListener {
            lastPosition.let {
                updateMapCamera()
            }
        }


        binding.shareFab.setOnClickListener {

            viewModel.checkMaster({
                findNavController().navigate(MapFragmentDirections.actionMapFragmentToShareGroupIDFragment())},
                {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    builder.setMessage(it)
                        .setPositiveButton("Yes", dialogShareClickListener)
                        .setNegativeButton("No", dialogShareClickListener)
                        .show()
            })

       }

        binding.joinFab.setOnClickListener {
            viewModel.checkMasterForJoining({
               viewModel.checkFollowers({
                    findNavController().navigate(MapFragmentDirections.actionMapFragmentToJoinTheMaster())
                }, {question, questionLeave->
                    val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    builder.setMessage(question)
                        .setPositiveButton("Yes", dialogJoinClickListener)
                        .setNegativeButton("No", dialogJoinClickListener)
                        .show()
                })
            }, {question, nickName ->


                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                builder.setMessage(question)
                    .setPositiveButton("Yes", dialogJoinLeaveClickListener)
                    .setNegativeButton("No", dialogJoinLeaveClickListener)
                    .show()
            })
        }

        getToken()


    }
    var dialogShareClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    viewModel.setMyPersonMasterId(""){
                        findNavController().navigate(MapFragmentDirections.actionMapFragmentToShareGroupIDFragment())
                    }
                }
            }
        }

    var dialogJoinClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                   viewModel.closeMyGroup{
                       findNavController().navigate(MapFragmentDirections.actionMapFragmentToJoinTheMaster())
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }

    var dialogJoinLeaveClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    viewModel.setMyPersonMasterId(""){
                        findNavController().navigate(MapFragmentDirections.actionMapFragmentToJoinTheMaster())
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }

    var dialogLeaveClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    viewModel.setMyPersonMasterId(""){
                        view?.let { Snackbar.make(it, "You leaved master's group", ONE_SECOND).show() }
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }


    fun showPersons(){
        Log.d("showPersons", "showPersons")
        FirebaseDB.getPerson({
            viewModel.message.postValue(it)
        }, {
            it?.let {
                viewModel.person.postValue(it)
                FirebaseDB.getPersonsGroup() {
                    it?.let {
                        personArrayList = it
                        updateMarkers()
                    }
                }
            }
        })

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navView = (activity as MainActivity).nav_view;
        navView.setNavigationItemSelectedListener(this)
    }

    fun updateMapCamera() {

      mapboxMap.flyTo(
          cameraOptions {
              center(lastPosition)
              zoom(16.0)
              bearing(0.0)

          },
          mapAnimationOptions {
              duration(1000)
          })

    }



    private fun updateMapCameraBound(animationTime: Int, padding: DoubleArray) {


        var pointArrayList: List<Point> = arrayListOf()

        personArrayList.filterNot { person->
            person.longitude.isNullOrEmpty() || person.latitude.isNullOrEmpty()
        }
                .map {
            Point.fromLngLat(it.longitude.toDouble(), it.latitude.toDouble())
        }.also { pointArrayList = it }

        val option = mapboxMap.cameraForCoordinates(pointArrayList)

        mapboxMap.flyTo(option,
            mapAnimationOptions {
                duration(1000)
            })



        option.zoom?.let {

            var zoom = option.zoom
            zoom = zoom?.minus(1)

            mapboxMap.flyTo(
                cameraOptions {
                    //zoom(option.zoom!! - 1 )
                    zoom(zoom)
                    //padding(edgeInsets)
                    bearing(0.0)
                },
                mapAnimationOptions {
                    duration(1000)
                })
        }





    }

    fun addListeners(){

// Disable scroll gesture, since we are updating the camera position based on the indicator location.
            mapView?.gestures?.scrollEnabled = true

            mapView?.gestures?.addOnMapClickListener { point ->
                mapView?.location?.isLocatedAt(point) { isPuckLocatedAtPoint ->
                    if (isPuckLocatedAtPoint) {
                        Toast.makeText(
                            requireActivity(),
                            "Clicked on location puck",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                true
            }
            mapView?.gestures?.addOnMapLongClickListener { point ->
                mapView?.location?.isLocatedAt(point) { isPuckLocatedAtPoint ->
                    if (isPuckLocatedAtPoint) {
                        Toast.makeText(
                            requireActivity(),
                            "Long-clicked on location puck",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                true
            }

        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        isStarted = true


    }

    private fun sendCommandToService(action: String) =
            Intent(requireContext(), TrackingService::class.java).also {
                it.action = action
                requireContext().startService(it)
            }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        mapView?.location?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)

        mainHandler.removeCallbacks(myRunnable);
        mainHandler.post(myRunnable)
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        mapView?.location?.removeOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        mainHandler.removeCallbacks(myRunnable);
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        mainHandler.removeCallbacks(myRunnable);
    }

    private fun requestPermissions(onMapReady: () -> Unit) {


        if(TrackingUtility.hasLocationPermissions(requireContext())) {
            onMapReady()
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                //Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions(){
                addListeners()
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        addListeners()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}
