package logcat.ayeautoapps.autoadmin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Point
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.google.zxing.WriterException
import com.wang.avi.AVLoadingIndicatorView
import logcat.ayeautoapps.autoadmin.adapter.DriverAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AllDriver : Fragment() {

    private val storageReference: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://auto-pickup-apps.appspot.com")

    private lateinit var totalStandRevenue:TextView
    private lateinit var currentDateTextView:TextView
    private lateinit var driverRecyclerView:RecyclerView
    private lateinit var noDataLayoutInAllDriver: LinearLayout
    private lateinit var progressLayout: FrameLayout
    private lateinit var progressBar: AVLoadingIndicatorView

    private var driverList = ArrayList<DriverModel>()
    private val argsInAllDriver:AllDriverArgs by navArgs()
    private var navController: NavController?=null

    private var standCity:String="no city name"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_driver, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        driverRecyclerView = view.findViewById(R.id.allDriverRecycler)
        noDataLayoutInAllDriver=view.findViewById(R.id.noDataLayoutInAllDriver)
        progressBar=view.findViewById(R.id.progressInAllDrivers)
        totalStandRevenue=view.findViewById(R.id.totalStandRevenue)
        progressLayout=view.findViewById(R.id.progressLayout)
        currentDateTextView=view.findViewById(R.id.today_date)
        navController = Navigation.findNavController(view)
        if(allDriverJSONString==null||globalStandName==null||globalStandName!!.compareTo(argsInAllDriver.standNameArg)!=0){
            GetStandDriverInfo().execute(argsInAllDriver.standNameArg)
        }
        else{
            setData(allDriverJSONString!!)
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class GetStandDriverInfo : AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.show()
            progressLayout.visibility=View.VISIBLE
            driverRecyclerView.visibility=View.INVISIBLE
            noDataLayoutInAllDriver.visibility=View.INVISIBLE
            if(driverList.isNotEmpty()){driverList.clear()}
            standCity = if(globalCityName!!.compareTo("Trivandrum")==0){
                "Thiruvananthapuram"
            } else{
                globalCityName!!
            }
        }
        override fun doInBackground(vararg params: String?): String {
            return try {
                val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/ViewAutoStandDrivers/${params[0]!!}"
                val url2 = "https://us-central1-auto-pickup-apps.cloudfunctions.net/RevenueCalculation/${params[0]!!}"
                val request: Request = Request.Builder().url(url).build()
                val request2: Request = Request.Builder().url(url2).build()
                val client = OkHttpClient()
                val response= client.newCall(request).execute()
                val response2= client.newCall(request2).execute()
                response.body()?.string().toString()+"|"+response2.body()?.string().toString()
            }catch (e: Exception){
                "ERROR"
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            globalStandName=argsInAllDriver.standNameArg
            allDriverJSONString=result
            setData(result!!)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData(result: String){
        progressBar.hide()
        progressLayout.visibility=View.GONE
        if(driverList.isNotEmpty()){driverList.clear()}
        when {
            result.compareTo("ERROR")==0 -> {
                noDataLayoutInAllDriver.visibility=View.INVISIBLE
                allDriverJSONString=null
                globalStandName=null
                Toast.makeText(context, "no network", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val allDriverDataJSON:String = result.split('|').first()
                when{
                    allDriverDataJSON.compareTo("no data")==0 -> {
                        noDataLayoutInAllDriver.visibility=View.VISIBLE
                    }
                    else->{
                        noDataLayoutInAllDriver.visibility=View.INVISIBLE
                        driverRecyclerView.visibility=View.VISIBLE
                        val standRevenue:String = result.split('|').last()
                        val allDriverDoc: java.util.HashMap<*, *>? = Gson().fromJson(allDriverDataJSON, HashMap::class.java)
                        allDriverDoc?.keys?.forEach{uid->
                            val eachDriverData=Gson().fromJson(Gson().toJson(allDriverDoc[uid.toString()]), FireDriverModel::class.java)
                            driverList.add(DriverModel(
                                requireActivity(),
                                allDriverDoc.keys.indexOf(uid),
                                uid.toString(), eachDriverData,
                                when (eachDriverData.phone.compareTo((eachDriverData.standRep.toLong()).toString()) == 0) {
                                    true -> true
                                    else -> false
                                },
                                generateQRCode = {driverUserID,position ->
                                    qRCodeDataFieldDialogBox(driverUserID,position)
                                },
                                nextPage = { driverObject, driverUid ->
                                    val action = AllDriverDirections.actionAllDriverToEachDriver()
                                    action.driverUid = driverUid
                                    action.demoStandLocation = driverObject.standLandMark
                                    action.driverStandName = driverObject.standName
                                    action.driverName = driverObject.username
                                    action.driverImage = driverObject.imageUrl
                                    action.driverPhone = driverObject.phone
                                    navController?.navigate(action)
                                }
                            ))
                        }
                        driverRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                        val adapter = DriverAdapter(driverList)
                        driverRecyclerView.adapter = adapter
                        val format = DateTimeFormatter.ofPattern("dd MMM yyyy")
                        val text: String = LocalDate.now().format(format)
                        currentDateTextView.text="( $text )"
                        totalStandRevenue.text="Amount  : Rs $standRevenue"
                    }
                }
            }
        }
    }

    private fun qRCodeDataFieldDialogBox(driverId:String,position: Int){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Generate QR Code")
        val dialogLayout = layoutInflater.inflate(R.layout.dialogqrfieldform, null)
        val UPIUsername  = dialogLayout.findViewById<EditText>(R.id.upiName)
        val UPICode  = dialogLayout.findViewById<EditText>(R.id.upiCode)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Generate") { _, _ ->
            if(UPIUsername.text.isNullOrBlank()||UPICode.text.isNullOrBlank()){
                Toast.makeText(requireContext(), "invalid fields", Toast.LENGTH_SHORT).show()
            }
            else{
                val display: Display = requireActivity().windowManager.defaultDisplay
                val point = Point()
                display.getSize(point)
                val width: Int = point.x
                val height: Int = point.y
                var smallerDimension = if (width < height) width else height
                smallerDimension = smallerDimension * 3 / 4
                val qrgEncoder = QRGEncoder(Gson().toJson(mapOf<String,String>("DriverID" to driverId,"UPIUsername" to UPIUsername.text.toString(),"UPICode" to UPICode.text.toString(),"Currency" to "INR")), null, QRGContents.Type.TEXT, smallerDimension)
                try {
                    val bitmap = qrgEncoder.encodeAsBitmap()
                    val ref = storageReference.child("AutoDriverQRCodes").child("$driverId.jpg")
                    val stream= ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                    val picData = stream.toByteArray()
                    val uploadTask: UploadTask = ref.putBytes(picData)
                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        ref.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "uploaded", Toast.LENGTH_SHORT).show()
                            driverRecyclerView.adapter!!.notifyItemChanged(position)
                        } else {
                            Toast.makeText(context, "upload profile pic failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: WriterException) {
                }
            }
        }
        builder.show()
    }
}