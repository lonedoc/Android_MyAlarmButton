package rubeg38.myalarmbutton.ui.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_main.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import rubeg38.myalarmbutton.BuildConfig
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.presenеtation.presenter.main.MainPresenter
import rubeg38.myalarmbutton.presenеtation.view.main.MainView
import rubeg38.myalarmbutton.ui.login.LoginActivity
import rubeg38.myalarmbutton.utils.PrefsUtils
import rubeg38.myalarmbutton.utils.services.NetworkService
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class MainActivity : MvpAppCompatActivity(),MainView {
    @InjectPresenter
    lateinit var presenter:MainPresenter

    lateinit var dialog:AlertDialog

    private val preference by lazy {
        PrefsUtils(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolbar)

        presenter.checkConnection()

        val access = true

        val message = JSONObject()
        message.put("\$c$", "admin")
        message.put("access", access)

        if(!preference.containsToken)
        {
            val login = Intent(this, LoginActivity::class.java)
            startActivity(login)
            return
        }

        val service = Intent(this, NetworkService::class.java)
        service.putStringArrayListExtra("ipList", preference.serverAddress)
        service.putExtra("token", preference.token)
        startService(service)

        checkGPSEnabled()

        alarmButton.setOnLongClickListener{
            if(!checkGPSEnabled()) return@setOnLongClickListener true

            thread {
                sleep(100)
                runOnUiThread {
                    if(preference.stationary=="0"){
                        presenter.sendMobileAlarm()
                        if(!NetworkService.isHaveCoordinate)
                        {
                            val dialog = AlertDialog.Builder(this)
                                .setMessage("Система пробует определить ваше месторасположение...")
                                .setPositiveButton("Отмена"){ dialog, which ->
                                    dialog.cancel()
                                }
                                .setCancelable(false)
                                .create()
                            dialog.show()

                            thread {
                                while (!NetworkService.isHaveCoordinate)
                                {
                                    //
                                }
                                runOnUiThread {
                                    dialog.cancel()
                                }
                            }

                        }
                    }
                    else
                    {
                        presenter.sendStationaryAlarm()
                        alarmButton.postDelayed({
                            cancelAlarm.visibility = View.GONE
                            alarmButton.visibility = View.VISIBLE
                        }, 30000)
                    }
                }
            }
            true
        }

        phoneButton.setOnClickListener {
            val number: Uri = Uri.parse("tel:${preference.companyPhone}")
            val callIntent = Intent(Intent.ACTION_DIAL, number)
            startActivity(callIntent)
        }

        roll_up.setOnClickListener {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)

        }

        check_it.setOnClickListener {
            presenter.checkConnection()
            if(!check_it.isEnabled)
            {
                Toast.makeText(this, "Нельзя совершать проверку чаще 2-х минут", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            check_it.isEnabled = false
           check_it.postDelayed({ check_it.isEnabled = true }, 10000)
        }

        cancelAlarm.setOnClickListener {
            if(preference.stationary=="0"){
                val view:View =  layoutInflater.inflate(R.layout.dialog_cancle_alarm, null)
                val codeTextView:TextInputEditText = view.findViewById(R.id.cancelCodeEditText)
                val sendCancel: Button = view.findViewById(R.id.sendCancelButton)
                dialog = AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .create()
                dialog.show()
                sendCancel.setOnClickListener {
                    val code = codeTextView.text.toString()
                    if(code == " ") {
                        codeTextView.error = "Поле не должбно быть пустым"
                        return@setOnClickListener
                    }
                    Log.d("Password", code)
                    presenter.sendCancel(code)
                }
            }
            else
            {
                //presenter.sendCancel()
                Toast.makeText(this, "Тревогу нельзя отменить", Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onStart() {
        super.onStart()

        val preference = PrefsUtils(this)
        if(preference.stationary == "0")
            supportActionBar?.title = "Мобильная v.${BuildConfig.VERSION_NAME}"
        else
            supportActionBar?.title="Стационарная v.${BuildConfig.VERSION_NAME}"
    }

    private fun checkGPSEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        return if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder(this)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.open_location_settings
                ) { _, _ ->
                    startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
                .show()
            false
        } else {
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            R.id.exit -> {
                AlertDialog.Builder(this)
                    .setTitle("Выйти из аккаунта")
                    .setMessage("Если вы выйдите из аккаунты все данные будут потеряны и чтобы зарегистрироваться снова под данным аккаунтом необходимо обратиться в ЧОП")
                    .setCancelable(true)
                    .setPositiveButton("Выйти") { dialog, which ->
                        dialog.cancel()

                        val service = Intent(this, NetworkService::class.java)
                        stopService(service)

                        val prefsUtils = PrefsUtils(this)
                        prefsUtils.clearData()

                        val login = Intent(this, LoginActivity::class.java)
                        startActivity(login)
                    }
                    .setNegativeButton("Отмена") { dialog, which ->
                        dialog.cancel()
                    }
                    .create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        val service = Intent(this, NetworkService::class.java)
        stopService(service)
        exitProcess(0)
    }

    override fun changeButton() {
        runOnUiThread {
            if(cancelAlarm.visibility == View.VISIBLE) return@runOnUiThread
            Toast.makeText(
                this,
                "Тревога отправлена, ожидаем отправку ГБР, пожалуйста, не выключайте приложение",
                Toast.LENGTH_LONG
            ).show()
            cancelAlarm.visibility = View.VISIBLE
            alarmButton.visibility = View.GONE
        }
    }

    override fun gbrLeft() {
        runOnUiThread {
            Toast.makeText(this, "ГБР выехало на вызов", Toast.LENGTH_LONG).show()
        }
    }

    override fun error(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

    }

    override fun cancelDialog() {
        runOnUiThread {
            val preference = PrefsUtils(this)
            if(preference.stationary == "0") {
                Toast.makeText(this, "Тревога завершена", Toast.LENGTH_LONG).show()
                cancelAlarm.visibility = View.GONE
                alarmButton.visibility = View.VISIBLE
                if(dialog.isShowing)
                    dialog.cancel()
            }
            else
            {
                Toast.makeText(this, "Тревога завершена", Toast.LENGTH_LONG).show()
                cancelAlarm.visibility = View.GONE
                alarmButton.visibility = View.VISIBLE
            }


        }
    }

    override fun openLoginActivity() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Ошибка данных")
                .setMessage("Ваша регистрация была сброшена, обратитесь в ЧОП за более подробной информацией")
                .setCancelable(true)
                .setPositiveButton("Выйти"){ dialog, which ->
                    dialog.cancel()

                    val service = Intent(this, NetworkService::class.java)
                    stopService(service)

                    val prefsUtils = PrefsUtils(this)
                    prefsUtils.clearData()

                    val login = Intent(this, LoginActivity::class.java)
                    startActivity(login)
                }
                .create().show()
        }

    }

    override fun blockDialog(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Задолженность")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Перепроверить баланс"){ dialog, which ->
                    dialog.cancel()
                    presenter.checkConnection()
                }
                .create().show()
        }
    }

    override fun connectDialog() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Проверка соединения")
                .setMessage("Соединение с сервером установлено")
                .setCancelable(true)
                .setPositiveButton("Ок"){ dialog, which ->
                    dialog.cancel()
                }
                .create().show()
        }
    }

}


