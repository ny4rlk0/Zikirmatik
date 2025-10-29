package com.ny4rlk0.zikirmatik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ny4rlk0.zikirmatik.ui.theme.ZikirmatikTheme
import android.widget.Button
import android.widget.TextView
import android.os.Vibrator
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

// imports
import android.media.ToneGenerator
import android.media.AudioManager
class MainActivity : ComponentActivity() {
    private val PREFS_NAME = "zikirmatik_prefs"
    private val COUNTER_KEY = "counter_value"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedCounter = prefs.getInt(COUNTER_KEY, 0)

        setContent {
            var counter by remember { mutableStateOf(savedCounter) }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //Kısa ses
                        fun playToneOnce(durationMs: Int = 70) {
                            try {
                                val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 100) // 0..100
                                tg.startTone(ToneGenerator.TONE_PROP_ACK, durationMs)
                                // kısa süre sonra serbest bırak
                                Thread {
                                    Thread.sleep(durationMs.toLong())
                                    tg.release()
                                }.start()
                            } catch (_: Exception) {}
                        }

                        //Titreşim
                        // Buton içinde veya Composable scope'unda:
                        val haptic = LocalHapticFeedback.current
                        val context = LocalContext.current

                        fun vibrateShort(context: Context, durationMs: Long = 15L) {
                            try {
                                val amplitude = VibrationEffect.DEFAULT_AMPLITUDE
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                                    val v = vm?.defaultVibrator
                                    v?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                    v?.vibrate(VibrationEffect.createOneShot(durationMs, 255))
                                } else {
                                    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                    @Suppress("DEPRECATION")
                                    v?.vibrate(durationMs)
                                }
                            } catch (_: Exception) { }
                        }


                        // setContent içinde, Column içinde kullan
                        val configuration = LocalConfiguration.current
                        val screenWidthDp = configuration.screenWidthDp
                        val density = LocalDensity.current
                        val screenWidthPx = with(density) { screenWidthDp.dp.toPx() }

                        val textMeasurer = rememberTextMeasurer()
                        val textString = "%,d".format(counter)

                        // aramaya başlayacağımız font aralığı (min-max)
                        val maxSp = 96f
                        val minSp = 18f

                        // hesaplanan fontSize (sp)
                        val fittedSp by remember(textString, screenWidthPx) {
                            mutableStateOf(run {
                                // binary search ile en büyük uygun sp bul
                                var low = minSp
                                var high = maxSp
                                var best = minSp
                                repeat(8) { // ~8 iterasyon yeterli
                                    val mid = (low + high) / 2f
                                    val style = TextStyle(fontSize = mid.sp)
                                    val layout = textMeasurer.measure(AnnotatedString(textString), style = style)
                                    val measuredWidth = layout.size.width.toFloat()
                                    if (measuredWidth <= screenWidthPx * 0.98f) {
                                        best = mid
                                        low = mid
                                    } else {
                                        high = mid
                                    }
                                }
                                best
                            })
                        }
                        Text(
                            text = textString,
                            fontSize = fittedSp.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                counter = 0
                                prefs.edit().putInt(COUNTER_KEY, counter).apply()
                                playToneOnce(1000)
                                vibrateShort(context, 2000L)
                            },
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(0.25f)
                                .height(64.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "⟳",
                                fontSize = 32.sp,            // daha büyük gösterir
                                fontWeight = FontWeight.Bold,// daha belirgin yapar
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                counter ++
                                if (counter >= 1_000_000) {
                                    counter = 0
                                    playToneOnce(2000)
                                    vibrateShort(context, 30000L)
                                }
                                else if(counter ==99){
                                    //Kısa ses çal
                                    playToneOnce(2000)
                                    vibrateShort(context, 1000L)
                                }
                                else if(counter ==999){
                                    //Kısa ses çal
                                    playToneOnce(4000)
                                    vibrateShort(context, 4000L)
                                }
                                else if(counter ==9999){
                                    //Kısa ses çal
                                    playToneOnce(8000)
                                    vibrateShort(context, 8000L)
                                }
                                else{
                                    //Kısa ses çal
                                    playToneOnce(20)
                                    vibrateShort(context, 33L)
                                }
                                prefs.edit().putInt(COUNTER_KEY, counter).apply()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .aspectRatio(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("")
                        }
                    }
                }
            }
        }
    }
}