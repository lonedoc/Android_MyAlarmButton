package rubeg38.myalarmbutton.utils.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.rubeg38.rubegprotocol.ConnectionWatcher
import rubeg38.myalarmbutton.R
import rubeg38.myalarmbutton.utils.api.cancelAlarm.CancelAPI
import rubeg38.myalarmbutton.utils.api.cancelAlarm.RPCancelAPI
import rubeg38.myalarmbutton.utils.api.checkConnection.ConnectionAPI
import rubeg38.myalarmbutton.utils.api.checkConnection.RPConnectionAPI
import rubeg38.myalarmbutton.utils.api.coordinate.CoordinateAPI
import rubeg38.myalarmbutton.utils.api.coordinate.RPCoordinateAPI
import rubegprotocol.RubegProtocol
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NetworkService: Service(),ConnectionWatcher,LocationListener{

    lateinit var protocol: RubegProtocol
    private lateinit var unsubscribe: () -> Unit

    private var coordinateAPI:CoordinateAPI? = null
    private var connectionAPI:ConnectionAPI? = null
    private var cancelAlarm:CancelAPI? = null
    private var connectionLost = false
    private var isStartAlarm = false
    private var sendingCheckpoint = false

    companion object{
        var isHaveCoordinate = false
        var isStarted = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConnectionLost() {
        Log.d("ConnectionLost","Yes")
        connectionAPI?.sendConnectionCheckedRequest {}
    }

    override fun onConnectionEstablished() {

    }

    @SuppressLint("MissingPermission")
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun startAlarm(event:AlarmState){
        isStartAlarm = when(event.state){
            true ->{
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

                if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!=null)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0F,this)
                if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!=null)
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0f,this)
                true
            }
            false->{
                cancelAlarm?.sendCancelRequest(event.code,lat,lon,speed,accuracy)
                false
            }
        }
        Log.d("Event","${event.state}")
    }

    data class AlarmState(
        val state:Boolean,
        val code:String
    )

    data class CheckpointEvent(val state: Boolean)

    @SuppressLint("MissingPermission")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun sendCheckpoint(event: CheckpointEvent) {
        sendingCheckpoint = true

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!=null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0F,this)
        if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!=null)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0f,this)
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        val notification = createServerNotification(this)
        startForeground(1,notification)

        protocol = RubegProtocol.sharedInstance

        if(protocol.isStarted)
            protocol.stop()

        if(intent!!.hasExtra("ipList"))
            protocol.configure(intent.getStringArrayListExtra("ipList")!!,9010)

        if(intent.hasExtra("token"))
            protocol.token = intent.getStringExtra("token")

        protocol.start()
        unsubscribe = protocol.subscribe(this as ConnectionWatcher)

        if(coordinateAPI!= null) coordinateAPI?.onDestroy()
        coordinateAPI = RPCoordinateAPI(protocol)

        if(connectionAPI != null) connectionAPI?.onDestroy()
        connectionAPI = RPConnectionAPI(protocol)

        if(cancelAlarm != null) cancelAlarm?.onDestroy()
        cancelAlarm = RPCancelAPI(protocol)


        connectionAPI?.sendConnectionCheckedRequest {  }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!=null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0F,this)
        if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!=null)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0f,this)

        isStarted = true

        wakeLock()

        isHaveCoordinate = false

        return START_NOT_STICKY
    }

    private fun createServerNotification(context: Context): Notification? {
        val notificationChannelID = "ENDLESS SERVICE CHANNEL"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context,notificationChannelID)
            builder
                .setContentTitle("Сервис")
                .setContentText("Сервис для общения с сервером")
                .setSmallIcon(R.drawable.ic_service)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelID,
                "Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it
            }
            notificationManager.createNotificationChannel(channel)
            return builder.build()
        }
        else
        {
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context,notificationChannelID)

            return builder
                .setContentTitle("Сервис")
                .setContentText("Сервис для общения с сервером")
                .setSmallIcon(R.drawable.ic_service)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build()
        }
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

        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        if(protocol.isStarted)
            protocol.stop()

        connectionAPI?.onDestroy()
        coordinateAPI?.onDestroy()
        cancelAlarm?.onDestroy()

        unsubscribe()
        stopForeground(true)

        stopSelf()

        super.onDestroy()
    }

    private var oldSpeed:Int? = 100000000
    private val coordinateBuffer:ArrayList<Coordinate> = ArrayList()

    private var lat:String? = null
    private var lon:String? = null
    private var speed:Int = 0
    private var accuracy:Float? = null

    data class Coordinate(
        val lat:String,
        val lon:String,
        val speed:Int,
        val accuracy:Float
    )
    override fun onLocationChanged(location: Location) {
        val df = DecimalFormat("#.######")
        lat = df.format(location.latitude).replace(",",".")
        lon = df.format(location.longitude).replace(",",".")
        speed = (location.speed * 3.6).toInt()
        accuracy = location.accuracy

        if (protocol.isConnected && sendingCheckpoint) {
            val latitude = lat?.toFloatOrNull() ?: 0.0f
            val longitude = lon?.toFloatOrNull() ?: 0.0f

            coordinateAPI?.sendCoordinateRequest(latitude, longitude, speed, accuracy ?: 0.0f, true)
            sendingCheckpoint = false
        }

        while (coordinateBuffer.isNotEmpty() && protocol.isConnected && isStartAlarm)
        {
            val lastIndex = coordinateBuffer.lastIndex
            val coordinate = coordinateBuffer.removeAt(lastIndex)
            isHaveCoordinate = true
            coordinateAPI?.sendCoordinateRequest(coordinate.lat.toFloat(),coordinate.lon.toFloat(),coordinate.speed,coordinate.accuracy, false)
        }

        if(protocol.token==null || !isStartAlarm)
        {
            coordinateBuffer.add(Coordinate(lat!!,lon!!,speed,accuracy!!))
            return
        }

        isHaveCoordinate = true

        if(!protocol.isConnected)
        {
            coordinateBuffer.add(Coordinate(lat!!,lon!!,speed!!,accuracy!!))
            return
        }

        coordinateAPI?.sendCoordinateRequest(lat!!.toFloat(),lon!!.toFloat(),speed!!,accuracy!!, false)
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }



}