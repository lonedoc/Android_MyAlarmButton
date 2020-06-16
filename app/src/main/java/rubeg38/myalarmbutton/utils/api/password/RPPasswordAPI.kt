package rubeg38.myalarmbutton.utils.api.password

import android.util.Log
import com.google.gson.JsonObject
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPPasswordAPI(
    private var protocol: RubegProtocol
):PasswordAPI {
    override var onPasswordListener: OnPasswordListener? = null
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendPasswordRequest(phone:String,complete: (Boolean) -> Unit) {

        val message = JSONObject()
        message.put("\$c$", "getpassword")
        message.put("phone",phone)
        Log.d("Reg",message.toString())
        protocol.send(message.toString()){
        }
    }

    override fun onTextMessageReceived(message: String) {
        if(JSONObject(message).getString("\$c$") != "getpassword") return
        onPasswordListener?.onPasswordDataReceived(message)
    }

    override fun onDestroy() {
        unsubscribe()
    }
}