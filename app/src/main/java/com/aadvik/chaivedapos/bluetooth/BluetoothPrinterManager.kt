package com.aadvik.chaivedapos.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinterManager(
    private val context: Context
) {

    companion object {
        private val SPP_UUID: UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(BluetoothManager::class.java)

    private val adapter
        get() = bluetoothManager?.adapter

    private var socket: android.bluetooth.BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        val btAdapter = adapter ?: return emptyList()

        if (!btAdapter.isEnabled) {
            return emptyList()
        }

        return try {
            btAdapter.bondedDevices
                .toList()
                .sortedBy { it.name ?: "" }
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(
        device: BluetoothDevice,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                disconnectInternal()

                val newSocket =
                    device.createRfcommSocketToServiceRecord(SPP_UUID)

                newSocket.connect()

                socket = newSocket
                outputStream = newSocket.outputStream

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                disconnectInternal()

                withContext(Dispatchers.Main) {
                    onError(
                        "Printer connection failed: ${
                            e.message ?: "Unknown Bluetooth error"
                        }"
                    )
                }
            }
        }
    }

    fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            disconnectInternal()
        }
    }

    private fun disconnectInternal() {
        try {
            outputStream?.close()
        } catch (_: Exception) {
        }

        try {
            socket?.close()
        } catch (_: Exception) {
        }

        outputStream = null
        socket = null
    }

    fun testPrint(
        paperSize: String,
        header: String,
        footer: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stream = outputStream
                    ?: throw IllegalStateException("Printer is not connected")

                val bytes = buildTestReceipt(
                    paperSize = paperSize,
                    header = header,
                    footer = footer
                )

                stream.write(bytes)
                stream.flush()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                disconnectInternal()

                withContext(Dispatchers.Main) {
                    onError(
                        "Print failed: ${
                            e.message ?: "Unknown printer error"
                        }"
                    )
                }
            }
        }
    }

    private fun buildTestReceipt(
        paperSize: String,
        header: String,
        footer: String
    ): ByteArray {

        val width =
            if (paperSize == "80mm") 48 else 32

        fun text(value: String): ByteArray =
            value.toByteArray(Charsets.UTF_8)

        fun line(value: String): ByteArray =
            text(value.take(width) + "\n")

        val output = ByteArrayOutputStream()

        // ESC @ - Initialize printer
        output.write(0x1B)
        output.write(0x40)

        // Center alignment
        output.write(0x1B)
        output.write(0x61)
        output.write(0x01)

        // Bold ON
        output.write(0x1B)
        output.write(0x45)
        output.write(0x01)

        output.write(text(header.ifBlank { "CHAI VEDA" }.take(width)))
        output.write(0x0A)

        // Bold OFF
        output.write(0x1B)
        output.write(0x45)
        output.write(0x00)

        output.write(text("Bluetooth Test Print"))
        output.write(0x0A)
        output.write(text("Printer connected successfully"))
        output.write(0x0A)
        output.write(text("--------------------------------"))
        output.write(0x0A)

        output.write(line("Paper: $paperSize"))
        output.write(line("Status: OK"))

        output.write(0x0A)

        output.write(text(footer.ifBlank { "Thank you. Visit again!" }.take(width)))
        output.write(0x0A)
        output.write(0x0A)
        output.write(0x0A)
        output.write(0x0A)

        // Left alignment
        output.write(0x1B)
        output.write(0x61)
        output.write(0x00)

        return output.toByteArray()
    }
}
