package rubeg38.myalarmbutton.utils.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.rubeg38.rubegprotocol.ConnectionWatcher
import rubeg38.myalarmbutton.utils.api.auth.OnAuthListener
import rubeg38.myalarmbutton.utils.api.checkConnection.ConnectionAPI
import rubeg38.myalarmbutton.utils.api.checkConnection.RPConnectionAPI
import rubeg38.myalarmbutton.utils.api.coordinate.CoordinateAPI
import rubeg38.myalarmbutton.utils.api.coordinate.OnCoordinateListener
import rubeg38.myalarmbutton.utils.api.coordinate.RPCoordinateAPI
import rubegprotocol.RubegProtocol
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NetworkService: Service(),ConnectionWatcher,LocationListener {

    lateinit var protocol: RubegProtocol
    private var coordinateAPI:CoordinateAPI? = null
    private var connectionAPI:ConnectionAPI? = null
    private var connectionLost = false
    private var isStarted = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConnectionLost() {
        connectionAPI?.sendConnectionCheckedRequest {}
    }

    override fun onConnectionEstablished() {

    }


    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        protocol = RubegProtocol.sharedInstance

        if(protocol.isStarted)
            protocol.stop()

        if(intent!!.hasExtra("ipList"))
            protocol.configure(intent.getStringArrayListExtra("ipList")!!,9010)
        if(intent!!.hasExtra("token"))
            protocol.token = intent.getStringExtra("token")

        protocol.start()

        if(coordinateAPI!= null) coordinateAPI?.onDestroy()
        coordinateAPI = RPCoordinateAPI(protocol)

        if(connectionAPI != null) connectionAPI?.onDestroy()
        connectionAPI = RPConnectionAPI(protocol)

        connectionAPI?.sendConnectionCheckedRequest {  }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0F,this)

        isStarted = true
        wakeLock()

        return START_NOT_STICKY
    }

    private var wakeLock:PowerManager.WakeLock? = null

    private fun wakeLock() {
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService:lock").apply {
                acquire()
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            while (isStarted) {
                launch(Dispatchers.IO) {
                    Log.d("Service", "process")
                    pingFakeServer()
                }
                delay(1 * 60 * 1000)
            }
        }
    }

    @SuppressLint("HardwareIds", "SimpleDateFormat")
    private fun pingFakeServer() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ")
        val gmtTime = df.format(Date())

        val deviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

        val json =
            """
                {
                    "deviceId": "$deviceId",
                    "createdAt": "$gmtTime"
                }
            """
        try {
            Log.d("PinkFakeService", "true")
            Fuel.post("https://jsonplaceholder.typicode.com/posts")
                .jsonBody(json)
                .response { _, _, result ->

                    val (bytes, error) = result
                    if (bytes != null) {
                        // faik
                    } else {
                        // faik
                    }
                }
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        isStarted = false

        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        if(protocol.isStarted)
            protocol.stop()

        connectionAPI?.onDestroy()
        coordinateAPI?.onDestroy()

        super.onDestroy()
    }

    private var oldSpeed:Float? = null
    private val coordinateBuffer:ArrayList<Pair<String,String>> = ArrayList()
    override fun onLocationChanged(location: Location?) {
        if(location==null || protocol.token==null) return

        while (coordinateBuffer.isNotEmpty())
        {
            val lastIndex = coordinateBuffer.lastIndex
            val coordinate = coordinateBuffer.removeAt(lastIndex)
            coordinateAPI?.sendCoordinateRequest(coordinate.first,coordinate.second)
        }

        if(oldSpeed == location.speed) return

        val df = DecimalFormat("#.######")

        if(!protocol.isConnected)
        {
            coordinateBuffer.add(Pair(df.format(location.latitude),df.format(location.longitude)))
            return
        }

        coordinateAPI?.sendCoordinateRequest(df.format(location.latitude),df.format(location.longitude))

    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }


}