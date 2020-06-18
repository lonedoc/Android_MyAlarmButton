package rubeg38.myalarmbutton.utils.api.checkConnection

import android.util.Log
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPConnectionAPI(
    private val protocol: RubegProtocol
):ConnectionAPI {
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendConnectionCheckedRequest(complete: (Boolean) -> Unit) {
        val message = JSONObject()
        message.put("\$c$", "checkConnection")
        protocol.send(message.toString()){
        }
    }

    override fun onDestroy() {
        unsubscribe()
    }

}