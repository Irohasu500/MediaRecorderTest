import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import 'package:video_player/video_player.dart';


void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(primarySwatch: Colors.blue),
      home: Scaffold(
        appBar: AppBar(title: Text('Flash Light Sample')),
        body: Center(
          child: Column(
            children: [
              RaisedButton(
                onPressed: _sart,
                child: Text('start'),
              ),
              RaisedButton(
                onPressed: _stop,
                child: Text('stop'),
              ),
              RaisedButton(
                onPressed: _print_directoly_info,
                child: Text('check'),
              ),
              RaisedButton(
                onPressed: _delete_files,
                child: Text('delete all'),
              ),
              RaisedButton(
                onPressed: _test_view_video,
                child: Text('view moview'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  static const MethodChannel _channel = const MethodChannel('com.example.methodchannel/interop');

  Future<void> _sart() async {
    await _channel.invokeMethod('start');
  }

  Future<void> _stop() async {
    await _channel.invokeMethod('stop');
  }

  void _print_directoly_info() async {
    print("確認します。");
    // final directory = await getApplicationDocumentsDirectory();
    var directory = await getExternalStorageDirectory();



    List<FileSystemEntity> directoryPath = Directory((directory?.path)!).listSync();
    print((directory?.path)!);
    print(directoryPath);
  }

  Future<void> _test_view_video() async {
    var directory = await getExternalStorageDirectory();
    List<FileSystemEntity> directoryPath = Directory((directory?.path)!).listSync();
    print(directoryPath[0].path + 'を再生します。');

    VideoPlayerController _controller = VideoPlayerController.file(File(directoryPath[0].path));
    _controller.initialize().then((_) {
      // 最初のフレームを描画するため初期化後に更新
      setState(() {});
    });
    _controller
        .seekTo(Duration.zero)
        .then((_) => _controller.play());
  }

  Future<void> _delete_files() async {
    print("=======================================");
    print("現在のファイル構成");
    // final directory = await getApplicationDocumentsDirectory();
    var directory = await getExternalStorageDirectory();
    List<FileSystemEntity> directoryPath = Directory((directory?.path)!).listSync();
    print((directory?.path)!);
    print(directoryPath);
    print("削除します");

    for( var file in directoryPath ){
      file.delete(recursive: true);
    }
    print("現在のファイル構成");
    directory = await getExternalStorageDirectory();
    directoryPath = Directory((directory?.path)!).listSync();
    print((directory?.path)!);
    print(directoryPath);
    print("=======================================");
  }


}