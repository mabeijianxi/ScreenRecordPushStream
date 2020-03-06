package com.cosmos.rtmprecordpush

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import java.nio.ByteBuffer

/**
 * Created on 2020-03-05.
 * @author jianxi[mabeijianxi@gmail.com]
 */
class MediaCodecWrapper {

    var inputSurface: Surface? = null
    private var encoderThread: HandlerThread? = null
    private var encoder: MediaCodec? = null
    private var hanlder: Handler? = null
    private var bufferInfo: MediaCodec.BufferInfo? = null

    @Volatile
    private var isStopRequested = false
    var codecStateChangeCallback: CodecStateChangeCallback? = null

    @Synchronized
    fun init(width: Int, height: Int) {
        initSurface(width, height)
        initEncodeThread()
    }

    private fun initEncodeThread() {
        encoderThread = HandlerThread("Encoder-Thread")
        encoderThread?.start()
        val looper = encoderThread?.looper
        hanlder = Handler(looper)
    }

    @Synchronized
    fun start() {
        hanlder?.post {
            realStart()
        }
    }

    private fun realStart() {
        val TIMEOUT_USEC = 10000
        var outputDone = false
        while (!outputDone) {
            if (isStopRequested) {
                return
            }

            val decoderStatus = encoder?.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC.toLong())
            if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = encoder?.outputFormat
                if (codecStateChangeCallback != null) {
                    codecStateChangeCallback?.onFormatChange(newFormat)
                }
            } else if ((decoderStatus ?: return) < 0) {
                throw RuntimeException(
                    "unexpected result from decoder.dequeueOutputBuffer: $decoderStatus"
                )
            } else {

                if (bufferInfo?.flags!! and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    outputDone = true
                }

                val doRender = bufferInfo?.size != 0

                if (doRender && codecStateChangeCallback != null) {
                    val outputBuffer = encoder?.getOutputBuffer(decoderStatus)
                    codecStateChangeCallback?.onFrameEncode(bufferInfo, outputBuffer)
                }
                encoder?.releaseOutputBuffer(decoderStatus, doRender)
            }
        }
    }

    @Synchronized
    fun stop() {
        hanlder?.post {
            encoder?.signalEndOfInputStream()
            encoder?.stop()
        }
    }

    @Synchronized
    fun release() {
        encoderThread?.quitSafely()
    }

    private fun initSurface(width: Int, height: Int) {
        bufferInfo = MediaCodec.BufferInfo()
        val format = MediaFormat.createVideoFormat(Configs.videoMime, width, height)

        format.setInteger(MediaFormat.KEY_BIT_RATE, Configs.bitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, Configs.frameRate)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Configs.interval)

        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )

        try {
            encoder = MediaCodec.createEncoderByType(Configs.videoMime)
            encoder?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = encoder?.createInputSurface()
            encoder?.start()
        } catch (e: java.lang.Exception) {
        }
    }

    interface CodecStateChangeCallback {
        fun onFormatChange(newFormat: MediaFormat?)
        fun onFrameEncode(
            bufferInfo: MediaCodec.BufferInfo?,
            outputBuffer: ByteBuffer?
        )
    }
}