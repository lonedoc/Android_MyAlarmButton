package rubeg38.myalarmbutton.presenеtation.presenter.main

import moxy.InjectViewState
import moxy.MvpPresenter
import org.greenrobot.eventbus.EventBus
import rubeg38.myalarmbutton.presenеtation.view.main.MainView
import rubeg38.myalarmbutton.utils.api.cancelAlarm.CancelAPI
import rubeg38.myalarmbutton.utils.api.cancelAlarm.OnCancelListener
import rubeg38.myalarmbutton.utils.api.cancelAlarm.RPCancelAPI
import rubeg38.myalarmbutton.utils.api.coordinate.CoordinateAPI
import rubeg38.myalarmbutton.utils.api.coordinate.OnCoordinateListener
import rubeg38.myalarmbutton.utils.api.coordinate.RPCoordinateAPI
import rubeg38.myalarmbutton.utils.services.NetworkService
import rubegprotocol.RubegProtocol

@InjectViewState
class MainPresenter:MvpPresenter<MainView>(),OnCoordinateListener,OnCancelListener{

    private var coordinateAPI:CoordinateAPI? = null
    private var cancelAPI:CancelAPI? = null

    override fun onCoordinateListener(message: String) {
        when(message){
            "ok"->{ viewState.changeButton()}
            "gbr"->{viewState.gbrLeft()}
            "tokennotreg"->{viewState.openLoginActivity()}
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
    }

    fun sendAlarm()
    {
        EventBus.getDefault().post(NetworkService.AlarmState(true,""))
    }

    fun sendCancel(code: String) {
        EventBus.getDefault().post(NetworkService.AlarmState(false,code))
    }

    override fun onDestroy() {
        cancelAPI?.onDestroy()
        coordinateAPI?.onDestroy()
        super.onDestroy()

    }

    override fun onCancelListener(message: String) {
        when(message){
            "ok"->{
                viewState.cancelDialog()
            }
            "codeerror"->{viewState.error("Код введен не верно")}
        }
    }
}