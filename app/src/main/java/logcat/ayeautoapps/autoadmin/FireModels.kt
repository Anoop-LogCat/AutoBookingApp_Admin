package logcat.ayeautoapps.autoadmin

data class FireDriverModel(val age:String, val autoNumber:String, val workingTime:String, val imageUrl:String,val currentLatitude:Number,val currentLongitude:Number,
                           val isHiring:Boolean, val phone:String, val standLandMark:String, val standName:String,val testMode:Boolean,val standRep:Number, val token:String, val username:String)

data class FireStandModel(val latitude:Number,val longitude:Number,val standName:String,val testMode:Boolean,val landMark:String,val city:String,val standRep:Number,val members:Map<String,Number>)
