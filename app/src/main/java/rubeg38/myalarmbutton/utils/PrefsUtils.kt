package rubeg38.myalarmbutton.utils

import android.content.Context
import android.content.SharedPreferences
import rubeg38.myalarmbutton.utils.models.Preferences

class PrefsUtils(context:Context): Preferences {
    private val prefsId = "myalarmbutton.DataStorage"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsId, Context.MODE_PRIVATE)

    override fun clearData(){
        prefs.edit().clear().apply()
    }

    override var serverAddress: ArrayList<String>
        get() {
            val arrayList: ArrayList<String> = ArrayList()
            val string = prefs.getString("ips", "")
            for (w in string?.split(", ")!!) {
                if (w.isNotEmpty()) {
                    arrayList.add(w)
                }
            }
            return arrayList
        }
        set(value) {
            val editor = prefs.edit()
            editor.putString("ips", value.joinToString { it }).apply()
        }

    override var serverPort: Int
        get() = prefs.getInt("port", -1)
        set(value) {
            val editor = prefs.edit()
            editor.putInt("port", value).apply()
        }

    override var fcmtoken: String?
        get() = prefs.getString("fcmtoken", null)
        set(value) {
            val editor = prefs.edit()
            editor.putString("fcmtoken", value).apply()
        }
    override var companyPhone: String?
        get() = prefs.getString("companyPhone", null)
        set(value) {
            val editor = prefs.edit()
            editor.putString("companyPhone", value).apply()
        }
    override var stationary: String?
        get() = prefs.getString("stationary", "0")
        set(value) {
            val editor = prefs.edit()
            editor.putString("stationary", value).apply()
        }
    override var patrol: String?
        get() = prefs.getString("patrol", "0")
        set(value) {
            val editor = prefs.edit()
            editor.putString("patrol", value).apply()
        }
    override var version: String?
        get() = prefs.getString("version", null)
        set(value) {
            val editor = prefs.edit()
            editor.putString("version", value).apply()
        }


    override var phone:String?
        get() = prefs.getString("phone",null)
        set(value){
            val editor = prefs.edit()
            editor.putString("phone",value).apply()
        }

    override var token: String?
        get() = prefs.getString("token",null)
        set(value){
            val editor = prefs.edit()
            editor.putString("token",value).apply()
        }


    override val containsAddress: Boolean
        get() = prefs.contains("ips")

    override val containsPort: Boolean
        get() = prefs.contains("port")

    override val containsToken: Boolean
        get() = prefs.contains("token")

    override val containsFcmToken: Boolean
        get() = prefs.contains("fcmtoken")
    override val containsStationary: Boolean
        get() = prefs.contains("stationary")
    override val containsPatrol: Boolean
        get() = prefs.contains("patrol")
    override val containsCompanyPhone: Boolean
        get() = prefs.contains("companyPhone")
    override val containsVersion: Boolean
        get() = prefs.contains("version")

    override val containsPhone:Boolean
        get() = prefs.contains("phone")

}