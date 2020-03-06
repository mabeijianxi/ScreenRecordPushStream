# ScreenRecordPushStream
这是一个基于 rtmp 协议的 Android 录屏推流demo ([Demo APK 下载](https://download.immomo.com/android/temp_84d939jh3u89j2_k1j2k9/app-release_57031.apk))


## 使用方法

##### 一、 服务器搭建
玩玩可以使用这个[https://github.com/illuspas/Node-Media-Server](https://github.com/illuspas/Node-Media-Server)，Node 的库自己玩玩行，用于生产差点意思。
```
mkdir nms
cd nms
npm install node-media-server
vi app.js
```
修改下 app.js里面的配置，改下 rtmp的端口就行了，你也可以使用它默认的1935
```
const NodeMediaServer = require('node-media-server');

const config = {
  rtmp: {
    port: 1935,
    chunk_size: 60000,
    gop_cache: true,
    ping: 30,
    ping_timeout: 60
  },
  http: {
    port: 8000,
    allow_origin: '*'
  }
};

var nms = new NodeMediaServer(config)
nms.run();
```
运行

```
node app.js
```

##### 二、安装 FFmpeg 和 FFPlay
安装这两个的目的呢是可以很方便的测试，利用 FFmpeg 推流，FFPlay 播放

以mac 系统为例:

* FFmpeg

```
brew install ffmpeg
```

* FFPlay

```
brew install ffmpeg --with-ffplay
```

##### 三、测试

运行 node app.js 成功后运行如下命令：

* 推流
```
ffmpeg -re -i https://github.com/mabeijianxi/ScreenRecordPushStream/raw/master/test_source/xxx.mp4  -c copy -f flv rtmp://localhost:1935/rtmplive/xxx.flv
```

* 拉流
```
ffplay rtmp://localhost:1935/rtmplive/xxx.flv
```
依次执行完成后将弹出 FFplay 的视频播放页面,里面会有一只很可爱的狗狗哈哈。

##### 四、安卓客户端上推流

demo 用了 [https://github.com/lakeinchina/librestreaming](https://github.com/lakeinchina/librestreaming)中的RTMP的代码，运行demo以后输入相应地址，本地 IP 用 ifconfig 获取下即可。

<video src="https://github.com/mabeijianxi/pic-trusteeship/raw/master/pic/record_v.mp4" width="681px" height="391px" controls="controls"></video>
![demo_pic](https://github.com/mabeijianxi/pic-trusteeship/raw/master/pic/image.png)


[点击观看视频效果](https://github.com/mabeijianxi/pic-trusteeship/raw/master/pic/record_v.mp4)




## 后期计划
可以发现node-media-server 中加入了些buffer，在拉流的时候明显能感觉到延迟。后期可能会移植live555到手机端上搭建服务，这样在客户端只需要一个拉流播放器就可以了，比较便捷。

