package com.yumetsuki.bcu.androidutil.io

import com.yumetsuki.bcu.BuildConfig
import main.MainBCU
import java.io.File
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MIN_SIZE = Int.MAX_VALUE//Temp size

class Logger(private val f : File) : PrintStream(f) {
    companion object {
        var success = false
        lateinit var logger : Logger
        fun init() {
            try {
                val path = MainBCU.getPublicDirectory() + "/logs";
                val folder = File(path)
                if (!folder.exists())
                    folder.mkdirs()

                val log = File(
                    path,
                    "/" + SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(Date()) + ".txt"
                )
                if (!log.exists())
                    log.createNewFile()

                logger = Logger(log)
                logger.logSetup()
                success = true
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logSetup() {
        if (MainBCU.WRITE)
            System.setOut(this)
    }

    fun logClose() {
        if (f.length() > MIN_SIZE)
            println("version: " + BuildConfig.VERSION_NAME)
        flush()
        close()
        if (f.length() <= MIN_SIZE)
            f.deleteOnExit()
    }
}