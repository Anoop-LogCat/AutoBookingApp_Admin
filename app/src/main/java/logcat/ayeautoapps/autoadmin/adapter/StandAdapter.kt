package logcat.ayeautoapps.autoadmin.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import logcat.ayeautoapps.autoadmin.R
import logcat.ayeautoapps.autoadmin.StandModel

class StandAdapter(private val standList:List<StandModel>) : RecyclerView.Adapter<StandAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.standcard, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(standList[position])
    }
    override fun getItemCount(): Int {
        return standList.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(standList: StandModel) {
            val standName: TextView =itemView.findViewById(R.id.stand)
            val standMode: TextView =itemView.findViewById(R.id.mode)
            val standLandMark: TextView =itemView.findViewById(R.id.palace)
            val modeSwitcher: SwitchCompat =itemView.findViewById(R.id.mode_switcher)
            val addMember: Button =itemView.findViewById(R.id.add_member)
            val changeNominee: Button =itemView.findViewById(R.id.change_nominee)
            val viewDrivers: Button =itemView.findViewById(R.id.next_page)
            addMember.setOnClickListener {
                val builder = AlertDialog.Builder(itemView.context)
                val inflater = standList.layoutInflater
                builder.setTitle("Add Member")
                val dialogLayout = inflater.inflate(R.layout.dialogwitheditext, null)
                val editText  = dialogLayout.findViewById<EditText>(R.id.ediText_number)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Confirm") { _, _ ->
                    if(editText.text.isNullOrBlank()||editText.text.length!=10){
                        Toast.makeText(itemView.context,"invalid phone number",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        var isMemberExist=false
                        standList.standMembers.values.forEach {
                            if(it.toLong()==(editText.text.toString()).toLong()){
                                Toast.makeText(itemView.context,"Member Exist",Toast.LENGTH_SHORT).show()
                                isMemberExist=true
                            }
                        }
                        if (!isMemberExist){
                            standList.standMembers[(standList.standMembers.size).toString()] = (editText.text.toString()).toLong()
                            standList.changeStandDataFunc(standList.standDocID,"members",standList.standMembers)
                        }
                    }
                }
                builder.show()
            }
            changeNominee.setOnClickListener {
                val builder = AlertDialog.Builder(itemView.context)
                val inflater = standList.layoutInflater
                builder.setTitle("Change Nominee")
                val dialogLayout = inflater.inflate(R.layout.dialogwitheditext, null)
                val editText  = dialogLayout.findViewById<EditText>(R.id.ediText_number)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Confirm") { _, _ ->
                    if(editText.text.isNullOrBlank()||editText.text.length!=10){
                        Toast.makeText(itemView.context,"invalid phone number",Toast.LENGTH_SHORT).show()
                    }
                    else if(standList.standNominee.toLong()==(editText.text.toString()).toLong()){
                        Toast.makeText(itemView.context,"same nominee number",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        standList.standNominee=(editText.text.toString()).toLong()
                        standList.changeStandDataFunc(standList.standDocID,"standRep",(editText.text.toString()).toLong())
                    }
                }
                builder.show()
            }
            when(standList.standMode){
                true -> {
                    standMode.text = "Test Mode"
                    modeSwitcher.isChecked=false
                    standMode.setTextColor(itemView.resources.getColor(R.color.colorGreen))
                }
                else-> {
                    standMode.text = "Production Mode"
                    modeSwitcher.isChecked=true
                    standMode.setTextColor(itemView.resources.getColor(R.color.light_blue_900))
                }
            }
            modeSwitcher.setOnCheckedChangeListener { _, isChecked ->
                when(isChecked){
                    true -> {
                        standList.changeStandDataFunc(standList.standDocID,"testMode",false)
                        standMode.text = "Production Mode"
                        standMode.setTextColor(itemView.resources.getColor(R.color.light_blue_900))
                    }
                    false -> {
                        standList.changeStandDataFunc(standList.standDocID,"testMode",true)
                        standMode.text = "Test Mode"
                        standMode.setTextColor(itemView.resources.getColor(R.color.colorGreen))
                    }
                }
            }
            standName.text=standList.standName
            standLandMark.text=standList.standLandMark
            viewDrivers.setOnClickListener {
                standList.nextPage(standName.text.toString())
            }
        }
    }
}