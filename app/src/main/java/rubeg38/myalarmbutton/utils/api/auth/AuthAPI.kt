package rubeg38.myalarmbutton.utils.api.auth

import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.interfaces.DestroyableAPI

interface AuthAPI:TextMessageWatcher,
    DestroyableAPI {
    var onAuthListener:OnAuthListener?
    fun sendAuthRequest(phone:String,password:String,model:String?,complete:(Boolean)->Unit)
}