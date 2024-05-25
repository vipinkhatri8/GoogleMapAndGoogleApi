package com.maths.googlemapandgoogleapi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.maths.googlemapandgoogleapi.databinding.ActivityLocationMapBinding

class LocationMapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityLocationMapBinding

    private var gMap: GoogleMap? = null


    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    binding.etSearch?.text = place.name
                    Log.i(
                        ContentValues.TAG, "Place: ${place.name}, ${place.id}"
                    )
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(ContentValues.TAG, "User canceled autocomplete")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationMapBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val apiKey = getString(R.string.api_key)

        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }

        (supportFragmentManager.findFragmentById(R.id.fm_mapid) as SupportMapFragment?)?.getMapAsync(
            this
        )

        binding.etSearch?.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this)
            startAutocomplete.launch(intent)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        googleMap.setOnMapClickListener {
         //   enterAddressPopup()
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            showCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Get current location coordinates
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    // Add a marker at the current location
                    gMap?.addMarker(MarkerOptions().position(currentLatLng).title("My Location"))
                    Log.e("Location Is ", "Success")
                    // Move the camera to the current location
                    gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                } else {
                    Log.e("Location Is ", "Null")
                }
            }
    }






    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}