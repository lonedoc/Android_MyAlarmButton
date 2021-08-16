package rubeg38.myalarmbutton.presenеtation.view.main

import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

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
    @StateStrategyType(value = SkipStrategy::class)
    fun connectDialog()
    @StateStrategyType(value = SkipStrategy::class)
    fun openTestDialog()
    @AddToEndSingle
    fun setTabsHidden(hidden: Boolean)
    @AddToEndSingle
    fun setPatrolMode(patrolMode: Boolean)
    @Skip
    fun showMessage(message: String)
}