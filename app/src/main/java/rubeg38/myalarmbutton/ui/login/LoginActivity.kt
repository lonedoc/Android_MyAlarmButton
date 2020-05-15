package rubeg38.myalarmbutton.ui.login

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_login.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.presenеtation.presenter.login.LoginPresenter
import rubeg38.myalarmbutton.presenеtation.view.login.LoginView
import rubeg38.myalarmbutton.utils.data.CityList
import rubeg38.myalarmbutton.utils.data.ListOfOrganizations

class LoginActivity:MvpAppCompatActivity(),LoginView {
    @InjectPresenter
    lateinit var presenter:LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password_request.setOnClickListener { presenter.passwordRequest() }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Model", Build.MODEL)
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

}