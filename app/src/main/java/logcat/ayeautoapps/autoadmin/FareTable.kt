package logcat.ayeautoapps.autoadmin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.*
import com.google.gson.Gson

class FareTable : Fragment() {

    private var viewInFareTable: View?=null
    private var city:String?=null

    private lateinit var uploadButton:Button
    private lateinit var minimumChargeEditText:EditText
    private lateinit var addedChargeEditText:EditText
    private lateinit var distanceCoverEditText:EditText

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val radioButtons = arrayOf(
        R.id.radioButton_fare,
        R.id.radioButton2_fare,
        R.id.radioButton3_fare,
        R.id.radioButton4_fare,
        R.id.radioButton5_fare,
        R.id.radioButton6_fare,
        R.id.radioButton7_fare,
        R.id.radioButton8_fare,
        R.id.radioButton9_fare,
        R.id.radioButton10_fare,
        R.id.radioButton11_fare,
        R.id.radioButton12_fare,
        R.id.radioButton13_fare,
        R.id.radioButton14_fare
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_fare_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewInFareTable=view
        addedChargeEditText=view.findViewById(R.id.addedCharge)
        minimumChargeEditText=view.findViewById(R.id.minimumCharge)
        uploadButton=view.findViewById(R.id.uploadFare)
        distanceCoverEditText=view.findViewById(R.id.distanceForMinCharge)
        uploadButton.setOnClickListener {
            if(city==null||distanceCoverEditText.text.isNullOrBlank()||minimumChargeEditText.text.isNullOrBlank()||addedChargeEditText.text.isNullOrBlank()){
                Toast.makeText(context,"Empty fields",Toast.LENGTH_SHORT).show()
            }
            else{
                val saveStandDataMap = mapOf<String,Any>(
                    "minimumCharge" to minimumChargeEditText.text.toString(),
                    "minimumDistance" to distanceCoverEditText.text.toString(),
                    "addCharge" to addedChargeEditText.text.toString()
                )
                database.child("FareTable").child(city!!).setValue(saveStandDataMap).addOnCompleteListener {
                    if(!it.isSuccessful){
                        Toast.makeText(context,"Failed to upload fare",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(context,"Fare uploaded",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        initializeRadios(view)
    }

    private fun initializeRadios(view: View){
        val radioGroup=view.findViewById<RadioGroup>(R.id.radioGroup_fare)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioText = view.findViewById<RadioButton>(checkedId).text.toString()
            city = if(radioText.compareTo("Trivandrum")==0){ "Thiruvananthapuram" } else{ radioText }
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        val fareObject = Gson().fromJson(Gson().toJson(dataSnapshot.value), FareTable::class.java)
                        minimumChargeEditText.setText(fareObject.minimumCharge)
                        addedChargeEditText.setText(fareObject.addCharge)
                        distanceCoverEditText.setText(fareObject.minimumDistance)
                    }catch (e:Exception){
                        minimumChargeEditText.text.clear()
                        addedChargeEditText.text.clear()
                        distanceCoverEditText.text.clear()
                        Toast.makeText(context, "Fare not provided", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) { }
            }
            database.child("FareTable").child(city!!).addValueEventListener(postListener)
            view.findViewById<RadioButton>(radioButtons.filter { checkedId == it }[0]).isChecked = true
            radioButtons.filter { checkedId != it }.forEach { view.findViewById<RadioButton>(it).isChecked = false }
            view.findViewById<RadioButton>(radioButtons.filter { checkedId == it }[0]).setTextColor(resources.getColor(R.color.colorSupporting))
            radioButtons.filter { checkedId != it }.forEach { view.findViewById<RadioButton>(it).setTextColor(resources.getColor(R.color.black_overlay)) }
        }
    }
    data class FareTable(val minimumCharge: String, val minimumDistance: String, val addCharge: String)
}