package rubeg38.myalarmbutton.utils.servicess

import android.app.Service
import android.content.Intent
import android.os.IBinder

class NetworkService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}