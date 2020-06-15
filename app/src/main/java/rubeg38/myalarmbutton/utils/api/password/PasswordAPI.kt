package rubeg38.myalarmbutton.utils.api.password

import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.interfaces.DestroyableAPI

interface PasswordAPI:TextMessageWatcher,
    DestroyableAPI {
    var onPasswordListener:OnPasswordListener?
    fun sendPasswordRequest(phone:String,complete:(Boolean)->Unit)
}