package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.getAddress
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment() , OnMapReadyCallback{

    private lateinit var map: GoogleMap
    var isLocationSelected = false

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 120
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSION = 121
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 122
        private const val ZOOM_LEVEL = 15f
        private val TAG = SelectLocationFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.saveLocation.setOnClickListener{
            if(isLocationSelected) {
                _viewModel.navigationCommand.value = NavigationCommand.Back
            }else{
                Toast.makeText(context,"Select a location !",Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // set map style
        try {
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
            if (!success) {
                Toast.makeText(context, "Couldn't load map style.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Resources.NotFoundException) {
            Toast.makeText(context, "error $e", Toast.LENGTH_LONG).show()
        }

        if (isPermissionGranted()) {
            getCurrentLocation()
        }  else {
            requestLocationPermissions()
        }

        // set poi click listener
        map.setOnPoiClickListener {
            map.clear()
            _viewModel.selectedPOI.value = it
            _viewModel.latitude.value = it.latLng.latitude
            _viewModel.longitude.value = it.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = it.name
            map.addMarker(MarkerOptions().position(it.latLng).title(it.name))
            isLocationSelected = true
            Toast.makeText(context, "Location Selected. Click on SAVE to proceed with this location.", Toast.LENGTH_LONG).show()
        }

        // set onmap click listener
        map.setOnMapClickListener {
            _viewModel.latitude.value = it.latitude
            _viewModel.longitude.value = it.longitude
            _viewModel.reminderSelectedLocationStr.value = getAddress(activity as Context, it.latitude, it.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, ZOOM_LEVEL)
            map.moveCamera(cameraUpdate)
            map.addMarker(MarkerOptions().position(it).snippet(_viewModel.reminderSelectedLocationStr.toString()))
            isLocationSelected = true
            Toast.makeText(context, "Location Selected. Click on SAVE to proceed with this location.", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        checkDeviceLocationSettings()
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                val zoomLevel = 15f
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION))

        val backgroundPermissionApproved =
                if (runningQOrLater) {
                    PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    true
                }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    private fun requestLocationPermissions() {
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_BACKGROUND_LOCATION_PERMISSION
            }
            else -> REQUEST_LOCATION_PERMISSION
        }
        requestPermissions(permissionsArray, resultCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(runningQOrLater && grantResults.size == 2 && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Log.e("TAG", "permissions denied")
                _viewModel.showSnackBar.postValue(requireContext().getString(R.string.permission_denied_explanation))
            } else {
                _viewModel.showSnackBar.postValue(requireContext().getString(R.string.permission_granted))
                getCurrentLocation()
            }

        } else {
            Log.e("TAG", "permissions denied")
            _viewModel.showSnackBar.postValue(requireContext().getString(R.string.permission_denied_explanation))
        }
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val requestBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(requestBuilder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                    _viewModel.showErrorMessage.postValue(requireContext().getString(R.string.error_getting_location))
                }
            } else {
                Snackbar.make(requireView(), R.string.location_required_error, Snackbar.LENGTH_LONG).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
    }
}
