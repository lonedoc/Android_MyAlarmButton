package rubeg38.myalarmbutton.utils.models

interface Preferences {
    var phone:String?
    var token:String?
    var serverAddress: ArrayList<String>
    var serverPort: Int
    var fcmtoken: String?
    var companyPhone:String?
    var stationary:String?
    var patrol:String?

    val containsPhone:Boolean
    val containsAddress:Boolean
    val containsPort:Boolean
    val containsToken:Boolean
    val containsFcmToken: Boolean
    val containsStationary:Boolean
    val containsPatrol:Boolean
    val containsCompanyPhone:Boolean

    fun clearData()
}