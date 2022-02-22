package com.example.test_ver2_16_night

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.io.File
import android.media.CamcorderProfile
import java.io.FileDescriptor
import java.io.FileInputStream


class ScreenRecordService : Service() {

    //Intentに詰めたデータを受け取る
    var data: Intent? = null
    var code = Activity.RESULT_OK

    //画面録画で使う
    lateinit var mediaRecorder: MediaRecorder
    lateinit var projectionManager: MediaProjectionManager
    lateinit var projection: MediaProjection
    lateinit var virtualDisplay: VirtualDisplay

    //画面の大きさ
    //Pixel 3 XLだとなんかおかしくなる
    var height = 2800
    var width = 1400
    var dpi = 1000

    // 下記に変更
//    var height = 2960
//    var width = 1440
//    var dpi = 560

    override fun onCreate() {
        super.onCreate()
        println("ScreenRecordService:onCreate")

    }

//    service


    override fun onBind(p0: Intent?): IBinder? {
        println("ScreenRecordService:onBind")

        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("ScreenRecordService:onStartCommand")


        //データ受け取る
        data = intent?.getParcelableExtra("data")
        code = intent?.getIntExtra("code", Activity.RESULT_OK) ?: Activity.RESULT_OK

        //画面の大きさ
           height = intent?.getIntExtra("height", 1000) ?: 1000
           width = intent?.getIntExtra("width", 1000) ?: 1000
        dpi = intent?.getIntExtra("dpi", 1000) ?: 1000

        dpi = 5

        //通知を出す。
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //通知チャンネル
        val channelID = "rec_notify"
        //通知チャンネルが存在しないときは登録する
        if (notificationManager.getNotificationChannel(channelID) == null) {
            val channel =
                NotificationChannel(channelID, "録画サービス起動中通知", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        //通知作成
        val notification = Notification.Builder(applicationContext, channelID)
            .setContentText("録画中です。")
            .setContentTitle("画面録画")
//            .setSmallIcon(R.drawable.ic_cast_black_24dp)    //アイコンはベクターアセットから
            .build()

        startForeground(1, notification)

        //録画開始
        startRec()

        return START_NOT_STICKY
    }

    //Service終了と同時に録画終了
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        println("ScreenRecordService:onDestroy")

        super.onDestroy()
        stopRec()
    }

    //録画開始
    @RequiresApi(Build.VERSION_CODES.O)
    fun startRec() {
        println("ScreenRecordService:startRec")

        // 一旦　強制実行する。
        if (data != null) {
            println("ScreenRecordService:startRec:データはnullではありません。")
            println("projectionManagerの設定開始。")
            projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            //codeはActivity.RESULT_OKとかが入る。
            println("projectionの設定開始。")
//        if(data == null) data = Intent()
            projection =
                projectionManager.getMediaProjection(code, data!!)

            mediaRecorder = MediaRecorder()
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)

            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            // エミュレーターの権限のため追加
            // MediaRecorderから開くことができない。
//            var fis: FileInputStream? = null
//            fis = FileInputStream(File(getFilesDir().getPath().toString() + "/sample.3gp"))
//            val fd: FileDescriptor = fis.getFD()
//            mediaRecorder.setOutputFile(fd)

            mediaRecorder.setOutputFile(getFilePath())

//            mediaRecorder.setVideoSize(width, height)
            // ビデオの形式がサポートされていない可能性
            val camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            mediaRecorder.setVideoSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight)

//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            // MPEG_4_SPからH264へ変更
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264) // エンコードはOutPutfileより前に呼ばれる必要がある。

            mediaRecorder.setVideoEncodingBitRate(1080 * 10000) //1080は512ぐらいにしといたほうが小さくできる def 1080 * 10000
//            mediaRecorder.setVideoEncodingBitRate(camcorderProfile.videoBitRate) //1080は512ぐらいにしといたほうが小さくできる def 1080 * 10000
            mediaRecorder.setVideoFrameRate(10)// def 30
//            mediaRecorder.setVideoFrameRate(camcorderProfile.videoBitRate)// def 30
//            mediaRecorder.setAudioSamplingRate(44100)
//            mediaRecorder.setAudioEncodingBitRate(camcorderProfile.audioBitRate)
//            mediaRecorder.setAudioSamplingRate(camcorderProfile.audioSampleRate)


            mediaRecorder.prepare()

            // スクリーンの内容をキャプチャするためにvirtualDisplayを作る。
            virtualDisplay = projection.createVirtualDisplay(
                "recode",
                camcorderProfile.videoFrameWidth,
                camcorderProfile.videoFrameHeight,
//                width,
//                height,
                dpi, // ここのdpiが高すぎる可能性。
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.surface,
                null,
                null
            )
            println("mediaRecorderを実行します。")
            Thread.sleep(1000)
            //開始
            mediaRecorder.start()
        }else{
            println("ScreenRecordService:startRec:データがnullでした！")

        }
        println("レコード完了!")


    }

    //録画止める
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stopRec() {
        println("ScreenRecordService:stopRec")

        println("mediaRecorder.stop")
        println(Build.VERSION.SDK_INT)
        println(mediaRecorder)
        println(mediaRecorder.surface.isValid)
        try {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            mediaRecorder.stop()
        } catch (e:Exception){
            println(e)
            println(e.stackTrace)
        }
        println("mediaRecorder.release")
        mediaRecorder.release()
        println("virtualDisplay.release")
        virtualDisplay.release()
        println("projection.stop")
        projection.stop()
        println("stop完了!")
    }

    //保存先取得。今回は対象範囲別ストレージに保存する
    fun getFilePath(): File {
        println("ScreenRecordService:getFilePath")


        //ScopedStorageで作られるサンドボックスへのぱす
        val scopedStoragePath = getExternalFilesDir(null)
        //写真ファイル作成
        val fileFullPath = "${scopedStoragePath?.path}/${System.currentTimeMillis()}.mp4"
        println("絶対パス")
        println(fileFullPath)

        val file = File(fileFullPath)
        return file
    }

}