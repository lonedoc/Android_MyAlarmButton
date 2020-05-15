package rubeg38.myalarmbutton.presenеtation.presenter.login

import android.util.Log
import com.google.gson.Gson
import moxy.InjectViewState
import moxy.MvpPresenter
import rubeg38.myalarmbutton.presenеtation.view.login.LoginView
import rubeg38.myalarmbutton.utils.api.ips.TCPRequest
import rubeg38.myalarmbutton.utils.api.password.OnPasswordListener
import rubeg38.myalarmbutton.utils.api.password.PasswordAPI
import rubeg38.myalarmbutton.utils.api.password.RPPasswordAPI
import rubeg38.myalarmbutton.utils.data.Cities
import rubegprotocol.RubegProtocol
import java.util.ArrayList
import kotlin.concurrent.thread

@InjectViewState
class LoginPresenter: OnPasswordListener,MvpPresenter<LoginView>(){

    lateinit var ipList:ArrayList<String>
    lateinit var nameOfOrganization: String
    var passwordAPI: PasswordAPI? = null

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

    fun savedPCSInfo(ipList: ArrayList<String>, nameOfOrganization: String) {
        Log.d("Debug", nameOfOrganization)
        Log.d("Debug","$ipList")
        this.ipList = ipList
        this.nameOfOrganization = nameOfOrganization
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun passwordRequest() {

        val ipList:ArrayList<String> = ArrayList()
        ipList.add("194.146.201.66")

        val protocol = RubegProtocol.sharedInstance
        protocol.configure(ipList,9010)
        protocol.start()
        passwordAPI = RPPasswordAPI(protocol)
        passwordAPI!!.onPasswordListener = this

        passwordAPI!!.sendPasswordRequest {
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
    }

}