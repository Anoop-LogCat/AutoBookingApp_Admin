package logcat.ayeautoapps.autoadmin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import logcat.ayeautoapps.autoadmin.HistoryModel
import logcat.ayeautoapps.autoadmin.R

class HistoryAdapter(private val historyList:List<HistoryModel>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.historycard, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(historyList[position])
    }
    override fun getItemCount(): Int {
        return historyList.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(historyList: HistoryModel) {
            val cusName: TextView =itemView.findViewById(R.id.historyCusName)
            val todayText: TextView =itemView.findViewById(R.id.isTodayTextView)
            val dateTravel: TextView =itemView.findViewById(R.id.history_date)
            val from: TextView =itemView.findViewById(R.id.history_from)
            val to: TextView =itemView.findViewById(R.id.history_to)
            val amount: TextView =itemView.findViewById(R.id.history_amount)
            from.isSelected=true
            to.isSelected=true
            cusName.text=historyList.historyCusName
            from.text=historyList.history_from
            to.text=historyList.history_to
            amount.text=historyList.history_amount
            dateTravel.text=historyList.history_date_time
            when(historyList.isHeading){
                true->todayText.text=historyList.history_date
                false->todayText.text=""
            }
        }
    }
}