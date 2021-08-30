package rubeg38.myalarmbutton.utils.api.logo

import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.interfaces.DestroyableAPI

interface LogoAPI: TextMessageWatcher, DestroyableAPI {
    var onLogoFetched: ((Boolean, String?) -> Unit)?
    fun sendLogoRequest(size: Int, complete: (Boolean) -> Unit)
}