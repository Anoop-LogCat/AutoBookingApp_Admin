package logcat.ayeautoapps.autoadmin

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.wang.avi.AVLoadingIndicatorView
import logcat.ayeautoapps.autoadmin.adapter.StandAdapter
import okhttp3.OkHttpClient

class ViewAutoStands : Fragment() {

    private var standList= ArrayList<StandModel>()

    private lateinit var standRecycler:RecyclerView
    private lateinit var noDataLayoutInViewAutoStands:LinearLayout
    private lateinit var progressBar:AVLoadingIndicatorView
    private var viewInViewAutoStands: View?=null

    private val radioButtons = arrayOf(
        R.id.radioButton,
        R.id.radioButton2,
        R.id.radioButton3,
        R.id.radioButton4,
        R.id.radioButton5,
        R.id.radioButton6,
        R.id.radioButton7,
        R.id.radioButton8,
        R.id.radioButton9,
        R.id.radioButton10,
        R.id.radioButton11,
        R.id.radioButton12,
        R.id.radioButton13,
        R.id.radioButton14
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_auto_stands, container, false)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class GetStandInfo : AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.show()
            standRecycler.visibility=View.INVISIBLE
            noDataLayoutInViewAutoStands.visibility=View.INVISIBLE
        }
        override fun doInBackground(vararg params: String?): String {
            return try {
                var urlParam:String = params[0]!!
                if(urlParam.compareTo("Trivandrum")==0){urlParam="Thiruvananthapuram"}
                val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/ViewAutoStand/$urlParam"
                val request: okhttp3.Request = okhttp3.Request.Builder().url(url).build()
                val response= OkHttpClient().newCall(request).execute()
                response.body()?.string().toString()
            }catch (e:Exception){
                "ERROR"
            }
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            allStandJSONString=result
            setData(result!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewInViewAutoStands=view
        standRecycler = view.findViewById(R.id.allStandsRecycler)
        noDataLayoutInViewAutoStands=view.findViewById(R.id.noDataLayoutInViewAutoStand)
        progressBar=view.findViewById(R.id.progressInViewAutoStand)
        progressBar.hide()
        noDataLayoutInViewAutoStands.visibility=View.INVISIBLE
        initializeRadios(view)
    }

    private fun setData(result:String){
        progressBar.hide()
        if(standList.isNotEmpty()){standList.clear()}
        when {
            result.compareTo("no data")==0 -> {
                noDataLayoutInViewAutoStands.visibility=View.VISIBLE
            }
            result.compareTo("ERROR")==0 -> {
                noDataLayoutInViewAutoStands.visibility=View.INVISIBLE
                allStandJSONString=null
                globalCityName=null
                Toast.makeText(context,"no network",Toast.LENGTH_SHORT).show()
            }
            else -> {
                standRecycler.visibility=View.VISIBLE
                noDataLayoutInViewAutoStands.visibility=View.INVISIBLE
                val allDocMap: java.util.HashMap<*, *>? = Gson().fromJson(result,HashMap::class.java)
                for(key in allDocMap!!.keys){
                    val eachDocData = Gson().fromJson(Gson().toJson(allDocMap[key]), FireStandModel::class.java)
                    standList.add(
                        StandModel(layoutInflater,key.toString(),eachDocData.standName,eachDocData.testMode, eachDocData.landMark,eachDocData.standRep, eachDocData.members as MutableMap<String, Number>,
                            changeStandDataFunc = {standDocID:String,fieldKey:String,fieldValue:Any->
                                FirebaseFunctions.getInstance().getHttpsCallable("ChangeStandData").call(mapOf(
                                    "docID" to standDocID,
                                    "fieldKey" to fieldKey,
                                    "fieldValue" to fieldValue
                                )).addOnCompleteListener {
                                    if(it.isSuccessful){Toast.makeText(context,"updated",Toast.LENGTH_SHORT).show()}
                                    else{Toast.makeText(context,"not updated",Toast.LENGTH_SHORT).show()}
                                }
                            },
                            nextPage= { name: String ->
                                val action =ViewAutoStandsDirections.actionViewAutoStandsToAllDriver()
                                action.standNameArg=name
                                val navController:NavController = Navigation.findNavController(viewInViewAutoStands!!)
                                navController.navigate(action)
                            }
                        )
                    )
                }
                val adapter = StandAdapter(standList)
                standRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                standRecycler.adapter = adapter
            }
        }
    }

    private fun initializeRadios(view: View){
        val radioGroup=view.findViewById<RadioGroup>(R.id.radioGroup)
        if(allStandJSONString!=null||globalCityName!=null) {
            radioButtons.forEach {
                if(view.findViewById<RadioButton>(it).text.toString().compareTo(globalCityName!!)==0){
                    view.findViewById<RadioButton>(it).isChecked=true
                    view.findViewById<RadioButton>(it).setTextColor(resources.getColor(R.color.colorSupporting))
                }
            }
            setData(allStandJSONString!!)
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if(allStandJSONString==null||globalCityName==null||globalCityName!!.compareTo(view.findViewById<RadioButton>(checkedId).text.toString())!=0) {
                globalCityName=view.findViewById<RadioButton>(checkedId).text.toString()
                globalStandName=null
                allDriverJSONString=null
                GetStandInfo().execute(globalCityName)
            }
            else{
                setData(allStandJSONString!!)
            }
            view.findViewById<RadioButton>(radioButtons.filter { checkedId == it }[0]).isChecked = true
             radioButtons.filter { checkedId != it }.forEach { view.findViewById<RadioButton>(it).isChecked = false }
         }
    }
}

