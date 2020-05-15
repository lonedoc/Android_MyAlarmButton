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

}