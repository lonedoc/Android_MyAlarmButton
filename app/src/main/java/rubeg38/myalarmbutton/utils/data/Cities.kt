package rubeg38.myalarmbutton.utils.data

import com.google.gson.annotations.SerializedName

data class Cities(
    @SerializedName("city") val cityList:ArrayList<CityList>
)

data class CityList (
    @SerializedName("name") val cityName:String,
    @SerializedName("pr") val organizations:ArrayList<ListOfOrganizations>
)

data class ListOfOrganizations(
    @SerializedName("name") val nameOfOrganization:String,
    @SerializedName("ip") val ipList:ArrayList<String>
)