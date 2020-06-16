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
import ru.rubeg38.rubegprotocol.ConnectionWatcher
import rubeg38.myalarmbutton.utils.api.auth.OnAuthListener
import rubeg38.myalarmbutton.utils.api.coordinate.CoordinateAPI
import rubeg38.myalarmbutton.utils.api.coordinate.OnCoordinateListener
import rubeg38.myalarmbutton.utils.api.coordinate.RPCoordinateAPI
import rubegprotocol.RubegProtocol
import java.text.DecimalFormat

class NetworkService: Service(),ConnectionWatcher,LocationListener {

    lateinit var protocol: RubegProtocol
    private var coordinateAPI:CoordinateAPI? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConnectionLost() {

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

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0F,this)

        return START_NOT_STICKY
    }

    override fun onDestroy() {

        if(protocol.isStarted)
            protocol.stop()

        super.onDestroy()
    }

    override fun onLocationChanged(location: Location?) {
        if(location==null || protocol.token==null) return
        val df = DecimalFormat("#.######")

        coordinateAPI?.sendCoordinateRequest(df.format(location.latitude),df.format(location.longitude))
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }


}