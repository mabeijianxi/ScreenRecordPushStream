package com.cosmos.rtmprecordpush

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import com.cry.cry.rtmp.rtmp.RESRtmpSender
import java.nio.ByteBuffer

/**
 * Created on 2020-03-04.
 * @author jianxi[mabeijianxi@gmail.com]
 */
class Pusher private constructor() {
    private var handler: Handler? = null
    private var resRtmpSender: RESRtmpSender? = null
    private var handlerThread: HandlerThread? = null

    companion object {
        val instance: Pusher by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { Pusher() }
    }

    fun openStream(url: String, width: Int, height: Int) {
        init()
        handler?.post {
            resRtmpSender?.rtmpOpen(url, width, height)
        }
    }

    fun closeStream() {
        handler?.post {
            resRtmpSender?.rtmpClose()
            handlerThread?.quit()
        }
    }

    fun setMediaFormat(format: MediaFormat) {
        handler?.post {
            resRtmpSender?.rtmpSendFormat(format)
        }
    }

    fun sendFrame(info: MediaCodec.BufferInfo, outputBuffer: ByteBuffer) {
        val realData = resRtmpSender?.getRealData(info, outputBuffer)

        handler?.post {
            resRtmpSender?.rtmpPublish(realData)
        }
    }


    private fun init() {
        resRtmpSender = RESRtmpSender()
        handlerThread = HandlerThread("push-stream")
        handlerThread?.start()
        handler = Handler((handlerThread ?: return).looper)
    }
}