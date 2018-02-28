package com.example.group69.alarm

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.EditText
import android.widget.Button
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.widget.RadioButton
import android.widget.CheckBox
import android.view.View
import org.jetbrains.anko.*
import java.util.Calendar
import org.jetbrains.anko.db.delete

class TimeDelay : AppCompatActivity() {

    /**
     * Customizes the UI based on intent extras "EditingCrypto" and "EditingExisting"
     * @todo make more modular by having Datenbank interaction in own function
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timedelay)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
