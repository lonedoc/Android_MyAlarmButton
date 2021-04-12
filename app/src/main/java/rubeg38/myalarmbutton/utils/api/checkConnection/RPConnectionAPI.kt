package rubeg38.myalarmbutton.utils.api.checkConnection

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPConnectionAPI(
    private val protocol: RubegProtocol
):ConnectionAPI {
    override var onConnectionListener: OnConnectionListener? = null
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendConnectionCheckedRequest(complete: (Boolean) -> Unit) {
        val message = JSONObject()
        message.put("\$c$", "checkconnection")
        protocol.send(message.toString()){
        }
    }

    override fun onTextMessageReceived(message: String) {
        if(JSONObject(message).getString("\$c$") != "checkconnection") return
        val gson = Gson().fromJson(message,CheckConnection::class.java)
        if(gson.block !=null && gson.desc != null)
            onConnectionListener?.onConnectionListener(gson.desc)
        else
            onConnectionListener?.onConnectionListener("connect")
    }

    override fun onDestroy() {
        unsubscribe()
    }

}
data class CheckConnection(
    @SerializedName("\$c$") val command:String,
    val block:String?,
    val desc:String?
)