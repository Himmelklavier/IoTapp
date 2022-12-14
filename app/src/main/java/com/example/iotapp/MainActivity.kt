package com.example.iotapp

import android.Manifest.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

   /* private val permissionsStorage = arrayOf(
        permission.READ_EXTERNAL_STORAGE,
        permission.WRITE_EXTERNAL_STORAGE,
        permission.ACCESS_FINE_LOCATION,
        permission.ACCESS_COARSE_LOCATION,
        permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        permission.BLUETOOTH_SCAN,
        permission.BLUETOOTH_CONNECT,
        permission.BLUETOOTH_PRIVILEGED
    )*/
    /*private val permissionsLocation = arrayOf(
        permission.ACCESS_FINE_LOCATION,
        permission.ACCESS_COARSE_LOCATION,
        permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        permission.BLUETOOTH_SCAN,
        permission.BLUETOOTH_CONNECT,
        permission.BLUETOOTH_PRIVILEGED
    )*/

    /*private fun checkPermissions() {
        val permission1 =
            ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)
        val permission2 =
            ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_SCAN)
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                this,
                permissionsStorage,
                1
            )
        } else if (permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                permissionsLocation,
                1
            )
        }
    }*/

    //-------------------------------------------
    //val bluetoothManager: BluetoothManager? = null
    private var btAdapter: BluetoothAdapter? = null
    var bluetoothIn: Handler? = null
    var handlerState = 0
    //private var btAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
/*    private val DataStringIN = StringBuilder()
* private de extrdevicaadress lo agegu?? yo*/
    private var myConexionBT: ConnectedThread? = null
    private var extraDeviceAddress = "device_address"
    // Identificador unico de servicio - SPP UUID

    private var btmoduleuuid: UUID? = null //UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // String para la direccion MAC
    private var address: String? = null

    //-------------------------------------------


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        var btAdapter: BluetoothAdapter? = bluetoothManager.adapter
*/

        //btAdapter = bluetoothManager.adapter
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
        //val intent = intent
        address = intent.getStringExtra(extraDeviceAddress)

        bluetoothIn = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                /*when(msg.what){
                    MESSAGE_READ -> {
                        val readBuff = msg.obj as ByteArray
                        val tempMsg = String(readBuff, 0, msg.arg1)
                        PrintMessage(tempMsg)
                    }*/
                if (msg.what == handlerState) {

                    val myChar = msg.obj as Char
                    val msgPuerta: TextView = findViewById(R.id.textView3)
                    val msgSensor: TextView = findViewById(R.id.textView4)

                    if (myChar == 'F') {
                        msgPuerta.text = (getString(R.string.puertaAbiertatxt))
                    }
                    if (myChar == 'T') {
                        msgPuerta.text = (getString(R.string.puertaCerradatxt))
                    }

                    if (myChar == 'B') {
                        msgSensor.text = (getString(R.string.bajo))
                    }

                    if (myChar == 'M') {
                        msgSensor.text = (getString(R.string.medio))
                    }

                    if (myChar == 'A') {
                        msgSensor.text = (getString(R.string.alto))
                    }
                }
            }
        }
    }

    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        if (ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            btmoduleuuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        }
/*      var did: Array<ParcelUuid?> = device.uuids
        var uuidArray = ArrayList<ParcelUuid>(device.uuids.size)
        for (ParcelUuid in device.uuids.iterator()) {
            uuidArray.add(ParcelUuid)
        }
        btmoduleuuid = UUID.fromString(uuidArray[0].toString())*/


        return device.createRfcommSocketToServiceRecord(btmoduleuuid)
        //return device.createInsecureRfcommSocketToServiceRecord(btmoduleuuid)
        //creates secure outgoing connection with BT device using UUID
    }

    override fun onResume() {
        super.onResume()
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val btAdapter: BluetoothAdapter? = bluetoothManager.adapter



        val intent = intent
        address = intent.getStringExtra(extraDeviceAddress)


        //Setea la direccion MAC
        val device = btAdapter!!.getRemoteDevice(address)
        try {
            btSocket = createBluetoothSocket(device)
        } catch (e: IOException) {
            Toast.makeText(baseContext, "La creacci??n del Socket fallo", Toast.LENGTH_LONG).show()
        }
        // Establece la conexi??n con el socket Bluetooth.
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                btSocket!!.connect()
                Toast.makeText(baseContext, "CONEXION EXITOSA", Toast.LENGTH_SHORT).show()

                //return;
            }

            btSocket!!.connect()
        } catch (e: IOException) {
            try {

                btSocket!!.inputStream.close()
                btSocket!!.outputStream.close()
                btSocket!!.close()
            } catch (e2: IOException) {
                Toast.makeText(baseContext, "CONEXION CERRADA", Toast.LENGTH_SHORT).show()
            }

        }
        myConexionBT = ConnectedThread(btSocket)
        myConexionBT!!.start()
    }

    override fun onPause() {
        super.onPause()
        try { // Cuando se sale de la aplicaci??n esta parte permite que no se deje abierto el socket
            btSocket!!.inputStream.close()
            btSocket!!.outputStream.close()
            btSocket!!.close()
        } catch (e2: IOException) {
            Toast.makeText(baseContext, "IoTapp en Pausa", Toast.LENGTH_LONG).show()
        }
    }

    //Comprueba que el dispositivo Bluetooth
    //est?? disponible y solicita que se active si est?? desactivado
    private fun verificarEstadoBT() {
        /*val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        var btAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()*/
        //btAdapter = bluetoothManager.adapter

        if (btAdapter == null) {
            Toast.makeText(baseContext, "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show()
        } else {
            if (btAdapter!!.isEnabled) {
                Toast.makeText(baseContext, "BtAdapter Activo", Toast.LENGTH_LONG).show()
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //
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

        //private val mmInStream: InputStream?
        //private val mmOutStream: OutputStream?

        private val mmInStream:InputStream = socket!!.inputStream
        private val mmOutStream:OutputStream = socket!!.outputStream
        private var mmBuffer = ByteArray(1024)

        override fun run(){
            var bytes: Int

            while (true) {
                try{
                    bytes = mmInStream.read(mmBuffer)
                }
                catch (IOE:IOException){
                    break
                }
                val readMsg =  bluetoothIn!!.obtainMessage(
                    handlerState, bytes, -1,mmBuffer)
                readMsg.sendToTarget()
            }

       /* private var mmInStream: InputStream? = socket?.inputStream
        private var mmOutStream: OutputStream? = socket?.outputStream
        private val mmBuffer:ByteArray = ByteArray(1024)*/
        //private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        /*init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket?.inputStream
                tmpOut = socket?.outputStream
            } catch (e: IOException) {
                Log.d(TAG, "No stream ha sido creada", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }*/

        //override fun run() {
            // Se mantiene en modo escucha para determinar el ingreso de datos
            /*while (true) {
                *//*var numBytes: Int // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    // Read from the InputStream.
                    numBytes = try {
                        mmInStream!!.read(mmBuffer)
                    } catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }

                    // Send the obtained bytes to the UI activity.
                    val readMsg = bluetoothIn!!.obtainMessage(
                        0, numBytes, -1,
                        mmBuffer)
                    readMsg.sendToTarget()
                }*//*
               try {
                    mmInStream!!.read(mmBuffer)
                    val ch: Char = Char(mmBuffer[0].toUShort())
                    bluetoothIn?.obtainMessage(handlerState, ch)?.sendToTarget()
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
            }*/
        }




        //Envio de trama
        fun write(input: Char) {
            try {
                /*val send: ByteArray = input.getBytes()
                mChatService.write(send)*/
                val bytes: Byte = input.code.toByte()
                val send = byteArrayOf(bytes)
                mmOutStream.write(send)
            } catch (e: Exception) {
                //si no es posible enviar datos se cierra la conexi??n
                Toast.makeText(this@MainActivity, "La Conexi??n fallo", Toast.LENGTH_LONG).show()
                //exitProcess(0)

            }
        }


        //**********************************************************
       /* override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = handler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()
            }
        }*/

        // Call this from the main activity to send data to the remote device.


        // Call this method from the main activity to shut down the connection.
        /*fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }*/
    }
}
