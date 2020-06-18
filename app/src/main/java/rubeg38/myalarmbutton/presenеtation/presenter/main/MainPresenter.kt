package rubeg38.myalarmbutton.presenеtation.presenter.main

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import moxy.MvpAppCompatActivity
import moxy.MvpPresenter
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.presenеtation.view.main.MainView
import rubeg38.myalarmbutton.ui.login.LoginActivity
import rubeg38.myalarmbutton.utils.PrefsUtils
import rubeg38.myalarmbutton.utils.api.coordinate.OnCoordinateListener
import rubeg38.myalarmbutton.utils.services.NetworkService

class MainPresenter:MvpPresenter<MainView>(),OnCoordinateListener{
    override fun onCoordinateListener(message: String) {

    }

}