package logcat.ayeautoapps.autoadmin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.wang.avi.AVLoadingIndicatorView
import logcat.ayeautoapps.autoadmin.adapter.HistoryAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EachDriver : Fragment() {

    private val argsInEachDriver:EachDriverArgs by navArgs()
    private lateinit var noDataLayoutInEachDriver:LinearLayout
    private lateinit var progressBar: AVLoadingIndicatorView

    private var historyList=ArrayList<HistoryModel>()
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var cashTextView:TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_each_driver, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val profileImage:ImageView=view.findViewById(R.id.profile_image)
        val driverNameTextView:TextView=view.findViewById(R.id.driverName)
        val driverStandTextView:TextView=view.findViewById(R.id.driverStandName)
        val driverStandLocTextView:TextView=view.findViewById(R.id.driverStandLoc)
        val driverPhoneTextView:TextView=view.findViewById(R.id.callDriver)
        noDataLayoutInEachDriver=view.findViewById(R.id.noDataLayoutInEachDriver)
        progressBar=view.findViewById(R.id.progressInEachDriver)
        cashTextView=view.findViewById(R.id.sum_cod)
        driverNameTextView.text=argsInEachDriver.driverName
        driverStandTextView.text=argsInEachDriver.driverStandName
        driverStandLocTextView.text=argsInEachDriver.demoStandLocation
        driverStandLocTextView.isSelected = true
        driverPhoneTextView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CALL_PHONE)) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 34)
                } else {
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            } else {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${argsInEachDriver.driverPhone}"))
                startActivity(intent)
            }
        }
        when(argsInEachDriver.driverImage.compareTo("no image")==0){
            true->profileImage.setImageDrawable(resources.getDrawable(R.drawable.profile_in_admin))
            else->Picasso.get().load(argsInEachDriver.driverImage).into(profileImage)
        }
        historyRecyclerView=view.findViewById(R.id.travelListView)
        GetDriverHistory().execute(argsInEachDriver.driverUid)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class GetDriverHistory : AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.show()
            noDataLayoutInEachDriver.visibility=View.INVISIBLE
        }
        override fun doInBackground(vararg params: String?): String {
            return try{
                val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/ViewDriverHistory/${params[0]!!}"
                val request: Request = Request.Builder().url(url).build()
                val response= OkHttpClient().newCall(request).execute()
                response.body()?.string().toString()
            }catch (e: Exception){
                "NO_READY"
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressBar.hide()
            if(result?.compareTo("NO_READY")==0){
                Toast.makeText(context, "no network", Toast.LENGTH_SHORT).show()
            }
            else{
                val allTravelHistory: java.util.HashMap<*, *>? = Gson().fromJson(result, HashMap::class.java)
                if(historyList.isNotEmpty()){ historyList.clear() }
                allTravelHistory?.keys?.forEach{
                    val eachTravelHistory: java.util.HashMap<*, *>? = Gson().fromJson(Gson().toJson(allTravelHistory[it.toString()]), HashMap::class.java)
                    if(eachTravelHistory!!["cusName"].toString().compareTo("demoName")!=0){
                        historyList.add(
                            HistoryModel(
                                eachTravelHistory["cusName"].toString(),
                                eachTravelHistory["destination"].toString().split("_").first(),
                                eachTravelHistory["destination"].toString().split("_").last(),
                                eachTravelHistory["amount"].toString(),
                                it.toString().substring(5, 15),
                                getTime(it.toString().substring(16, 21)),
                                false
                            )
                        )
                    }
                }
                if(historyList.isEmpty()){
                    noDataLayoutInEachDriver.visibility=View.VISIBLE
                }
                else{
                    var totalCash=0.0
                    val displayList = historyList.sortedBy { it.history_date }
                    displayList.forEach {
                        val date= LocalDate.parse(it.history_date, DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH))
                        if(date == LocalDate.now()){
                            totalCash += (it.history_amount).toDouble()
                        }
                        if(displayList.indexOf(it)!=0){
                            it.isHeading = it.history_date != displayList[displayList.indexOf(it)-1].history_date
                        }
                        else{
                            it.isHeading=true
                        }
                    }
                    cashTextView.text="Today's Collections : Rs $totalCash"
                    historyRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    val adapter= HistoryAdapter(displayList)
                    historyRecyclerView.adapter = adapter
                }
            }
        }
        private fun getTime(time:String):String{
            val splitTime=time.split(":")
            return when {
                Integer.parseInt(splitTime[0]) > 12 -> {
                    "${Integer.parseInt(splitTime[0])-12}:${splitTime[1]} PM"
                }
                else -> {
                    "${splitTime[0]}:${splitTime[1]} AM"
                }
            }
        }
    }
}