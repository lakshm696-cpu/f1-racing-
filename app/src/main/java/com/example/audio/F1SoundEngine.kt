package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.sin

/**
 * Procedural real-time Formula 1 audio and haptic feedback engine.
 * Synthesizes engine V6 hybrid RPM whine, throttle roar, tire screech, and kerb haptics.
 */
class F1SoundEngine(private val context: Context) {

    private val isRunning = AtomicBoolean(false)
    var isMuted: Boolean = false
    var masterVolume: Float = 0.7f

    // Dynamic audio parameters updated from game loop and Unity CarAudio
    @Volatile var targetRpm: Float = 1200f
    @Volatile var throttleLoad: Float = 0f
    @Volatile var tireScreechIntensity: Float = 0f
    @Volatile var pitchMultiplier: Float = 1.0f  // Modulated by CarAudio.source.pitch
    @Volatile var engineVolume: Float = 0.5f     // Modulated by CarAudio.source.volume

    private var audioTrack: AudioTrack? = null
    private var audioThread: Thread? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun start() {
        if (isRunning.getAndSet(true)) return

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(sampleRate / 10)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        audioThread = thread(name = "F1AudioThread", priority = Thread.MAX_PRIORITY) {
            val shortBuffer = ShortArray(512)
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0
            var noisePhase = 0.0

            while (isRunning.get()) {
                if (isMuted || masterVolume <= 0.01f) {
                    shortBuffer.fill(0)
                    audioTrack?.write(shortBuffer, 0, shortBuffer.size)
                    try { Thread.sleep(15) } catch (_: InterruptedException) { break }
                    continue
                }

                // Map RPM (1200 - 15000) to fundamental engine frequency (80 Hz - 900 Hz) scaled by CarAudio pitchMultiplier
                val rpm = targetRpm.coerceIn(1000f, 15000f)
                val baseFreq = (rpm / 60f) * 3f * pitchMultiplier.coerceIn(0.4f, 3.0f) // V6 4-stroke fires 3 cylinders per revolution
                val f1 = baseFreq.toDouble()
                val f2 = f1 * 1.5 // Turbocharger whine harmonic
                val f3 = f1 * 2.0 // High pitch resonance

                val load = throttleLoad.coerceIn(0f, 1f)
                val screech = tireScreechIntensity.coerceIn(0f, 1f)

                for (i in shortBuffer.indices) {
                    // Update phases
                    phase1 += 2.0 * Math.PI * f1 / sampleRate
                    phase2 += 2.0 * Math.PI * f2 / sampleRate
                    phase3 += 2.0 * Math.PI * f3 / sampleRate
                    if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                    if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                    if (phase3 > 2.0 * Math.PI) phase3 -= 2.0 * Math.PI

                    // Synthesize engine tone with distortion
                    val fundamental = sin(phase1)
                    val turboWhine = sin(phase2) * 0.35
                    val harmonic = sin(phase3) * 0.25

                    var sample = (fundamental + turboWhine + harmonic) * (0.4 + load * 0.6)

                    // Add tire screech noise
                    if (screech > 0.05f) {
                        noisePhase += 0.3
                        val noise = (Math.random() * 2.0 - 1.0) * sin(noisePhase)
                        sample = sample * (1f - screech * 0.5f) + noise * screech * 0.6f
                    }

                    // Soft clip with master and CarAudio engine volume
                    val clipped = sample.coerceIn(-1.0, 1.0)
                    val effectiveVol = masterVolume * engineVolume.coerceIn(0.1f, 1.0f)
                    shortBuffer[i] = (clipped * Short.MAX_VALUE * 0.35f * effectiveVol).toInt().toShort()
                }

                audioTrack?.write(shortBuffer, 0, shortBuffer.size)
            }
        }
    }

    fun stop() {
        isRunning.set(false)
        audioThread?.interrupt()
        audioThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    /**
     * Kerb rumble strip haptic vibration
     */
    fun vibrateKerbRumble() {
        vibrator?.let { vib ->
            if (vib.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(35, 120)
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(35)
                }
            }
        }
    }

    /**
     * Hard brake lockup / crash vibration
     */
    fun vibrateLockup() {
        vibrator?.let { vib ->
            if (vib.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(60, 200)
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(60)
                }
            }
        }
    }
}
