package com.example.iotapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class DispositivosVinculados : AppCompatActivity() {

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_PRIVILEGED
    )
    private val PERMISSIONS_LOCATION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_PRIVILEGED
    )

    private fun checkPermissions() {
        val permission1 =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission2 =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                1
            )
        } else if (permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_LOCATION,
                1
            )
        }
    }

    // Depuración de LOGCAT
    //private val tag = "DispositivosVinculados"

    // Declaracion de ListView Sebas añadió private
    //var idLista:ListView = findViewById<View>(R.id.idLista) as ListView
    //idLista = findViewById<View>(R.id.idLista) as ListView

    //*************************************************************************************
    //private var mPairedDevicesArrayAdapter = ArrayAdapter<Any?>(this, R.layout.dispositivos_encontrados)
    //private var listView: ListView = findViewById(R.id.idLista)


    //*************************************************************************************



    //private var idLista: ListView = findViewById<ListView>(R.id.idLista)
    //var mPairedDevicesArrayAdapter = ArrayAdapter<Any?>(this, R.layout.dispositivos_encontrados)
    //idLista.adapter = mPairedDevicesArrayAdapter
    /* var mListView = findViewById<ListView>(R.id.userlist)
    arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, users)
    mListView.adapter = arrayAdapter*/


    // String que se enviara a la actividad principal, mainactivity Sebas añadió private
    private var extraDeviceAddress = "device_address"

    // Declaracion de campos
    /*private var mBtAdapter: BluetoothAdapter? = null //Sebas añadió private*/
    //private val bluetoothAdapter: BluetoothAdapter? = getDefaultAdapter()
    /*private var mPairedDevicesArrayAdapter: ArrayAdapter<*>? = null // Sebas añadió private*/



    /* if (bluetoothAdapter == null)
     {
         // Device doesn't support Bluetooth
     }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispositivos_vinculados)
        checkPermissions()
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

        var listView = findViewById<ListView>(R.id.idLista)

    }

    override fun onResume() {
        super.onResume()
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
        //---------------------------------------------------------------------
        verificarEstadoBT()
        var mPairedDevicesArrayAdapter = ArrayAdapter<Any?>(this, R.layout.dispositivos_encontrados)
        var listView = findViewById<ListView>(R.id.idLista)
        //idLista = findViewById<View>(R.id.idLista) as ListView
        listView.adapter = mPairedDevicesArrayAdapter
        listView.onItemClickListener = mDeviceClickListener
        bluetoothAdapter = bluetoothManager.adapter
//        mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val pairedDevices: Set<*> = bluetoothAdapter!!.bondedDevices
            if (pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.toString() )//+ "\n" + extraDeviceAddress)
                }
            }
        }
        //---------------------------------------------------------------------
    }

    // Configura un (on-click) para la lista
    private val mDeviceClickListener =
        //OnItemClickListener { av, v, arg2, arg3 -> // Obtener la dirección MAC del dispositivo
        OnItemClickListener { _, v, _, _ -> // Obtener la dirección MAC del dispositivo
            val info = (v as TextView).text.toString()
            val address = info.substring(info.length - 17)
            finishAffinity()

            // Realiza un intent para iniciar la siguiente actividad
            val intend = Intent(this@DispositivosVinculados, MainActivity::class.java)
            intend.putExtra(extraDeviceAddress, address)
            startActivity(intend)
        }

    private fun verificarEstadoBT() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
        // Comprueba que el dispositivo tiene Bluetooth y que está encendido.
        //mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(baseContext, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT)
                .show()}
        if (bluetoothAdapter?.isEnabled == false) {
            val REQUEST_ENABLE_BT = 1
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
                return
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        /*} else {

            if (bluetoothAdapter.isEnabled) {
                Log.d(tag, "...Bluetooth Activado...")
            } else {
                //Solicita al usuario que active Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(enableBtIntent, 1)
                }
            }
        }*/
    }
}

