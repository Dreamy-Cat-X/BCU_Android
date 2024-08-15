package com.yumetsuki.bcu.androidutil.io

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.yumetsuki.bcu.androidutil.StaticStore
import main.MainBCU
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ErrorLogWriter(private val path: String?) : Thread.UncaughtExceptionHandler {
    private val errors: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (path != null)
            writeToFile(e)

        errors?.uncaughtException(t, e)
    }

    private fun writeToFile(e: Throwable) {
        try {
            if(path == null)
                return

            val f = File(path)
            if (!f.exists()) {
                f.mkdirs()
            }
            val fe = File(MainBCU.getPublicDirectory() + "/logs")
            if (!fe.exists()) fe.mkdirs()
            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
            val date = Date()
            var name = dateFormat.format(date)
            val stringbuff: Writer = StringWriter()
            val printWriter = PrintWriter(stringbuff)
            e.printStackTrace(printWriter)
            val current = stringbuff.toString()
            printWriter.close()

            val dname = name + "_" + Build.MODEL + ".txt"
            val df = File(MainBCU.getPublicDirectory() + "/logs/", dname)
            val dfileWriter = FileWriter(df)
            dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
            dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
            dfileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
            dfileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
            dfileWriter.append(current)
            dfileWriter.flush()
            dfileWriter.close()

            name += ".txt"
            val file = File(path, name)
            if (!file.exists()) f.createNewFile()
            val fileWriter = FileWriter(file)
            fileWriter.append(current)
            fileWriter.flush()
            fileWriter.close()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    companion object {
        fun generateMessage(msg: String, obj: Any?) : String {
            val result = StringBuilder(msg)

            when (obj) {
                is Array<*> -> {
                    result.append(obj.contentDeepToString())
                }
                is IntArray -> {
                    result.append(obj.contentToString())
                }
                null -> {
                    result.append("null")
                }
            }

            return result.toString()
        }

        fun writeDriveLog(e: Exception) {
            try {
                val path = File(MainBCU.getPublicDirectory() + "/logs")

                if(!path.exists() && !path.mkdirs()) {
                    Log.e("ErrorLogWriter", "Failed to create folder "+path.absolutePath)
                    return
                }

                val stringbuff: Writer = StringWriter()
                val printWriter = PrintWriter(stringbuff)

                e.printStackTrace(printWriter)

                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val date = Date()
                val name = dateFormat.format(date) + "_" + Build.MODEL + ".txt"

                val df = File(MainBCU.getPublicDirectory() + "/logs/", name)
                if (!df.exists()) df.createNewFile()
                val dfileWriter = FileWriter(df)
                dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
                dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
                dfileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
                dfileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
                dfileWriter.append(stringbuff.toString())
                dfileWriter.flush()
                dfileWriter.close()
                printWriter.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun writeLog(error: Exception, upload: Boolean, c: Context) {
            error.printStackTrace()

            try {
                val path = StaticStore.getExternalPath(c)+"logs/"
                val f = File(path)
                if (!f.exists()) {
                    f.mkdirs()
                }
                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val date = Date()
                val name = dateFormat.format(date) + ".txt"
                var file = File(path, name)
                val stringbuff: Writer = StringWriter()
                val printWriter = PrintWriter(stringbuff)
                error.printStackTrace(printWriter)
                if (!file.exists()) file.createNewFile() else {
                    file = File(path, getExistingFileName(path, name))
                    file.createNewFile()
                }
                if (upload) {
                    val dname = dateFormat.format(date) + "_" + Build.MODEL + ".txt"
                    val df = File(MainBCU.getPublicDirectory() + "/logs", dname)
                    if (!df.exists()) df.createNewFile()
                    val dfileWriter = FileWriter(df)
                    dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
                    dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
                    dfileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
                    dfileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
                    dfileWriter.append(stringbuff.toString())
                    dfileWriter.flush()
                    dfileWriter.close()
                }
                val fileWriter = FileWriter(file)
                fileWriter.append(stringbuff.toString())
                fileWriter.flush()
                fileWriter.close()
                printWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun writeLog(error: Exception, msg: String, upload: Boolean, c: Context) {
            try {
                val path = StaticStore.getExternalLog(c)+"logs/"
                val f = File(path)
                if (!f.exists()) {
                    f.mkdirs()
                }
                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val date = Date()
                val name = dateFormat.format(date) + ".txt"
                var file = File(path, name)
                val stringbuff: Writer = StringWriter()
                val printWriter = PrintWriter(stringbuff)
                error.printStackTrace(printWriter)
                if (!file.exists())
                    file.createNewFile()
                else {
                    file = File(path, getExistingFileName(path, name))
                    file.createNewFile()
                }
                if (upload) {
                    val dname = dateFormat.format(date) + "_" + Build.MODEL + ".txt"
                    val df = File(MainBCU.getPublicDirectory() + "/logs", dname)//Environment.getDataDirectory().toString() + "/data/com.yumetsuki.bcu/upload/"
                    if (!df.exists()) df.createNewFile()
                    val dfileWriter = FileWriter(df)
                    dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
                    dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
                    dfileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
                    dfileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
                    dfileWriter.append("Message : ").append(msg).append("\r\n\r\n")
                    dfileWriter.append(stringbuff.toString())
                    dfileWriter.flush()
                    dfileWriter.close()
                }
                val fileWriter = FileWriter(file)
                fileWriter.append("Message : ").append(msg).append("\r\n\r\n")
                fileWriter.append(stringbuff.toString())
                fileWriter.flush()
                fileWriter.close()
                printWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun getExistingFileName(path: String, name: String): String {
            var decided = false
            var exist = 1
            var nam = "$name-$exist"
            while (!decided) {
                val f = File(path, nam)
                nam = if (!f.exists()) return nam else {
                    exist++
                    "$name-$exist"
                }
                decided = true
            }
            return nam
        }
    }
}