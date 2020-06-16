package rubeg38.myalarmbutton.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.ui.login.LoginActivity
import rubeg38.myalarmbutton.utils.PrefsUtils
import rubeg38.myalarmbutton.utils.services.NetworkService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preference = PrefsUtils(this)

        if(!preference.containsToken)
        {
            val login = Intent(this, LoginActivity::class.java)
            startActivity(login)
        }

        val service = Intent(this, NetworkService::class.java)
        service.putStringArrayListExtra("ipList",preference.serverAddress)
        service.putExtra("token",preference.token)
        startService(service)
    }
}
