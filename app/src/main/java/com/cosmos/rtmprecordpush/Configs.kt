package com.cosmos.rtmprecordpush

/**
 * Created on 2020-03-06.
 * @author jianxi[mabeijianxi@gmail.com]
 */
object Configs {
    val defaultUrl = "rtmp://192.168.8.108:1939/rtmplive/xxx.flv"
    val width = 480
    val height = 640
    val videoMime = "video/avc"
    val bitRate: Int = 5 shl 20
    val frameRate: Int = 15
    val interval: Int = 10
}