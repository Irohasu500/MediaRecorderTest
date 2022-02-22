package com.example.test_ver2_16_night

import io.flutter.embedding.android.FlutterActivity


import android.content.Intent

import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import io.flutter.plugin.common.MethodCall
import io.flutter.embedding.engine.FlutterEngine
import android.util.Log
import androidx.annotation.NonNull

import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.content.Context;

import androidx.core.content.ContextCompat

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import androidx.annotation.RequiresApi
//import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.R
import android.R.attr
import java.lang.Exception
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import android.R.attr.screenDensity
import android.app.*
import android.os.*
import androidx.core.app.NotificationManagerCompat

//import ru.rinekri.servicetest.utils.showToast



class MainActivity: FlutterActivity() {
    companion object {
        private const val CHANNEL = "com.example.methodchannel/interop"
        private const val METHOD_GET_LIST = "getList"
        private const val METHOD_REC_START = "start"
        private const val METHOD_REC_STOP = "stop"
        private const val METHOD_TOP_PRESET = "topPreset"
    }
    private lateinit var channel: MethodChannel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        println("MainActivity:configureFlutterEngine")

        // ここのエラーは無視してよい。
        GeneratedPluginRegistrant.registerWith(flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler { methodCall: MethodCall, result: MethodChannel.Result ->
            if (methodCall.method == METHOD_GET_LIST) {
                val name = methodCall.argument<String>("name").toString()
                val age = methodCall.argument<Int>("age")
                Log.d("Android", "name = ${name}, age = $age")

                val list = listOf("data0", "data1", "data2")
                result.success(list)
            } else if(methodCall.method == METHOD_REC_START) {
                println("start")
                start_button()
            }else if(methodCall.method == METHOD_REC_STOP) {
                println("stop")
                stop_button()
            }else if(methodCall.method == METHOD_TOP_PRESET) {
                println("flutter:log:top_preset")
            }
            else
                result.notImplemented()
        }
    }

    //リクエストの結果
    val code = 513
//    val code = 512
    val permissionCode = 8100
    lateinit var projectionManager: MediaProjectionManager

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        println("MainActivity:onCreate")
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    }
    private val REQUEST_CODE_SCREEN_CAPTURE = 1

    // 下記を実行。
//    https://suihan74.github.io/posts/2021/02_25_00_relace_start_activity_for_result/
    @RequiresApi(Build.VERSION_CODES.M)
    fun start_button() {
        println("MainActivity:start_button")

        //その前にマイクへアクセスしていいか尋ねる
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            println("MainActivity:start_button:マイクへアクセス不可能")
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE), permissionCode)
//            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), permissionCode) // こちらから変更
        } else {
            println("MainActivity:start_button:マイクへアクセス可能")
            //マイクの権限があるので画面録画リクエスト
            //ダイアログを出す
            val permissionIntent: Intent = projectionManager.createScreenCaptureIntent()
            startActivityForResult(permissionIntent, code)

//            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE)
//            startService(projectionManager.createScreenCaptureIntent())
            println("ended!") // 発見！どうやら、実行はされているらしい。startが終了している。
        }

        fun check_permissions(): Boolean {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
        }

        // -------------------------------- まるごと追加 -------------------------
        //Service起動
        //Manifestに「android:foregroundServiceType="mediaProjection"」を付け足しておく
//        val intent = Intent(this, ScreenRecordService::class.java)
//        intent.putExtra("code", resultCode) //必要なのは結果。startActivityForResultのrequestCodeではない。
//        intent.putExtra("data", data)
        //画面の大きさも一緒に入れる
//        val metrics = resources.displayMetrics;
//        intent.putExtra("height", metrics.heightPixels)
//        intent.putExtra("width", metrics.widthPixels)
//        intent.putExtra("dpi", metrics.densityDpi)
//
//        startForegroundService(intent)
        // -------------------------------- まるごと追加 -------------------------


    }

    fun stop_button() {
        println("MainActivity:stop_button")
        val intent = Intent(this, ScreenRecordService::class.java)
        stopService(intent)
    }

    //画面録画の合否を受け取る
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("MainActivity:onActivityResult")
        println(resultCode)
        println(requestCode)

        //成功＋結果が画面録画の物か
        if (resultCode == Activity.RESULT_OK && requestCode == code) {
            //Service起動
            //Manifestに「android:foregroundServiceType="mediaProjection"」を付け足しておく
            val intent = Intent(this, ScreenRecordService::class.java)
            intent.putExtra("code", resultCode) //必要なのは結果。startActivityForResultのrequestCodeではない。
            intent.putExtra("data", data)
            //画面の大きさも一緒に入れる
            val metrics = resources.displayMetrics;
            intent.putExtra("height", metrics.heightPixels)
            intent.putExtra("width", metrics.widthPixels)
            intent.putExtra("dpi", metrics.densityDpi)

            startForegroundService(intent)
        }
    }

    //権限の結果受け取る
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        println("MainActivity:onRequestPermissionsResult")

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            //マイクへアクセス権げっと
            Toast.makeText(this, "権限が付与されました。", Toast.LENGTH_SHORT).show()
        }else {
            println("権限は付与されませんでした。")
        }
    }

}

