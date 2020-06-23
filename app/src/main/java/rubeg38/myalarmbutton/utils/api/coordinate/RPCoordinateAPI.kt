package rubeg38.myalarmbutton.utils.api.coordinate

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.data.Auth
import rubeg38.myalarmbutton.utils.data.MobAlarm
import rubegprotocol.RubegProtocol

class RPCoordinateAPI(
    private var protocol:RubegProtocol
):CoordinateAPI {

    override var onCoordinateListener: OnCoordinateListener? = null
    override fun sendCoordinateRequest(lat: String, lon: String, speed: Int, accuracy: Float) {
        val message = JSONObject()
        message.put("\$c$", "mobalarm")
        message.put("id", "879A8884-1D0C-444F-8003-765A747B5C76")
        message.put("speed",speed)
        message.put("accuracy",accuracy)
        message.put("lat", lat)
        message.put("lon", lon)

        Log.d("Coordinate",message.toString())
        protocol.send(message.toString()){
        }
    }

    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)


    override fun onTextMessageReceived(message: String) {
        if(JSONObject(message).getString("\$c$") != "mobalarm") return
        val gson = Gson()
        val mobalarm = gson.fromJson(message, MobAlarm::class.java)

        if(mobalarm.result != null)
            onCoordinateListener?.onCoordinateListener(mobalarm.result)
        if(mobalarm.gbr != null)
            onCoordinateListener?.onCoordinateListener("gbr")
    }

    override fun onDestroy() {
        unsubscribe()
    }
}