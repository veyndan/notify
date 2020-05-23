package com.veyndan.notify

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.FileObserver
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import timber.log.Timber
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey

class MainActivity : AppCompatActivity() {

    private val fileObserver = object : FileObserver("/data/user/0/com.veyndan.notify/files/_todo/") {
        override fun onEvent(event: Int, path: String?) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            when (event and FileObserver.ALL_EVENTS) {
                FileObserver.ACCESS -> {
                    Timber.d("       ACCESS $path")
                }
                FileObserver.ATTRIB -> {
                    Timber.d("       ATTRIB $path")
                }
                FileObserver.CLOSE_NOWRITE -> {
                    Timber.d("CLOSE_NOWRITE $path")
                }
                FileObserver.CLOSE_WRITE -> {
                    // TODO This should be the place that the contents of the file is written to,
                    // but apparently the abstract pathname doesn't exist i.e. the file isn't
                    // available at this point. This is the last event of the FileObserver events.
                    // Where else to do it? Or am I missing something?
                    Timber.d("  CLOSE_WRITE $path")

                    val file = File("${this@MainActivity.filesDir.path}/_todo/$path")

                    Timber.d("$file")
                    Timber.d("${file.exists()}")
                }
                FileObserver.CREATE -> {
                    Timber.d("       CREATE $path")

                    val notification = NotificationCompat.Builder(this@MainActivity)
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle(path)
//                            .setContentText(file.readText())
                            .build()

                    notificationManager.notify(path!!.hashCode(), notification)
                }
                FileObserver.DELETE -> {
                    Timber.d("       DELETE $path")

                    notificationManager.cancel(path!!.hashCode())
                }
                FileObserver.DELETE_SELF -> {
                    Timber.d("  DELETE_SELF $path")
                }
                FileObserver.MODIFY -> {
                    Timber.d("       MODIFY $path")
                }
                FileObserver.MOVED_FROM -> {
                    Timber.d("   MOVED_FROM $path")
                }
                FileObserver.MOVED_TO -> {
                    Timber.d("     MOVED_TO $path")
                }
                FileObserver.MOVE_SELF -> {
                    Timber.d("    MOVE_SELF $path")
                }
                FileObserver.OPEN -> {
                    Timber.d("         OPEN $path")
                }
                else -> {
                    Timber.v("unknown event with event=$event path=$path")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        fileObserver.startWatching()

        val watchService = FileSystems.getDefault().newWatchService()

        val path = Paths.get("/data/user/0/com.veyndan.notify/files/_todo")

        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE)

        var key = watchService.take()
        while (key != null) {
            for (event in key.pollEvents()) {
                Timber.d("Event kind:${event.kind()} + File affected: ${event.context()}.")
            }
            key.reset()
            key = watchService.take()
        }
    }
}
