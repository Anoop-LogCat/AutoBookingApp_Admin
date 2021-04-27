package logcat.ayeautoapps.autoadmin

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.collection.arrayMapOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.functions.FirebaseFunctions
import logcat.ayeautoapps.autoadmin.adapter.AddMemberAdapter

class AddAutoStand : Fragment() {

    private var adapter:AddMemberAdapter?=null

    private lateinit var autoStandName:EditText
    private lateinit var nomineeNumber: EditText
    private lateinit var landMark:TextView

    private val REQUEST_CHECK_SETTINGS = 214

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_auto_stand, container, false)
    }

    override fun onStart() {
        super.onStart()
        if(addMemberList.isNotEmpty()){ addMemberList.clear()}
    }

    override fun onResume() {
        super.onResume()
        if(landMark.text.toString() != landMarkData){landMark.text= landMarkData}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoStandName = view.findViewById(R.id.standName)
        nomineeNumber = view.findViewById(R.id.nomineeNumber)
        landMark = view.findViewById(R.id.landMark)
        val memberRecyclerView: RecyclerView = view.findViewById(R.id.addMemberRecyclerView)
        view.findViewById<TextView>(R.id.saveStandDetails).setOnClickListener {
            if (standPosition==null || nomineeNumber.text.isNullOrBlank()  || autoStandName.text.isNullOrBlank() || landMark.text.toString().compareTo("no land mark")==0|| cityName.compareTo("null")==0) {
                Toast.makeText(context, "data format is incorrect", Toast.LENGTH_SHORT).show()
            }
            else if(!memberDataChecker()){
                Toast.makeText(context, "member phone numbers are incorrect", Toast.LENGTH_LONG).show()
            }
            else{
               when(nomineeNumber.text.toString().length){
                   10->saveData()
                   else ->Toast.makeText(context, "invalid phone number for nominee", Toast.LENGTH_LONG).show()
               }
            }
        }

        landMark.setOnClickListener {
            locationProvided=true
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 158)
                }
                else{
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            } else {
                val builder = LocationSettingsRequest.Builder().addLocationRequest(LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
                builder.addLocationRequest(LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
                builder.setAlwaysShow(true)
                val mSettingsClient:SettingsClient=LocationServices.getSettingsClient(requireActivity())
                val mLocationSettingsRequest:LocationSettingsRequest=builder.build()
                val fusedLocationClient: FusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(requireActivity())
                mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener {
                    val reqSetting = LocationRequest.create().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 10000
                        fastestInterval =10000
                        smallestDisplacement = 1.0f
                    }
                    val locationUpdates = object : LocationCallback() {
                        override fun onLocationResult(lr: LocationResult) {
                            if(locationProvided){
                                val intentData = Intent(requireActivity(), LocationSelector::class.java)
                                intentData.putExtra("latitude", (lr.lastLocation.latitude).toString())
                                intentData.putExtra("longitude", (lr.lastLocation.longitude).toString())
                                startActivity(intentData)
                            }
                            locationProvided=false
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(reqSetting, locationUpdates, null)
                }.addOnFailureListener {e->
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            val rae: ResolvableApiException = e as ResolvableApiException
                            rae.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                        }
                    }
                }
            }
        }

        view.findViewById<TextView>(R.id.addMember).setOnClickListener {
            when {
                addMemberList.isEmpty() -> {
                    initialMemberData(1)
                    adapter = AddMemberAdapter(addMemberList)
                    memberRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    memberRecyclerView.adapter = adapter
                }
                addMemberList.size>=30 -> {
                    Toast.makeText(context,"driver limit reached",Toast.LENGTH_SHORT).show()
                }
                else -> {
                    initialMemberData(addMemberList.size+1)
                    adapter = AddMemberAdapter(addMemberList)
                    memberRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    memberRecyclerView.adapter = adapter
                }
            }
        }

        view.findViewById<TextView>(R.id.removeMember).setOnClickListener {
            if(addMemberList.isNotEmpty()){
                addMemberList.removeAt(addMemberList.size-1)
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun saveData(){
        val progressDialog= ProgressDialog(requireActivity())
        progressDialog.setMessage("Uploading Stand Details")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val members= arrayMapOf<String,Long>()
        addMemberList.forEach {
            members[(addMemberList.indexOf(it)).toString()] = it.phone.toLong()
        }
        val saveStandDataMap = mapOf<String,Any>(
            "latitude" to standPosition!!.latitude,
            "longitude" to standPosition!!.longitude,
            "standName" to autoStandName.text.toString(),
            "standRep" to nomineeNumber.text.toString().toLong(),
            "landMark" to landMark.text.toString(),
            "testMode" to true,
            "city" to cityName,
            "members" to members
            )
        FirebaseFunctions.getInstance().getHttpsCallable("SaveAutoStand").call(saveStandDataMap).addOnCompleteListener{
            progressDialog.hide()
            if(it.isSuccessful){
                Toast.makeText(context,"Data Uploaded",Toast.LENGTH_SHORT).show()
                addMemberList.clear()
                nomineeNumber.setText("")
                autoStandName.setText("")
                landMark.text=resources.getString(R.string.no_land_mark)
                landMarkData="no land mark"
            }
            else{
                Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun memberDataChecker():Boolean{
        var countChecker=0
        addMemberList.forEach{addMember->
             if(addMember.phone.length==10){countChecker++}
        }
        return when {
            countChecker==addMemberList.size->true
            addMemberList.size==0->false
            else->false
        }
    }

    private fun initialMemberData(i:Int){
        addMemberList.add(AddMemberModel("Driver - $i",""))
    }
}