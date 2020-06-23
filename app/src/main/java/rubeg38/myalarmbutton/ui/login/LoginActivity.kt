package rubeg38.myalarmbutton.ui.login

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_login.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.presenеtation.presenter.login.LoginPresenter
import rubeg38.myalarmbutton.presenеtation.view.login.LoginView
import rubeg38.myalarmbutton.ui.main.MainActivity
import rubeg38.myalarmbutton.ui.password.DialogPassword
import rubeg38.myalarmbutton.utils.PrefsUtils
import rubeg38.myalarmbutton.utils.data.CityList
import rubeg38.myalarmbutton.utils.data.ListOfOrganizations
import rubeg38.myalarmbutton.utils.interfaces.RegistrationCallback
import rubeg38.myalarmbutton.utils.models.setOnFocusChanged
import rubeg38.myalarmbutton.utils.models.setOnTextChanged
import rubeg38.myalarmbutton.utils.services.NetworkService
import kotlin.system.exitProcess

class LoginActivity:MvpAppCompatActivity(),LoginView,RegistrationCallback {
    @InjectPresenter
    lateinit var presenter:LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        passwordRequest.setOnClickListener { presenter.passwordRequest(phoneEditText.text.toString()) }

        phoneEditText.setOnTextChanged { str -> presenter.validatePhone(str.toString()) }

        phoneEditText.setOnFocusChanged{ _,hasFocus ->
            val phone = phoneEditText.text.toString()
            if(hasFocus) presenter.validatePhone(phone)
        }

        initPhoneMask()
    }

    private fun initPhoneMask() {
        val listener = MaskedTextChangedListener(
            "+7 ([000]) [000] [00] [00]",
            phoneEditText
        )
        phoneEditText.addTextChangedListener(listener)
        phoneEditText.onFocusChangeListener = listener

    }

    override fun onStart() {
        super.onStart()
        Log.d("Model", Build.MODEL)

    }

    override fun setPhone(phone: String) {
        phoneEditText.setText(phone)
    }

    override fun errorDialog() {
        val error = AlertDialog.Builder(this)
        error.setTitle("Ошибка")
            .setMessage("Номер введен не корректно")
            .setPositiveButton("Закрыть"){ dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

    override fun setErrorPhoneEditText(message: String?) {
        textinput_error.error = message
    }

    override fun showPasswordDialog() {
        val passwordDialog = DialogPassword.newInstance(this)
        passwordDialog.isCancelable = false
        passwordDialog.show(supportFragmentManager,"PasswordDialog")
    }

    override fun startService(ipList: java.util.ArrayList<String>) {
        val service = Intent(this,NetworkService::class.java)
        service.putStringArrayListExtra("ipList",ipList)
        startService(service)
    }

    override fun openMainActivity(){
        val main = Intent(this,MainActivity::class.java)
        startActivity(main)
    }

    override fun openNoConnectionToTheCityServer() {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage("Невозможно получить список городов, приложение будет закрыто")
            .setPositiveButton("Закрыть"){
                dialog, which ->
                onDestroy()
                dialog.cancel()
            }
            .create()
            .show()
    }

    override fun initSpinnerTown(cityList: ArrayList<CityList>) {
        runOnUiThread {
            cityList.sortBy { it.cityName }
            spinner_city.prompt = "Список городов"
            spinner_city.adapter = ArrayAdapter(
                this@LoginActivity,
                R.layout.support_simple_spinner_dropdown_item,
                cityList.map { it.cityName }
            )
            spinner_city.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if(spinner_city.selectedItem != null)
                    {
                        initSpinnerSPC(cityList[position].organizations)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
    }

    private fun initSpinnerSPC(organizations: java.util.ArrayList<ListOfOrganizations>) {
        organizations.sortedBy { it.nameOfOrganization }
        spinner_spc.prompt = "Список организаций"
        spinner_spc.adapter = ArrayAdapter(
            this@LoginActivity,
            R.layout.support_simple_spinner_dropdown_item,
            organizations.map { it.nameOfOrganization }
        )
        spinner_spc.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(spinner_spc.selectedItem != null)
                {
                    presenter.savedPCSInfo(organizations[position].ipList,organizations[position].nameOfOrganization)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun sendRegistration(password:String) {
        val phone = phoneEditText.text.toString()
        val model = Build.MODEL
        presenter.sendRegistration(phone,password,model)
    }

    override fun cancelRegistration() {
        val errorPasswordDialog = AlertDialog.Builder(this)
        errorPasswordDialog.setTitle("Отмена регистрации")
            .setMessage("Проверьте правильность данных введеных вами, если все верно, обратитесь в ЧОП за подробной информацией о вашей учетной записи")
            .setPositiveButton("Закрыть"){
                dialog, which ->
                dialog.cancel()
            }
            .create()
            .show()
    }



    private val permissionGranted = PackageManager.PERMISSION_GRANTED

    override fun checkPermission() {
        if(ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.READ_PHONE_STATE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.FOREGROUND_SERVICE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) == permissionGranted
        )
        {
            val preferences = PrefsUtils(this)
            presenter.init(preferences)
        }


        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.WAKE_LOCK,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.SYSTEM_ALERT_WINDOW,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.VIBRATE
            ),
            permissionGranted
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.FOREGROUND_SERVICE
                ),
                permissionGranted
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                permissionGranted
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(grantResults.isEmpty()) return

        when{
            grantResults.isNotEmpty() && grantResults[1] == permissionGranted && grantResults[2] == permissionGranted && grantResults[3] == permissionGranted ->{
                val preferences = PrefsUtils(this)
                presenter.init(preferences)
            }
            else->{
                presenter.errorPermission()
            }
        }
    }

    override fun errorPermissionDialog(errorMessage: String) {

        AlertDialog.Builder(this)
            .setMessage(errorMessage)
            .setCancelable(false)
            .setPositiveButton("Разрешить"){_,_->
                checkPermission()
            }
            .setNegativeButton("Закрыть приложение"){_,_->
                exitProcess(0)
            }
            .create().show()
    }

}