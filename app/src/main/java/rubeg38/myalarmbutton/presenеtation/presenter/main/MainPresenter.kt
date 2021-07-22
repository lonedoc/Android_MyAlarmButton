package rubeg38.myalarmbutton.presenеtation.presenter.main

import moxy.InjectViewState
import moxy.MvpPresenter
import org.greenrobot.eventbus.EventBus
import rubeg38.myalarmbutton.presenеtation.view.main.MainView
import rubeg38.myalarmbutton.utils.api.cancelAlarm.CancelAPI
import rubeg38.myalarmbutton.utils.api.cancelAlarm.OnCancelListener
import rubeg38.myalarmbutton.utils.api.cancelAlarm.RPCancelAPI
import rubeg38.myalarmbutton.utils.api.checkConnection.ConnectionAPI
import rubeg38.myalarmbutton.utils.api.checkConnection.OnConnectionListener
import rubeg38.myalarmbutton.utils.api.checkConnection.RPConnectionAPI
import rubeg38.myalarmbutton.utils.api.coordinate.CoordinateAPI
import rubeg38.myalarmbutton.utils.api.coordinate.OnCoordinateListener
import rubeg38.myalarmbutton.utils.api.coordinate.RPCoordinateAPI
import rubeg38.myalarmbutton.utils.services.NetworkService
import rubegprotocol.RubegProtocol

@InjectViewState
class MainPresenter:MvpPresenter<MainView>(),OnCoordinateListener,OnCancelListener,OnConnectionListener{

    private var coordinateAPI:CoordinateAPI? = null
    private var cancelAPI:CancelAPI? = null
    private var connectionAPI:ConnectionAPI? = null

    override fun onCoordinateListener(message: String) {
        when(message){
            "ok"->{ viewState.changeButton()}
            "gbr"->{viewState.gbrLeft()}
            "tokennotreg"->{viewState.openLoginActivity()}
            "test"->{viewState.openTestDialog()}
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val protocol = RubegProtocol.sharedInstance
        if(coordinateAPI != null) coordinateAPI?.onDestroy()
        coordinateAPI= RPCoordinateAPI(protocol)
        coordinateAPI?.onCoordinateListener = this

        if(cancelAPI != null) cancelAPI?.onDestroy()
        cancelAPI= RPCancelAPI(protocol)
        cancelAPI?.onCancelListener = this

        if(connectionAPI != null) connectionAPI?.onDestroy()
        connectionAPI = RPConnectionAPI(protocol)
        connectionAPI?.onConnectionListener = this
    }

    fun sendMobileAlarm()
    {
        EventBus.getDefault().post(NetworkService.AlarmState(true,""))
    }

    fun sendCancel(code: String) {
        EventBus.getDefault().post(NetworkService.AlarmState(false,code))
    }
    fun sendCancel(){
        cancelAPI?.sendCancelRequest("","","",0,0f)
    }

    override fun onDestroy() {
        cancelAPI?.onDestroy()
        coordinateAPI?.onDestroy()
        connectionAPI?.onDestroy()
        super.onDestroy()

    }

    override fun onCancelListener(message: String) {
        when(message){
            "ok"->{
                viewState.cancelDialog()
            }
            "codeerror"->{
                viewState.error("Код введен не верно")
            }
        }
    }

    fun checkConnection() {
        coordinateAPI?.sendStationaryRequest(1)
    }

    override fun onConnectionListener(message: String) {
        if(message=="connect")
            viewState.connectDialog()
        else
            viewState.blockDialog(message)
    }

    fun sendStationaryAlarm() {
        coordinateAPI?.sendStationaryRequest(0)
    }
}