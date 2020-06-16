package rubeg38.myalarmbutton.presenеtation.view.login

import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import rubeg38.myalarmbutton.utils.data.CityList

interface LoginView:MvpView {

    @StateStrategyType(value = SkipStrategy::class)
    fun openNoConnectionToTheCityServer()

    @StateStrategyType(value = SkipStrategy::class)
    fun initSpinnerTown(cityList: ArrayList<CityList>)
    @StateStrategyType(value = SkipStrategy::class)
    fun setPhone(phone: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun errorDialog()
    @StateStrategyType(value = SkipStrategy::class)
    fun setErrorPhoneEditText(message: String?)
    @StateStrategyType(value = SkipStrategy::class)
    fun showPasswordDialog()
    @StateStrategyType(value = SkipStrategy::class)
    fun startService(ipList: java.util.ArrayList<String>)
    @StateStrategyType(value = SkipStrategy::class)
    fun openMainActivity()
}