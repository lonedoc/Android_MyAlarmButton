package rubeg38.myalarmbutton.utils.api.logo

import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPLogoAPI(private var protocol: RubegProtocol): LogoAPI {
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override var onLogoFetched: ((Boolean, String?) -> Unit)? = null

    override fun sendLogoRequest(size: Int, complete: (Boolean) -> Unit) {
        val message = JSONObject()
        message.put("\$c$", "getlogo")
        message.put("size", size)

        protocol.send(
            message = message.toString(),
            onComplete = complete
        )
    }

    override fun onTextMessageReceived(message: String) {
        val json = JSONObject(message)

        if (shouldSkipMessage(json)) {
            return
        }

        if (!shouldHavePayload(json)) {
            onLogoFetched?.invoke(false, null)
            return
        }

        val payload = extractPayload(json)
        onLogoFetched?.invoke(true, payload)
    }

    private fun shouldSkipMessage(json: JSONObject): Boolean {
        if (!json.has("\$c$")) {
            return true
        }

        if (json.getString("\$c$") != "logo") {
            return true
        }

        return false
    }

    private fun shouldHavePayload(json: JSONObject): Boolean {
        if (!json.has("result")) {
            return true
        }

        return json.getString("result") != "notrefresh"
    }

    private fun extractPayload(json: JSONObject): String {
        if (!json.has("data")) {
            return ""
        }

        return json.getString("data")
    }

    override fun onDestroy() {
        unsubscribe()
    }
}