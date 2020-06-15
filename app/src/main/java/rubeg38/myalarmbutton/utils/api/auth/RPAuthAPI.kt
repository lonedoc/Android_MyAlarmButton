package rubeg38.myalarmbutton.utils.api.auth

import android.util.Log
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPAuthAPI(
    private var protocol: RubegProtocol
):AuthAPI {
    override var onAuthListener: OnAuthListener? = null
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendAuthRequest(phone:String,password:String,model:String?,complete:(Boolean)->Unit){

        val message = JSONObject()
        message.put("\$c$", "reg")
        message.put("id", "879A8884-1D0C-444F-8003-765A747B5C76")
        message.put("username", phone)
        message.put("password", password)
        message.put("os", "android")
        message.put("online", "0")
        message.put("keeplive", "10")
        message.put("model",model)

        Log.d("Reg",message.toString())
        protocol.send(message.toString()){
        }
    }

    override fun onTextMessageReceived(message: String) {
        if(JSONObject(message).getString("\$c$") == "regok")
        onAuthListener?.onAuthDataReceived(message)
    }

    override fun onDestroy() {
        unsubscribe()
    }
}