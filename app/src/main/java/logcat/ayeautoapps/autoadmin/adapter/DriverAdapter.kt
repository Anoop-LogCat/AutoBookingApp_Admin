package logcat.ayeautoapps.autoadmin.adapter

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import logcat.ayeautoapps.autoadmin.DriverModel
import logcat.ayeautoapps.autoadmin.R


class DriverAdapter(private val driverList: List<DriverModel>) : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.drivercard, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(driverList[position])
    }
    override fun getItemCount(): Int {
        return driverList.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val storageReference: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(
            "gs://auto-pickup-apps.appspot.com"
        )

        fun bindItems(driverList: DriverModel) {
            val driverName: TextView =itemView.findViewById(R.id.driverName)
            val driverPhone: TextView =itemView.findViewById(R.id.driverPhone)
            val statusLevel: TextView =itemView.findViewById(R.id.statusLevel)
            val profileImage: CircleImageView =itemView.findViewById(R.id.profile_image_all_drivers)
            val driverCode: ImageView =itemView.findViewById(R.id.profile_driver_qr_code)
            val downloadQR: ImageView = itemView.findViewById(R.id.downloadCode)
            downloadQR.visibility=View.GONE
            storageReference.child("AutoDriverQRCodes/${driverList.driverUid}.jpg").downloadUrl.addOnSuccessListener { downloadUri->
                Picasso.get().load(downloadUri).into(driverCode)
                downloadQR.visibility=View.VISIBLE
                downloadQR.setOnClickListener {
                    val request = DownloadManager.Request(downloadUri)
                    request.setDescription("Downloading QR Code")
                    request.setTitle("QR Code")
                    request.allowScanningByMediaScanner()
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${driverName.text}'s QR Code.jpg")
                    val manager = driverList.activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                    manager!!.enqueue(request)
                    Toast.makeText(driverList.activity,"Downloaded",Toast.LENGTH_SHORT).show()
                }
            }
            when(driverList.driverObject.imageUrl.compareTo("no image")==0){
                true -> profileImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.profile_in_admin))
                else->Picasso.get().load(driverList.driverObject.imageUrl).into(profileImage)
            }
            driverName.text=driverList.driverObject.username
            driverPhone.text=driverList.driverObject.phone
            if(driverList.isRepresentative){
                statusLevel.setTextColor(itemView.resources.getColor(R.color.colorAccent))
                statusLevel.text="( Stand Nominee )"
            }else{
                statusLevel.setTextColor(itemView.resources.getColor(R.color.colorPrimary))
                statusLevel.text="( Driver )"
            }
            itemView.findViewById<Button>(R.id.view_profile).setOnClickListener {
                driverList.nextPage(driverList.driverObject, driverList.driverUid)
            }
            itemView.findViewById<Button>(R.id.generateButton).setOnClickListener {
                driverList.generateQRCode(driverList.driverUid,driverList.index)
            }
        }
    }
}