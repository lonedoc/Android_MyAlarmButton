package rubeg38.myalarmbutton.utils.models

interface Preferences {
    var login:String?
    var password:String?
    var serverAddress: ArrayList<String>
    var serverPort: Int
    var imei: String?
    var fcmtoken: String?
    var version:String?

    val containsLogin:Boolean
    val containsPassword:Boolean
    val containsAddress: Boolean
    val containsPort: Boolean
    val containsImei: Boolean
    val containsFcmToken: Boolean
}