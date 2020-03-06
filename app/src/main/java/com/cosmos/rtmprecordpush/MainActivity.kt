package com.cosmos.rtmprecordpush

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private var isStart: Boolean = false
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaCodecWrapper: MediaCodecWrapper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestAllPermissions()
        initViews()
        initEvents()
    }

    private fun initViews() {
        et_url.setText(Configs.defaultUrl)
    }

    private fun requestAllPermissions() {

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
                ), 1001
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "不授权你玩个毛线", Toast.LENGTH_LONG).show()
                    finish()
                    break
                }
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val mp = mediaProjectionManager?.getMediaProjection(resultCode, data)
        mp?.let {
            if (mediaCodecWrapper != null) {
                val surface = (mediaCodecWrapper ?: return).inputSurface
                surface?.let {
                    mp.createVirtualDisplay(
                        "jian",
                        Configs.width,
                        Configs.height,
                        1,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                        surface,
                        null,
                        null
                    )
                    Pusher.instance.openStream(
                        et_url.text.toString(), Configs.width,
                        Configs.height
                    )
                    mediaCodecWrapper?.codecStateChangeCallback = CodecStateChangeImpl()
                    mediaCodecWrapper?.start()
                    isStart = true
                    refreshStartButtomUIState()
                }
            }
        }
    }

    private fun initEvents() {
        bt_start.setOnClickListener {
            if (!isStart) {
                mediaProjectionManager =
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaCodecWrapper = MediaCodecWrapper()
                mediaCodecWrapper?.init(480, 640)
                val targetIntent = mediaProjectionManager?.createScreenCaptureIntent()
                startActivityForResult(targetIntent, 1)
            } else {
                mediaCodecWrapper?.stop()
                mediaCodecWrapper?.release()
                Pusher.instance.closeStream()
                isStart = false
                refreshStartButtomUIState()
            }
        }
    }

    private fun refreshStartButtomUIState() {
        bt_start.text = if (isStart) "点击停止" else "点击开始"
    }

    class CodecStateChangeImpl : MediaCodecWrapper.CodecStateChangeCallback {
        override fun onFrameEncode(bufferInfo: MediaCodec.BufferInfo?, outputBuffer: ByteBuffer?) {
            if (bufferInfo != null && outputBuffer != null) {
                Pusher.instance.sendFrame(bufferInfo, outputBuffer)
            }
        }

        override fun onFormatChange(newFormat: MediaFormat?) {
            newFormat?.let {
                Pusher.instance.setMediaFormat(newFormat)
            }
        }

    }
}
