package com.example.iotapp

import android.Manifest.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class DispositivosVinculados : AppCompatActivity() {

    private val permissionsStorage = arrayOf(
        permission.READ_EXTERNAL_STORAGE,
        permission.WRITE_EXTERNAL_STORAGE,
        permission.ACCESS_FINE_LOCATION,
        permission.ACCESS_COARSE_LOCATION,
        permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        permission.BLUETOOTH_SCAN,
        permission.BLUETOOTH_CONNECT,
        permission.BLUETOOTH_PRIVILEGED
    )
    private val permissionsLocation = arrayOf(
        permission.ACCESS_FINE_LOCATION,
        permission.ACCESS_COARSE_LOCATION,
        permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        permission.BLUETOOTH_SCAN,
        permission.BLUETOOTH_CONNECT,
        permission.BLUETOOTH_PRIVILEGED
    )

    private fun checkPermissions() {
        val permission1 =
            ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)
        val permission2 =
            ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_CONNECT)
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
    }

    // String que se enviara a la actividad principal, mainactivity Sebas añadió private
    private var extraDeviceAddress = "device_address"
    // Declaracion de campos teniamos bluetooth manager y adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispositivos_vinculados)
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
        verificarEstadoBT()
        val mPairedDevicesArrayAdapter = ArrayAdapter<Any?>(this, R.layout.dispositivos_encontrados)
        val listView = findViewById<ListView>(R.id.idLista)
        listView.adapter = mPairedDevicesArrayAdapter
        listView.onItemClickListener = mDeviceClickListener

        if (ActivityCompat.checkSelfPermission(
                this,
                permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val pairedDevices: Set<*> = bluetoothAdapter.bondedDevices
            if (pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.toString() )//+ "\n" + extraDeviceAddress)
                }
            }
        }
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
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
        // Comprueba que el dispositivo tiene Bluetooth y que está encendido.
        if (!bluetoothAdapter.isEnabled) {
            val requestEnableBT = 1
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {return}
            startActivityForResult(enableBtIntent, requestEnableBT)
        } else {
            if (bluetoothAdapter.isEnabled) {
                Log.d(TAG, "...Bluetooth Activado...")
            } else {
                //Solicita al usuario que active Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(enableBtIntent, 1)
                }
            }
        }
        /*if (bluetoothAdapter == null) {
            Toast.makeText(baseContext, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT)
                .show()}*/
    }
}

