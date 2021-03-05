package rubeg38.myalarmbutton.utils.api.checkConnection

import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.interfaces.DestroyableAPI

interface ConnectionAPI:TextMessageWatcher,DestroyableAPI {
    var onConnectionListener:OnConnectionListener?
    fun sendConnectionCheckedRequest(complete:(Boolean)->Unit)
}