package rubeg38.myalarmbutton.utils.api.password

import android.util.Log
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPPasswordAPI(
    private var protocol: RubegProtocol
):PasswordAPI {
    override var onPasswordListener: OnPasswordListener? = null
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendPasswordRequest(complete: (Boolean) -> Unit) {
        val message = "{\"\$c$\":\"getpassword\",\"phone\":\"9041472887\"}"

        protocol.send(message){
            if (it)
                Log.d("Password","get")
            else
                Log.d("Password","notGet")
        }
    }

    override fun onTextMessageReceived(message: String) {
        onPasswordListener?.onPasswordDataReceived(message)
    }
}