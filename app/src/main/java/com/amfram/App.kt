package com.amfram

import android.app.Application
import android.os.Process
import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import java.io.PrintWriter
import java.io.StringWriter

class App : MultiDexApplication(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stack = sw.toString()
            try {
                openFileOutput("last_crash.log", MODE_PRIVATE).use { it.write(stack.toByteArray()) }
            } catch (_: Exception) {}
            // 让系统默认处理流程终止进程
            Process.killProcess(Process.myPid())
        }
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this).crossfade(true).build()
}