package rubeg38.myalarmbutton.utils.api.checkConnection

import rubeg38.myalarmbutton.utils.interfaces.DestroyableAPI

interface ConnectionAPI:DestroyableAPI {
    fun sendConnectionCheckedRequest(complete:(Boolean)->Unit)
}