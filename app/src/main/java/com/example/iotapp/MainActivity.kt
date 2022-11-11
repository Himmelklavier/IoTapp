package com.example.iotapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.system.exitProcess



class MainActivity : AppCompatActivity() {

    //-------------------------------------------
    var bluetoothIn: Handler? = null
    var handlerState = 0
    private var btAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
/*    private val DataStringIN = StringBuilder()
* private de extrdevicaadress lo agegué yo*/
    private var myConexionBT: ConnectedThread? = null
    private var extraDeviceAddress = "device_address"
    // Identificador unico de servicio - SPP UUID
    private val btmoduleuuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // String para la direccion MAC
    private var address: String? = null
    //-------------------------------------------


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        verificarEstadoBT()

        val abrirButton: Button = findViewById(R.id.button)
        val cerrarButton: Button = findViewById(R.id.button2)
        val estadoPuertasTextView: TextView = findViewById(R.id.textView3)
        val puertasImage: ImageView = findViewById(R.id.imageView)

        abrirButton.setOnClickListener {
            estadoPuertasTextView.text = getString(R.string.puertaAbierta)
            puertasImage.setImageResource(R.drawable.puertasabiertas)
            myConexionBT!!.write('f')
        }
        cerrarButton.setOnClickListener {
            estadoPuertasTextView.text = getString(R.string.puertaCerrada)
            puertasImage.setImageResource(R.drawable.puertascerradas)
            myConexionBT!!.write('t')
        }
       /* btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket != null) {
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
                        ;
                    }
                }
                finish();
            }
        });*/
        /*val intent = intent*/
        address = intent.getStringExtra(extraDeviceAddress)

        bluetoothIn = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == handlerState) {

                    val myChar = msg.obj as Char
                    val msgPuerta: TextView = findViewById(R.id.textView3)
                    val msgSensor: TextView = findViewById(R.id.textView4)

                    if (myChar == 'F') {
                        msgPuerta.text = ("Puerta Abierta")
                    }
                    if (myChar == 'T') {
                        msgPuerta.text = ("Puerta Cerrada")
                    }

                    if (myChar == 'L') {
                        msgSensor.text = ("Bajo")
                    }

                    if (myChar == 'M') {
                        msgSensor.text = ("Medio")
                    }

                    if (myChar == 'H') {
                        msgSensor.text = ("Alto")
                    }

                    if (myChar == 'E') {
                        msgSensor.text = ("Error")
                    }

                }
            }
        }

    }

    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
        }
        return device.createRfcommSocketToServiceRecord(btmoduleuuid)
        //creates secure outgoing connection with BT device using UUID
    }

    override fun onResume() {
        super.onResume()



        //Setea la direccion MAC
        val device = btAdapter!!.getRemoteDevice(address)
        try {
            btSocket = createBluetoothSocket(device)
        } catch (e: IOException) {
            Toast.makeText(baseContext, "La creacción del Socket fallo", Toast.LENGTH_LONG).show()
        }
        // Establece la conexión con el socket Bluetooth.
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                btSocket!!.connect()
                //Toast.makeText(getBaseContext(), "CONEXION EXITOSA", Toast.LENGTH_SHORT).show();

                //return;
            }

            //btSocket.connect();
        } catch (e: IOException) {
            try {
                btSocket!!.close()
            } catch (e2: IOException) {
            }
        }
        myConexionBT = ConnectedThread(btSocket)
        myConexionBT!!.start()
    }

    override fun onPause() {
        super.onPause()
        try { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket!!.close()
        } catch (e2: IOException) {
        }
    }

    //Comprueba que el dispositivo Bluetooth
    //está disponible y solicita que se active si está desactivado
    private fun verificarEstadoBT() {
        if (btAdapter == null) {
            Toast.makeText(baseContext, "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG)
                .show()
        } else {
            if (btAdapter!!.isEnabled) {
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    startActivityForResult(enableBtIntent, 1)
                    //return;
                }
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    inner class ConnectedThread(socket: BluetoothSocket?) : Thread() {
        /*private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream*/

        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?


        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket?.inputStream
                tmpOut = socket?.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            val bytein = ByteArray(1)
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    mmInStream!!.read(bytein)
                    val ch = Char(bytein[0].toUShort())
                    bluetoothIn?.obtainMessage(handlerState, ch)?.sendToTarget()
                } catch (e: IOException) {
                    break
                }
            }
        }

        //Envio de trama
        fun write(input: Char) {
            try {
                /*val send: ByteArray = input.getBytes()
                mChatService.write(send)*/
                val bytes: Byte = input.code.toByte()
                val send = byteArrayOf(bytes)
                mmOutStream!!.write(send)
            } catch (e: Exception) {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(this@MainActivity, "La Conexión fallo", Toast.LENGTH_LONG).show()
                exitProcess(0)
            }
        }
    }
}
