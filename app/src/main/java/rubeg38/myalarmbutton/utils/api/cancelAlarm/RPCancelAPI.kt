package rubeg38.myalarmbutton.utils.api.cancelAlarm

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.data.CancelAlarm
import rubeg38.myalarmbutton.utils.data.MobAlarm
import rubegprotocol.RubegProtocol

class RPCancelAPI(
    private var protocol:RubegProtocol
):CancelAPI {

    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)
    override var onCancelListener: OnCancelListener? = null

    override fun sendCancelRequest(
        code: String,
        lat: String?,
        lon: String?,
        speed: Int?,
        accuracy: Float?
    ) {
        val message = JSONObject()
        message.put("\$c$", "cancelalarm")
        message.put("id", "879A8884-1D0C-444F-8003-765A747B5C76")
        message.put("code",code)
        message.put("lat", lat)
        message.put("lon", lon)
        message.put("speed",speed)
        message.put("accuracy",accuracy)

        Log.d("Cancel",message.toString())
        protocol.send(message.toString()){
        }
    }

    override fun onTextMessageReceived(message: String) {
        Log.d("Message",message)

        when(JSONObject(message).getString("\$c$")){
            "cancelalarm"->{
                Log.d("CancelAlarm",message)
                val gson = Gson()
                val cancelAlarm = gson.fromJson(message, CancelAlarm::class.java)

                onCancelListener?.onCancelListener(cancelAlarm.result)
            }
            else->{ return}
        }


    }

    override fun onDestroy() {
        unsubscribe()
    }
}