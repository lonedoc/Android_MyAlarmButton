package rubeg38.myalarmbutton.ui.password

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import moxy.MvpAppCompatDialogFragment
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.utils.interfaces.RegistrationCallback

class DialogPassword:MvpAppCompatDialogFragment() {

    companion object{
        lateinit var callback: RegistrationCallback
        fun newInstance(registrationCallback:RegistrationCallback):DialogPassword{
            this.callback = registrationCallback
            return DialogPassword()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?):Dialog {
        super.onCreate(savedInstanceState)

        dialog?.setCancelable(false)

        val builder = AlertDialog.Builder(context)
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = layoutInflater.inflate(R.layout.dialog_password, null)

        val passwordText:TextInputEditText = rootView.findViewById(R.id.passwordEditText)
        val registrationButton: Button = rootView.findViewById(R.id.registrationButton)

        registrationButton.setOnClickListener {
            callback.sendRegistration(passwordText.text.toString())
            dialog?.cancel()
        }

        return builder
            .setView(rootView)
            .create()
    }

}