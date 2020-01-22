package com.mandarin.bcu.androidutil.io;

import android.graphics.BitmapFactory;

import com.mandarin.bcu.androidutil.battle.sound.SoundHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Queue;
import java.util.function.Function;

import common.CommonStatic;
import common.io.InStream;
import common.io.OutStream;
import common.system.VImg;
import common.system.files.FileData;
import common.system.files.VFile;

public class DefineItf implements CommonStatic.Itf {
    @Override
    public void check(File f) {
        try {
            if (f.isFile()) {
                File g = new File(f.getAbsolutePath());

                if (!g.exists())
                    g.mkdirs();

                if (!f.exists())
                    f.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(File file) {
        if (file.isDirectory())
            for (File g : file.listFiles())
                delete(g);
        else
            file.delete();
    }

    @Override
    public void exit(boolean save) {

    }

    @Override
    public void prog(String str) {

    }

    @Override
    public InStream readBytes(File fi) {
        try {
            byte[] bytes = new byte[(int) fi.length()];

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fi));
            bis.read(bytes, 0, bytes.length);
            bis.close();

            return InStream.getIns(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public VImg readReal(File fi) {
        return new VImg(BitmapFactory.decodeFile(fi.getAbsolutePath()));
    }

    @Override
    public <T> T readSave(String path, Function<Queue<String>, T> func) {
        VFile<? extends FileData> f = VFile.getFile(path);
        Queue<String> qs = f.getData().readLine();
        if (qs != null)
            try {
                T t = func.apply(qs);
                if (t != null)
                    return t;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return func.apply(null);
    }

    @Override
    public void redefine(Class<?> class1) {

    }

    @Override
    public void setSE(int ind) {
        SoundHandler.setSE(ind);
    }

    @Override
    public boolean writeBytes(OutStream os, String path) {
        return true;
    }

    public void init() {
        CommonStatic.def = this;
    }
}