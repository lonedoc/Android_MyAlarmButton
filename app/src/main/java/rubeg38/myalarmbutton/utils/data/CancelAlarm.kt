package rubeg38.myalarmbutton.utils.data

import com.google.gson.annotations.SerializedName

data class CancelAlarm(
    @SerializedName("\$c$") val command:String,
    val result:String
)