package rubeg38.myalarmbutton.presenеtation.view.main

import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

interface MainView:MvpView {
    @StateStrategyType(value = SkipStrategy::class)
    fun changeButton()
    @StateStrategyType(value = SkipStrategy::class)
    fun gbrLeft()
    @StateStrategyType(value = SkipStrategy::class)
    fun error(message: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun cancelDialog()
    @StateStrategyType(value = SkipStrategy::class)
    fun openLoginActivity()
    @StateStrategyType(value = SkipStrategy::class)
    fun blockDialog(message: String)
}