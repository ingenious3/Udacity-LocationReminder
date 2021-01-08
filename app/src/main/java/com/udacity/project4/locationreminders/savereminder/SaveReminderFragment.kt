package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    var pendingIntent: PendingIntent? = null

    companion object {
        const val GEOFENCE_RADIUS_IN_METERS = 100f
        const val GEOFENCE_EVENT = "GEOFENCE_EVENT"
        val TAG = SaveReminderFragment::class.java.simpleName
        val GEOFENCE_EXPIRATION_DURATION: Long = java.util.concurrent.TimeUnit.DAYS.toMillis(3)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            val geofenceId = UUID.randomUUID().toString()


            val reminder = ReminderDataItem(title, description, location, latitude, longitude, userEmail, geofenceId)
            if (_viewModel.validateEnteredData(reminder)) {
                addGeofence(latitude!!, longitude!!, reminder.id)
            }
            _viewModel.validateAndSaveReminder(reminder)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(latitude:Double, longitude:Double, reminderId:String) {
        val geofence: Geofence = getGeofence(LatLng(latitude, longitude), reminderId,
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest: GeofencingRequest = getGeofencingRequest(geofence)
        val pendingIntent: PendingIntent? = getGeofencePendingIntent()
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    Log.d(TAG, "Geofence added !")
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.d(TAG, "Error in adding geofence")
                })

    }


    fun getGeofencingRequest(geofence: Geofence?): GeofencingRequest {
        return GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build()
    }

    fun getGeofence(latLng: LatLng, id: String,  transitionTypes: Int): Geofence {
        return Geofence.Builder()
                .setRequestId(id)
                .setTransitionTypes(transitionTypes)
                .setExpirationDuration(GEOFENCE_EXPIRATION_DURATION)
                .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS_IN_METERS)
                .build()
    }

    fun getGeofencePendingIntent(): PendingIntent? {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = GEOFENCE_EVENT
        pendingIntent = PendingIntent.getBroadcast(requireContext(), 122, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }

}