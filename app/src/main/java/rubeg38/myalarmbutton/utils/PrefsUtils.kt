package rubeg38.myalarmbutton.utils

import android.content.Context
import android.content.SharedPreferences
import rubeg38.myalarmbutton.utils.models.Preferences

class PrefsUtils(context:Context): Preferences {
    private val prefsId = "ru.rubeg38.myalarmbutton"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsId, Context.MODE_PRIVATE)

    override var serverAddress: ArrayList<String>
        get() {
            val arrayList: ArrayList<String> = ArrayList()
            val string = prefs.getString("ip", "")
            for (w in string?.split(", ")!!) {
                if (w.isNotEmpty()) {
                    arrayList.add(w)
                }
            }
            return arrayList
        }
        set(value) {
            val editor = prefs.edit()
            editor.putString("ip", value.joinToString { it }).apply()
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
        get() = prefs.contains("ip")

    override val containsPort: Boolean
        get() = prefs.contains("port")

    override val containsToken: Boolean
        get() = prefs.contains("token")


    override val containsFcmToken: Boolean
        get() = prefs.contains("fcmtoken")

    override val containsPhone:Boolean
        get() = prefs.contains("phone")

}