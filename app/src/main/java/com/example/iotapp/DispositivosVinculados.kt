package com.example.iotapp

import android.Manifest
import android.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class DispositivosVinculados : AppCompatActivity() {

    // Depuración de LOGCAT
    private val TAG = "DispositivosVinculados"

    // Declaracion de ListView
    var idLista:ListView = findViewById<View>(R.id.IdLista) as ListView

    // String que se enviara a la actividad principal, mainactivity
    var EXTRA_DEVICE_ADDRESS = "device_address"

    // Declaracion de campos
    /*private var mBtAdapter: BluetoothAdapter? = null*/
    val mBtAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    /*private var mPairedDevicesArrayAdapter: ArrayAdapter<*>? = null*/
    var mPairedDevicesArrayAdapter = ArrayAdapter<Any?>(this, R.layout.dispositivos_encontrados)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispositivos_vinculados)
    }

    override fun onResume() {
        super.onResume()
        //---------------------------------------------------------------------
        VerificarEstadoBT()
        mPairedDevicesArrayAdapter = ArrayAdapter<Any?>(this, R.layout.dispositivos_encontrados)
        //var idLista = findViewById<View>(R.id.IdLista) as ListView
        idLista.adapter = mPairedDevicesArrayAdapter
        idLista.onItemClickListener = mDeviceClickListener
//        mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val pairedDevices: Set<*> = mBtAdapter!!.bondedDevices
            if (pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.name + "\n" + device.address)
                }
            }
        }
        //---------------------------------------------------------------------
    }

    // Configura un (on-click) para la lista
    private val mDeviceClickListener =
        OnItemClickListener { av, v, arg2, arg3 -> // Obtener la dirección MAC del dispositivo
            val info = (v as TextView).text.toString()
            val address = info.substring(info.length - 17)
            finishAffinity()

            // Realiza un intent para iniciar la siguiente actividad
            val intend = Intent(this@DispositivosVinculados, MainActivity::class.java)
            intend.putExtra(EXTRA_DEVICE_ADDRESS, address)
            startActivity(intend)
        }

    private fun VerificarEstadoBT() {
        // Comprueba que el dispositivo tiene Bluetooth y que está encendido.
        //mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBtAdapter == null) {
            Toast.makeText(baseContext, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT)
                .show()
        } else {
            if (mBtAdapter.isEnabled) {
                Log.d(TAG, "...Bluetooth Activado...")
            } else {
                //Solicita al usuario que active Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //return;
                    startActivityForResult(enableBtIntent, 1)
                }
            }
        }
    }
}