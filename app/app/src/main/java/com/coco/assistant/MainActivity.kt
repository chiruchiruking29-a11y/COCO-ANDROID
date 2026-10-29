
package com.coco.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.TextToSpeech.LANG_AVAILABLE
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech
    private val voiceRequest = 101
    private val permissionRequest = 102
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(Color.rgb(9, 13, 24))
        }

        val title = TextView(this).apply {
            text = "COCO"
            textSize = 40f
            gravity = Gravity.CENTER
            setTextColor(Color.CYAN)
        }

        status = TextView(this).apply {
            text = "Hi! I'm COCO. Tap the microphone button."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 32, 0, 32)
        }

        val listenButton = Button(this).apply {
            text = "🎙 TALK TO COCO"
            setOnClickListener { startVoiceInput() }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(listenButton)
        setContentView(layout)
    }

    private fun startVoiceInput() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionRequest
            )
            return
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak to COCO"
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault().toLanguageTag()
            )
            startActivityForResult(intent, voiceRequest)
            status.text = "Listening..."
        } catch (e: Exception) {
            status.text = "Voice recognition is unavailable."
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )
        if (requestCode == permissionRequest &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceInput()
        } else if (requestCode == permissionRequest) {
            status.text = "Please allow microphone permission."
        }
    }

    @Deprecated("Uses Android speech recognition activity")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == voiceRequest) {
            if (resultCode == RESULT_OK) {
                val words = data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                val spoken = words?.firstOrNull()

                if (!spoken.isNullOrBlank()) {
                    status.text = "You said: $spoken"
                    if (ttsReady) {
                        tts.speak(
                            "You said $spoken",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "coco_reply"
                        )
                    }
                } else {
                    status.text = "I didn't hear anything. Try again."
                }
            } else {
                status.text = "Try talking to COCO again."
            }
        }
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            ttsReady = true
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
