package rubeg38.myalarmbutton.utils.api.auth

import rubeg38.myalarmbutton.utils.data.Auth

interface OnAuthListener {
    fun onAuthDataReceived(message: Auth)
}