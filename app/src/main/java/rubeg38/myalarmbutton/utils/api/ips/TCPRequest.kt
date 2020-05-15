package rubeg38.myalarmbutton.utils.api.ips

import ru.rubeg38.rubegprotocol.TCPCoder
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class TCPRequest {

    val ipList= arrayOf("94.177.183.4","194.125.255.105")

    lateinit var echoSocket: Socket
    fun requestServer():String
    {
        var count = 0

        do {
           for(i in ipList.indices)
           {
               echoSocket = Socket()
               echoSocket.connect(InetSocketAddress(ipList[i],8300),5000)
               if(!echoSocket.isConnected)
                   count++
               else
                   return serverResponse()
           }
        }while (count != 3 )

        return "NotConnection"
    }

    val HEADERS_SIZE = 21

    private fun serverResponse():String {
        val requestString = "{\"\$c$\":\"getcity\"}"
        val out = DataOutputStream(echoSocket.getOutputStream())
        val requestArray = TCPCoder().encode(requestString.toByteArray())
        out.write(requestArray)

        val din = DataInputStream(echoSocket.getInputStream())
        val headers = ByteArray(HEADERS_SIZE)
        din.read(headers)

        val count = TCPCoder().decodeHeaders(headers)

        val response = ByteArray(count)

        val data = ByteArray(count)

        var position = 0

        do {
            val bytesRead = din.read(response)
            for (i in 0 until bytesRead) {
                data[position + i] = response[i]
            }
            position += bytesRead
        } while (position != count)

        echoSocket.close()


        return TCPCoder().decode(data)
    }
}