package com.example.clockapp

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference
import java.util.*

private lateinit var myTextView: TextView

class MainActivity : AppCompatActivity() {

    private lateinit var clockHandler: ClockHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clockHandler = ClockHandler(this)
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                clockHandler.sendEmptyMessage(0)
            }
        }, 0, 1000)
    }

    fun updateClock() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val clockView = findViewById<ClockView>(R.id.clock_view)
        clockView.setTime(hour, minute, second)
    }

    private class ClockHandler(activity: MainActivity) : Handler() {
        private val weakReference = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = weakReference.get()
            activity?.updateClock()
        }
    }
}