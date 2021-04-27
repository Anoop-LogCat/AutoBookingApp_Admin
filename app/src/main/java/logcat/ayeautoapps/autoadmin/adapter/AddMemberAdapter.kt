package logcat.ayeautoapps.autoadmin.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import logcat.ayeautoapps.autoadmin.AddMemberModel
import logcat.ayeautoapps.autoadmin.R
import logcat.ayeautoapps.autoadmin.addMemberList

class AddMemberAdapter(private val addList:List<AddMemberModel>) : RecyclerView.Adapter<AddMemberAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.addmemberlayout, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(addList[position])
    }
    override fun getItemCount(): Int {
        return addList.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(addList: AddMemberModel) {
            val title: TextView =itemView.findViewById(R.id.addMemberText)
            val memberPhone: EditText =itemView.findViewById(R.id.addMemberPhone)

            memberPhone.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    addMemberList[addMemberList.indexOf(addList)].phone=s.toString()
                }
                override fun afterTextChanged(s: Editable?) {
                }
            })
            title.text=addList.title
            memberPhone.setText(addList.phone)
        }
    }
}