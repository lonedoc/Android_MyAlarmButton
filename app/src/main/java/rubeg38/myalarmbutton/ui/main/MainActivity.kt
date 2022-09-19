package rubeg38.myalarmbutton.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_main.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import org.json.JSONObject
import rubeg38.myalarmbutton.BuildConfig
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.presenеtation.presenter.main.MainPresenter
import rubeg38.myalarmbutton.presenеtation.view.main.MainView
import rubeg38.myalarmbutton.ui.login.LoginActivity
import rubeg38.myalarmbutton.utils.PrefsUtils
import rubeg38.myalarmbutton.utils.services.NetworkService
import java.io.*
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logoFilename = "company_logo.jpg"

class MainActivity : MvpAppCompatActivity(),MainView {
    @InjectPresenter
    lateinit var presenter:MainPresenter

    lateinit var dialog:AlertDialog

    private var timer: Timer? = null

    private val preference by lazy {
        PrefsUtils(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolbar)

        //presenter.checkConnection()

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
            vibrate()

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
                            cancelButton.visibility = View.GONE
                            alarmButton.visibility = View.VISIBLE
                        }, 30000)
                    }
                }
            }
            true
        }

        phoneButton.setOnClickListener {
            vibrate()
            val number: Uri = Uri.parse("tel:${preference.companyPhone}")
            val callIntent = Intent(Intent.ACTION_DIAL, number)
            startActivity(callIntent)
        }

        callButton.setOnClickListener {
            vibrate()
            val number: Uri = Uri.parse("tel:${preference.companyPhone}")
            val callIntent = Intent(Intent.ACTION_DIAL, number)
            startActivity(callIntent)
        }

        roll_up.setOnClickListener {
            vibrate()
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)

        }



        check_it.setOnClickListener {
                vibrate()
                thread {
                    sleep(100)
                    runOnUiThread {
                        if(!NetworkService.isHaveCoordinateTest)
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
                                    Toast.makeText(this,"Ваши координаты определены",Toast.LENGTH_SHORT).show()
                                    presenter.checkConnection()
                                    if(!check_it.isEnabled)
                                    {
                                        Toast.makeText(this, "Нельзя совершать проверку чаще 2-х минут", Toast.LENGTH_LONG).show()
                                        return@runOnUiThread
                                    }
                                    check_it.isEnabled = false
                                    check_it.postDelayed({ check_it.isEnabled = true }, 10000)
                                }
                            }

                        }
                        else{
                            runOnUiThread {
                                Toast.makeText(this,"Ваши координаты определены",Toast.LENGTH_SHORT).show()
                                presenter.checkConnection()
                                if(!check_it.isEnabled)
                                {
                                    Toast.makeText(this, "Нельзя совершать проверку чаще 2-х минут", Toast.LENGTH_LONG).show()
                                    return@runOnUiThread
                                }
                                check_it.isEnabled = false
                                check_it.postDelayed({ check_it.isEnabled = true }, 10000)
                            }
                        }
                    }
                }

            }

        cancelButton.setOnClickListener {
            vibrate()
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

        setupViews()
        loadCompanyLogo()
        updateCompanyLogo()
    }

    override fun loadCompanyLogo() {
        thread {
            val data = getCompanyLogo()
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.count())

            runOnUiThread {
                val logo = findViewById<ImageView>(R.id.companyLogo)
                Glide.with(this).load(bitmap).into(logo)
            }
        }
    }

    private fun updateCompanyLogo() {
        thread {
            val data = getCompanyLogo()
            val base64String = Base64.encodeToString(data, Int.MAX_VALUE)
            presenter.updateCompanyLogo(base64String.count())
        }
    }

    private fun getCompanyLogo(): ByteArray {
        return try {
            readCompanyLogo()
        } catch (ex: FileNotFoundException) {
            byteArrayOf()
        }
    }

    private fun readCompanyLogo(): ByteArray {
        val file = File(filesDir, logoFilename)
        FileInputStream(file).use { inputStream ->
            BufferedInputStream(inputStream).use { bufferedStream ->
                return@readCompanyLogo bufferedStream.readBytes()
            }
        }
    }

    override fun hideSplashScreen() {
        runOnUiThread {
            val splashScreen = findViewById<View>(R.id.splashScreen)

            splashScreen.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        splashScreen.visibility = View.GONE
                    }
                })
        }
    }

    override fun saveLogo(data: ByteArray) {
        thread {
            try {
                writeLogoToFile(data)
                loadCompanyLogo()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun writeLogoToFile(data: ByteArray) {
        deleteLogoFile()

        val file = File(filesDir, logoFilename)
        FileOutputStream(file).use { outputStream ->
            BufferedOutputStream(outputStream).use { stream ->
                stream.write(data)
                stream.flush()
            }
        }
    }

    private fun deleteLogoFile() {
        val file = File(filesDir, logoFilename)

        if (file.exists()) {
            file.delete()
        }
    }

    private fun setupViews() {
        val patrolModeAvailable = preference.patrol
        if (patrolModeAvailable == null || !patrolModeAvailable) {
            modeTabs.visibility = View.GONE
        }

        patrolButton.setOnClickListener {
            vibrate()
            presenter.sendCheckpoint()
        }

        modeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (modeTabs.selectedTabPosition) {
                    0 -> presenter.alarmTabSelected()
                    1 -> presenter.patrolTabSelected()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) { }
            override fun onTabReselected(tab: TabLayout.Tab?) { }
        })

        timer?.cancel()
        timer = Timer()

        timer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    hideSplashScreen()
                }
            }

        }, 3500)
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

    override fun setTabsHidden(hidden: Boolean) {
        runOnUiThread {
            modeTabs.visibility = if (hidden) View.GONE else View.VISIBLE
        }
    }

    override fun setPatrolMode(patrolMode: Boolean) {
        runOnUiThread {
            if (patrolMode) {
                if (cancelButton.visibility == View.VISIBLE) {
                    return@runOnUiThread
                }

                alarmButton.visibility = View.GONE
                phoneButton.visibility = View.GONE
                check_it.visibility = View.GONE

                patrolButton.visibility = View.VISIBLE
                callButton.visibility = View.VISIBLE

                return@runOnUiThread
            }

            patrolButton.visibility = View.GONE
            callButton.visibility = View.GONE

            alarmButton.visibility = View.VISIBLE
            phoneButton.visibility = View.VISIBLE
            check_it.visibility = View.VISIBLE
        }
    }

    override fun changeButton() {
        runOnUiThread {
            if(cancelButton.visibility == View.VISIBLE) return@runOnUiThread
            Toast.makeText(
                this,
                "Тревога отправлена, ожидаем отправку ГБР, пожалуйста, не выключайте приложение",
                Toast.LENGTH_LONG
            ).show()
            cancelButton.visibility = View.VISIBLE
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

    override fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun cancelDialog() {
        runOnUiThread {
            val preference = PrefsUtils(this)
            if(preference.stationary == "0") {
                Toast.makeText(this, "Тревога завершена", Toast.LENGTH_LONG).show()
                cancelButton.visibility = View.GONE
                alarmButton.visibility = View.VISIBLE
                if(dialog.isShowing)
                    dialog.cancel()
            }
            else
            {
                Toast.makeText(this, "Тревога завершена", Toast.LENGTH_LONG).show()
                cancelButton.visibility = View.GONE
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

    override fun openTestDialog() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Проверка ТК")
                .setMessage("Тревожная кнопка работает")
                .setCancelable(true)
                .setPositiveButton("Ок"){ dialog, which ->
                    dialog.cancel()
                }
                .create().show()
        }
    }

    private fun vibrate() {
        runOnUiThread {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(250)
            }
        }
    }

}


