package logcat.ayeautoapps.autoadmin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import java.util.*

class LocationSelector : AppCompatActivity(),OnMapReadyCallback,MapboxMap.OnMapClickListener {

    private val code = 100

    private lateinit var permissionManager: PermissionsManager
    private lateinit var map: MapboxMap
    private lateinit var villageTextView: TextView
    private lateinit var landMarkTextView:EditText
    private lateinit var mapView: MapView

    private var destinationMarker: Marker?=null
    private var settingsClient: SettingsClient? = null
    private var position:LatLng?=null
    private var intentData:Intent?=null

    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        map.addOnMapClickListener(this)
        intentData=intent
        position = LatLng(intentData!!.getStringExtra("latitude")!!.toDouble(),intentData!!.getStringExtra("longitude")!!.toDouble())
        getCompleteAddressString()
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position!!, 15.0))
        destinationMarker = map.addMarker(MarkerOptions().position(position))
    }

    @SuppressLint("SetTextI18n")
    private fun getCompleteAddressString(){
        val geoCoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geoCoder.getFromLocation(position!!.latitude, position!!.longitude, 1)
            if (addresses != null) {
                val zip = addresses[0].postalCode
                val road = addresses[0].thoroughfare
                val panchayath = addresses[0].locality
                val gilla =addresses[0].subAdminArea
                val state = addresses[0].adminArea
                cityName=gilla
                villageTextView.text=panchayath
                landMarkTextView.setText("$zip $road $panchayath $gilla $state")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, resources.getString(R.string.MAP_BOX_API_KEY))
        setContentView(R.layout.activity_location_selector)
        villageTextView = findViewById(R.id.panchayat)
        mapView = findViewById(R.id.mapboxMap)
        landMarkTextView = findViewById(R.id.subLandMark)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        settingsClient = LocationServices.getSettingsClient(this)
        findViewById<Button>(R.id.loc_confirm).setOnClickListener {
            if(landMarkTextView.text.toString().isNotEmpty()|| cityName.compareTo("null")!=0){
                landMarkData=landMarkTextView.text.toString()
                standPosition=position
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        map = mapboxMap ?: return
        mapboxMap.addImage(resources.getString(R.string.ICON_ID), BitmapFactory.decodeResource(this.resources, R.drawable.mapbox_marker_icon_default))
        enableLocation()
    }
    override fun onMapClick(point: LatLng) {
        if(destinationMarker!=null){
            map.removeMarker(destinationMarker!!)
        }
        position= LatLng(point.latitude,point.longitude)
        getCompleteAddressString()
        destinationMarker = map.addMarker(MarkerOptions().position(point))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == code) {
                    enableLocation()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}