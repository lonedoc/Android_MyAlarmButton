package rubeg38.myalarmbutton.utils.api.coordinate

import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.interfaces.DestroyableAPI

interface CoordinateAPI:TextMessageWatcher,DestroyableAPI {
    var onCoordinateListener:OnCoordinateListener?
    fun sendCoordinateRequest(lat:Float,lon:Float,speed:Int,accuracy:Float)
    fun sendStationaryRequest(test:Int)
}