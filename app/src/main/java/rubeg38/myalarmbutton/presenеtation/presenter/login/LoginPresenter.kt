package rubeg38.myalarmbutton.presenеtation.presenter.login

import android.util.Log
import com.google.gson.Gson
import moxy.InjectViewState
import moxy.MvpPresenter
import rubeg38.myalarmbutton.presenеtation.view.login.LoginView
import rubeg38.myalarmbutton.utils.PrefsUtils
import rubeg38.myalarmbutton.utils.api.auth.AuthAPI
import rubeg38.myalarmbutton.utils.api.auth.OnAuthListener
import rubeg38.myalarmbutton.utils.api.auth.RPAuthAPI
import rubeg38.myalarmbutton.utils.api.ips.TCPRequest
import rubeg38.myalarmbutton.utils.api.password.OnPasswordListener
import rubeg38.myalarmbutton.utils.api.password.PasswordAPI
import rubeg38.myalarmbutton.utils.api.password.RPPasswordAPI
import rubeg38.myalarmbutton.utils.data.Cities
import rubegprotocol.RubegProtocol
import java.util.ArrayList
import kotlin.concurrent.thread

@InjectViewState
class LoginPresenter: OnAuthListener,OnPasswordListener,MvpPresenter<LoginView>(){

    lateinit var ipList:ArrayList<String>
    lateinit var nameOfOrganization: String
    var passwordAPI: PasswordAPI? = null
    var authAPI:AuthAPI? = null
    var preferences:PrefsUtils? = null
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        thread{
            when(val serverResponse = TCPRequest().requestServer()){
                "NotConnection"->{
                    viewState.openNoConnectionToTheCityServer()
                }
                else ->{
                    val cities = Gson().fromJson(serverResponse,Cities::class.java)
                    viewState.initSpinnerTown(cities!!.cityList)
                }
            }

        }
    }

    fun init(preferences: PrefsUtils) {
        this.preferences = preferences
        val phone = preferences.phone
        if(phone!=null)
            viewState.setPhone(phone)
    }

    fun savedPCSInfo(ipList: ArrayList<String>, nameOfOrganization: String) {
        this.ipList = ipList
        this.nameOfOrganization = nameOfOrganization
    }

    fun passwordRequest(phone: String) {

        val formatPhone = phone.replace("+7 (", "")
            .replace(") ", "")
            .replace(" ", "")
        Log.d("IPs","$ipList")

        if(!validatePhone(phone))
        {
            viewState.errorDialog()
            return
        }

        //viewState.showProgressDialog()
            val protocol = RubegProtocol.sharedInstance

            if(protocol.isStarted)
            protocol.stop()

            protocol.configure(ipList,9010)
            protocol.start()

            if(passwordAPI!= null) passwordAPI?.onDestroy()

            passwordAPI = RPPasswordAPI(protocol)
            passwordAPI!!.onPasswordListener = this

            passwordAPI!!.sendPasswordRequest(formatPhone) {
                if (it)
                {
                    Log.d("Password","get")
                }
                    else
                {
                    Log.d("Password","notget")
                }
            }
    }

    override fun onPasswordDataReceived(message: String) {
        Log.d("Password",message)
        //viewState.closeProgressDialog()
        val protocol = RubegProtocol.sharedInstance

        if(protocol.isStarted)
            protocol.stop()

        viewState.showPasswordDialog()
    }

    fun validatePhone(str: String):Boolean{
        val message: String?
        return when{
            str.length<18 ->{
                message = "Номер телефона введен не полностью"
                viewState.setErrorPhoneEditText(message)
                false
            }
            else ->{
                viewState.setErrorPhoneEditText(null)
                true
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    fun sendRegistration(phone: String, password: String, model: String?) {

        val formatPhone = phone.replace("+7 (", "")
            .replace(") ", "")
            .replace(" ", "")

        viewState.startService(ipList)

        val protocol = RubegProtocol.sharedInstance

        if(authAPI!= null) authAPI?.onDestroy()

        authAPI = RPAuthAPI(protocol)
        authAPI!!.onAuthListener = this

        authAPI!!.sendAuthRequest(formatPhone,password,model) {
            if (it)
            {
                Log.d("Auth","get")
            }
            else
            {
                Log.d("Auth","notget")
            }
        }
    }

    override fun onAuthDataReceived(token: String) {
        val protocol = RubegProtocol.sharedInstance

        preferences?.token = token
        preferences?.serverAddress = ipList
        preferences?.serverPort = 9010

        protocol.token = token

        viewState.openMainActivity()
    }
}