package rubeg38.myalarmbutton.utils.api.cancelAlarm

import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.interfaces.DestroyableAPI

interface CancelAPI:TextMessageWatcher,DestroyableAPI {
    var onCancelListener:OnCancelListener?
    fun sendCancelRequest(
        code: String,
        lat: String?,
        lon: String?,
        speed: Int?,
        accuracy: Float?
    )
}