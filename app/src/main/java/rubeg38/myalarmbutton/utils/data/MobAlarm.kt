package rubeg38.myalarmbutton.utils.data

import com.google.gson.annotations.SerializedName

data class MobAlarm(
    @SerializedName("\$c$") val command:String,
    val result:String?,
    val gbr:String?
)