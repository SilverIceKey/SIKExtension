package com.sik.skextensionsample

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.anim.AnimConfig
import com.sik.sikcore.anim.EffectInterpolatorType
import com.sik.sikcore.anim.anim

class SecActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sec)
        findViewById<Button>(R.id.button).apply {
            setOnClickListener {
                it.anim(AnimConfig().apply {
                    gravity = Gravity.TOP.or(Gravity.LEFT)
                    maxAlpha = 1f
                    rotation = 360f
                    duration = 800
                    setInterpolator(EffectInterpolatorType.Bounce)
                })
            }
        }
    }
}