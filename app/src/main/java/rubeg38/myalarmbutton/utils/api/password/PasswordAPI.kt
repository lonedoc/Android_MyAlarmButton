package rubeg38.myalarmbutton.utils.api.password

import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface PasswordAPI:TextMessageWatcher {
    var onPasswordListener:OnPasswordListener?
    fun sendPasswordRequest(complete:(Boolean)->Unit)
}