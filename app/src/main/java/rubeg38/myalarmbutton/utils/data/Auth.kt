package rubeg38.myalarmbutton.utils.data

import com.google.gson.annotations.SerializedName

data class Auth(
    @SerializedName("tid") val token:String
)