package rubeg38.myalarmbutton.ui.main

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import moxy.MvpView
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.json.JSONObject
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.presenеtation.presenter.main.MainPresenter
import rubeg38.myalarmbutton.presenеtation.view.main.MainView
import rubeg38.myalarmbutton.ui.login.LoginActivity
import rubeg38.myalarmbutton.utils.PrefsUtils
import rubeg38.myalarmbutton.utils.services.NetworkService
import rubegprotocol.RubegProtocol
import kotlin.concurrent.thread
import kotlin.concurrent.timer

class MainActivity : MvpAppCompatActivity(),MainView {
    @InjectPresenter
    lateinit var presenter:MainPresenter

    lateinit var dialog:AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolbar)

        val access = true

        val message = JSONObject()
        message.put("\$c$","admin")
        message.put("access",access)

        Log.d("AnswerToAdmin",message.toString())

        val preference = PrefsUtils(this)

        if(!preference.containsToken)
        {
            val login = Intent(this, LoginActivity::class.java)
            startActivity(login)
            return
        }

        mainToolbar.title = "Кобра МТК"

        val service = Intent(this, NetworkService::class.java)
        service.putStringArrayListExtra("ipList",preference.serverAddress)
        service.putExtra("token",preference.token)
        startService(service)

        roll_up.setOnClickListener {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)

        }
        check_it.setOnClickListener {
            if(!check_it.isEnabled)
            {
                Toast.makeText(this,"Нельзя совершать проверку чаще 2-х минут",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            check_it.isEnabled = false
           check_it.postDelayed({check_it.isEnabled = true},/*120000*/10000)
        }
        alarmButton.setOnClickListener {
            if(NetworkService.isStartAlarm)
                Toast.makeText(this,"Тревога уже отправлена",Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this,"Тревога отправится только при долгом зажатие",Toast.LENGTH_LONG).show()
        }
        alarmButton.setOnLongClickListener{
            if(NetworkService.isStartAlarm)
                Toast.makeText(this,"Тревога уже отправлена",Toast.LENGTH_LONG).show()
            else
            {

                presenter.sendAlarm()
                if(!NetworkService.isHaveCoordinate)
                {
                    val dialog = AlertDialog.Builder(this)
                        .setMessage("Система пробует определить ваше месторасположение...")
                        .setPositiveButton("Отмена"){
                            dialog, which ->
                            dialog.cancel()
                            NetworkService.isStartAlarm = false
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
            true
        }
        cancelAlarm.setOnClickListener {
            if(!NetworkService.isStartAlarm) {
                Toast.makeText(this,"Тревога еще не началась",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

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

                Log.d("Password",code)
                presenter.sendCancel(code)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            R.id.exit->{
                AlertDialog.Builder(this)
                    .setTitle("Выйти из аккаунта")
                    .setMessage("Если вы выйдите из аккаунты все данные будут потеряны и чтобы зарегистрироваться снова под данным аккаунтом необходимо обратиться в ЧОП")
                    .setCancelable(true)
                    .setPositiveButton("Выйти"){
                        dialog, which ->
                        dialog.cancel()

                        val service = Intent(this,NetworkService::class.java)
                        stopService(service)

                        val prefsUtils = PrefsUtils(this)
                        prefsUtils.clearData()

                        val login = Intent(this,LoginActivity::class.java)
                        startActivity(login)
                    }
                    .setNegativeButton("Отмена"){
                        dialog, which ->
                        dialog.cancel()
                    }
                    .create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onDestroy() {
        super.onDestroy()
        val service = Intent(this,NetworkService::class.java)
        stopService(service)
    }

    override fun changeButton() {
        runOnUiThread {
            if(cancelAlarm.visibility == View.VISIBLE) return@runOnUiThread
            Toast.makeText(this,"Тревога отправлена, ожидаем отправку ГБР, пожалуйста, не выключайте приложение",Toast.LENGTH_LONG).show()
            cancelAlarm.visibility = View.VISIBLE
            alarmButton.visibility = View.GONE
        }
    }

    override fun gbrLeft() {
        runOnUiThread {
            Toast.makeText(this,"ГБР выехало на вызов",Toast.LENGTH_LONG).show()
        }
    }

    override fun error(message: String) {
        runOnUiThread {
            Toast.makeText(this,message,Toast.LENGTH_LONG).show()
        }

    }

    override fun cancelDialog() {
        runOnUiThread {
            Toast.makeText(this,"Тревога завершена",Toast.LENGTH_LONG).show()
            cancelAlarm.visibility = View.GONE
            alarmButton.visibility = View.VISIBLE
            if(dialog.isShowing)
            dialog.cancel()
        }
    }

    override fun openLoginActivity() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Ошибка данных")
                .setMessage("Ваша регистрация была сброшена, обратитесь в ЧОП за более подробной информацией")
                .setCancelable(true)
                .setPositiveButton("Выйти"){
                        dialog, which ->
                    dialog.cancel()

                    val service = Intent(this,NetworkService::class.java)
                    stopService(service)

                    val prefsUtils = PrefsUtils(this)
                    prefsUtils.clearData()

                    val login = Intent(this,LoginActivity::class.java)
                    startActivity(login)
                }
                .create().show()
        }

    }
}


