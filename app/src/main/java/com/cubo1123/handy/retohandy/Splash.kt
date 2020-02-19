package com.cubo1123.handy.retohandy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        object : CountDownTimer(1500, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val i = Intent(this@Splash, MainActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()

            }
        }.start()
    }
}

