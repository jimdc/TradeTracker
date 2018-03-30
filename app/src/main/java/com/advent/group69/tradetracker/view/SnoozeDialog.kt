package com.advent.group69.tradetracker.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.advent.group69.tradetracker.R
import com.advent.group69.tradetracker.model.SnoozeManager
import com.advent.group69.tradetracker.Utility
import com.advent.group69.tradetracker.model.SnoozeInterface
import org.jetbrains.anko.async
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

class SnoozeDialog {

    private lateinit var dialog: Dialog
    private lateinit var textHour: EditText
    private lateinit var textMinute: EditText
    private lateinit var context: Context
    private var snoozeInterface: SnoozeInterface? = null
    private var snoozeMsecInterval = 1000L

    /**
     * @param[context] must implement [SnoozeInterface] or it will throw a ClassCastException!
     */
    @SuppressLint("InflateParams")
    fun open(context: Context, isNetworkServiceRunning: Boolean) {

        try {
            snoozeInterface = context as SnoozeInterface
            this.context = context
        } catch (classCastException: ClassCastException) {
            Log.d("SnoozeManager", "Could not cast $snoozeInterface as StockInterface. Implement in MainActivity?")
        }

        if (SnoozeManager.isSnoozing()) {
            context.toast(R.string.alreadysnoozing)
            return
        }

        if (!isNetworkServiceRunning) {
            context.toast(R.string.onlysnoozewhenscanning)
            return
        }

        val mBuilder = AlertDialog.Builder(context)
        val mView = context.layoutInflater.inflate(R.layout.snooze_dialog, null)
        mBuilder.setView(mView)

        Log.i("SnoozeDialog", "Opening snooze dialog")
        dialog = mBuilder.create()
        textHour = mView.findViewById(R.id.inputHour) as EditText
        textMinute = mView.findViewById(R.id.inputMinute) as EditText
        dialog.show()

        val btnConfirmSnooze = mView.findViewById(R.id.btnSnooze) as Button
        btnConfirmSnooze.setOnClickListener(onPressSnooze)
    }

    private val onPressSnooze = View.OnClickListener {
        val iHourt = textHour.text.trim().toString()
        val iMinutet = textMinute.text.trim().toString()

        with(context) {
            if (iHourt.isEmpty() && iMinutet.isEmpty())
                Toast.makeText(this, getString(R.string.invalid_entry), Toast.LENGTH_SHORT).show()
            else {
                var snoozeMsecTotal: Long = 0
                try {
                    val hours = if (iHourt.isEmpty()) 0L else iHourt.toLong()
                    val minutes = if (iMinutet.isEmpty()) 0L else iMinutet.toLong()
                    snoozeMsecTotal = 1000 * (hours * 3600 + minutes * 60)

                    SnoozeManager.startSnooze(snoozeMsecTotal, forceRestart = true)

                } catch (numberFormatException: NumberFormatException) {
                    Toast.makeText(this, getString(R.string.NaN, "$iHourt/$iMinutet"), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                snoozeInterface?.setSnoozeInfo(resources.getString(R.string.snoozingfor, snoozeMsecTotal))
                Log.i("MainActivity", "isSnoozing set to true. Scan pausing.")
                async {
                    snoozeInterface?.setMaxSnoozeProgress(snoozeMsecTotal.toInt())
                    SnoozeManager.snoozeMsecTotal = snoozeMsecTotal
                    while (SnoozeManager.isSnoozing()) {
                        uiThread {
                            snoozeInterface?.setSnoozeProgress(SnoozeManager.getSnoozeTimeRemaining().toInt())
                            snoozeInterface?.setSnoozeInfo(
                                    resources.getString(R.string.snoozeremain,
                                            SnoozeManager.getSnoozeTimeRemaining().toInt(),
                                            snoozeMsecTotal))
                        }
                        Utility.sleepWithThreadInterruptIfWokenUp(snoozeMsecInterval)
                    }

                    uiThread {
                        snoozeInterface?.setSnoozeInfo(resources.getString(R.string.notsnoozing))
                    }
                }
                dialog.dismiss()
            }
        }
    }

}