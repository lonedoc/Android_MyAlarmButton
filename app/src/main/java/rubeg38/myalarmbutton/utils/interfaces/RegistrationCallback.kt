package rubeg38.myalarmbutton.utils.interfaces

interface RegistrationCallback {
    fun sendRegistration(password:String)
    fun cancelRegistration()
}