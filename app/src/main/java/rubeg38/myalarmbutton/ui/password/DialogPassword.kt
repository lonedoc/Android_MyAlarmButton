package rubeg38.myalarmbutton.ui.password

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import moxy.MvpAppCompatDialogFragment
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.utils.interfaces.RegistrationCallback


class DialogPassword:MvpAppCompatDialogFragment() {

    var timerHintTextView: TextView? = null
    var cancelButton:Button? = null
    companion object{
        lateinit var callback: RegistrationCallback
        fun newInstance(registrationCallback:RegistrationCallback):DialogPassword{
            this.callback = registrationCallback
            return DialogPassword()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?):Dialog {
        super.onCreate(savedInstanceState)

        val builder = AlertDialog.Builder(context)
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = layoutInflater.inflate(R.layout.dialog_password, null)

        val passwordText:TextInputEditText = rootView.findViewById(R.id.passwordEditText)
        val passwordInputLayout:TextInputLayout = rootView.findViewById(R.id.passwordInputLayout)
        val registrationButton: Button = rootView.findViewById(R.id.registrationButton)
        cancelButton = rootView.findViewById(R.id.cancelButton)
        timerHintTextView = rootView.findViewById(R.id.timerHintTextView)

        startTimer()

        registrationButton.setOnClickListener {
            if(passwordText.text.toString()!= ""){
                stopTimer()
                callback.sendRegistration(passwordText.text.toString())
                dialog?.cancel()
            }
            else
            {
                passwordInputLayout.error = "Поле пароля не может быть пустым"
            }
        }

        cancelButton?.setOnClickListener {
            callback.cancelRegistration()
            dialog?.cancel()
        }

        return builder
            .setView(rootView)
            .create()

    }

    private var downTimer:CountDownTimer? = null
    private fun startTimer()
    {
        downTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val second = millisUntilFinished/1000
                timerHintTextView?.hint = "Eсли пароль не пришел в течение 2-х минут, попробуйте получить пароль снова: ${minutesRemaining(second)}:${secondRemaining(second)}"

            }
            override fun onFinish() {
                /*timerHintTextView?.hint = "Eсли пароль не пришел в течение 2-х минут, попробуйте получить пароль снова: 00:00"*/
                cancelButton?.isEnabled = true
            }
        }
        downTimer?.start()
    }

    private fun stopTimer(){
        if(downTimer!=null)
            downTimer?.cancel();
    }

    fun minutesRemaining(second:Long):String{
        val minute = second/60
        return if(minute<10) {
            "0$minute"
        } else {
            "$minute"
        }
    }
    fun secondRemaining(second:Long):String{
        val seconds = second % 60
        return if(seconds<10) {
            "0$seconds"
        } else {
            "$seconds"
        }
    }
}