package logcat.ayeautoapps.autoadmin

import android.app.Activity
import android.app.DownloadManager
import android.view.LayoutInflater
import com.mapbox.mapboxsdk.geometry.LatLng

class AddMemberModel(val title:String, var phone:String)

class StandModel(val layoutInflater: LayoutInflater,val standDocID:String,val standName:String,val standMode:Boolean, val standLandMark:String,var standNominee:Number,var standMembers:MutableMap<String,Number>,val changeStandDataFunc:(String,String,Any)->Unit,val nextPage:(String)->Unit)

class DriverModel(val activity: Activity,val index:Int,val driverUid:String,val driverObject:FireDriverModel,val isRepresentative:Boolean,val generateQRCode:(String,Int)->Unit,val nextPage:(FireDriverModel,String)->Unit)

class HistoryModel(val historyCusName:String, val history_from:String,val history_to:String,val history_amount:String,val history_date:String,val history_date_time:String,var isHeading:Boolean)

var landMarkData:String="no land mark"
var cityName:String="no city"
var standPosition:LatLng?=null
var addMemberList = ArrayList<AddMemberModel>()

var locationProvided:Boolean=false

var globalStandName:String?=null
var globalCityName:String?=null

var allDriverJSONString:String?=null
var allStandJSONString:String?=null
