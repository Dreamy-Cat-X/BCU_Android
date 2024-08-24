package com.yumetsuki.bcu.androidutil.io

import com.yumetsuki.bcu.BuildConfig.VERSION_NAME
import com.yumetsuki.bcu.androidutil.StaticStore
import main.MainBCU
import java.io.File
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MIN_SIZE = Int.MAX_VALUE//Temp size

class Logger(private val f : File) : PrintStream(f) {
    companion object {
        var success = false
        lateinit var logger : Logger
        fun init() {
            try {
                val path = StaticStore.getPublicDirectory() + "logs"
                val folder = File(path)
                if (!folder.exists() && !folder.mkdirs()) {
                    println(folder.absolutePath)
                    return
                }
                println("Funky")
                val log = File(path, SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(Date()) + ".txt")
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
            println("version: $VERSION_NAME")
        flush()
        close()
        if (f.length() <= MIN_SIZE)
            f.deleteOnExit()
    }
}