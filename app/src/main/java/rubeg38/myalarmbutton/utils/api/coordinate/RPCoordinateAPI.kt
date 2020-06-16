package rubeg38.myalarmbutton.utils.api.coordinate

import android.util.Log
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPCoordinateAPI(
    private var protocol:RubegProtocol
):CoordinateAPI {

    override var onCoordinateListener: OnCoordinateListener? = null
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendCoordinateRequest(lat: String, lon: String) {
        val message = JSONObject()
        message.put("\$c$", "mobalarm")
        message.put("id", "879A8884-1D0C-444F-8003-765A747B5C76")
        message.put("lat", lat)
        message.put("lon", lon)

        Log.d("Coordinate",message.toString())
        protocol.send(message.toString()){
        }
    }

    override fun onTextMessageReceived(message: String) {
        Log.d("Message",message)
    }

    override fun onDestroy() {
        unsubscribe()
    }
}