package com.mandarin.bcu.androidutil.io.asynchs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.ApkDownload;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class CheckApk extends AsyncTask<Void, String, Void> {
    private final WeakReference<Activity> weakReference;
    private String thisver;
    private boolean cando;
    private String path;
    private boolean lang;
    private ArrayList<String> fileneed = new ArrayList<>();
    private ArrayList<String> filenum = new ArrayList<>();
    private boolean contin = true;
    private boolean config = false;

    public CheckApk(String path, boolean lang, Activity context, boolean cando) {
        this.weakReference = new WeakReference<>(context);
        this.path = path;
        this.lang = lang;
        this.cando = cando;
    }

    public CheckApk(String path, boolean lang, Activity context, boolean cando, boolean config) {
        this.weakReference = new WeakReference<>(context);
        this.path = path;
        this.lang = lang;
        this.cando = cando;
        this.config = config;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if (activity == null) return;

        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            thisver = packageInfo.versionName;
            StaticStore.VER = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ProgressBar prog = activity.findViewById(R.id.mainprogup);
        Button retry = activity.findViewById(R.id.checkupretry);

        retry.setVisibility(View.GONE);
        prog.setVisibility(View.VISIBLE);
        TextView state = activity.findViewById(R.id.mainstup);
        state.setText(R.string.main_check_apk);

    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if (activity == null) return null;

        try {
            String apklink = "https://raw.githubusercontent.com/battlecatsultimate/bcu-page/master/api/getUpdate.json";
            URL apkurl = new URL(apklink);

            InputStream in = apkurl.openStream();
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = isr.read()) != -1) {
                sb.append((char) cp);
            }
            String result = sb.toString();
            JSONObject ans = new JSONObject(result);
            in.close();

            SharedPreferences shared = activity.getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE);
            String thatver;

            if (shared.getBoolean("apktest", false)) {
                thatver = ans.getString("android_test");
            } else {
                thatver = ans.getString("android_ver");
            }

            publishProgress(thatver);


        } catch (JSONException e) {
            e.printStackTrace();
            contin = false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            contin = false;
        } catch (IOException e) {
            e.printStackTrace();
            contin = false;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        goToApk(values[0]);
    }

    @Override
    protected void onPostExecute(Void results) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        if (!contin) {
            if (cando)
                if (!config)
                    new CheckUpdates(path, lang, fileneed, filenum, activity, true).execute();
                else
                    new CheckUpdates(path, lang, fileneed, filenum, activity, true, true).execute();
            else {
                ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);

                if (connectivityManager.getActiveNetworkInfo() == null) {
                    Button checkup = activity.findViewById(R.id.checkupretry);
                    ProgressBar prog = activity.findViewById(R.id.mainprogup);
                    TextView mainstup = activity.findViewById(R.id.mainstup);

                    checkup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (connectivityManager.getActiveNetworkInfo() != null)
                                new CheckApk(path, lang, weakReference.get(), cando).execute();
                            else
                                Toast.makeText(activity, R.string.needconnect, Toast.LENGTH_SHORT).show();
                        }
                    });

                    prog.setVisibility(View.GONE);
                    mainstup.setText(R.string.main_internet_no);
                } else {
                    new CheckApk(path, lang, weakReference.get(), cando).execute();
                }
            }
        }
    }

    private void goToApk(String ver) {
        Activity activity = weakReference.get();

        String[] thisnum = thisver.split("\\.");
        String[] thatnum = ver.split("\\.");

        boolean update = check(thisnum, thatnum);

        if (update) {
            AlertDialog.Builder apkdon = new AlertDialog.Builder(activity);
            apkdon.setCancelable(false);
            apkdon.setTitle(R.string.apk_down_title);
            String content = activity.getString(R.string.apk_down_content) + ver;
            apkdon.setMessage(content);
            apkdon.setPositiveButton(R.string.main_file_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent result = new Intent(activity, ApkDownload.class);
                    result.putExtra("ver", ver);
                    activity.startActivity(result);
                    activity.finish();
                }
            });
            apkdon.setNegativeButton(R.string.main_file_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!config)
                        new CheckUpdates(path, lang, fileneed, filenum, activity, cando).execute();
                    else
                        new CheckUpdates(path, lang, fileneed, filenum, activity, cando, true).execute();
                }
            });

            AlertDialog apkdown = apkdon.create();
            apkdown.show();
        } else {
            if (!config)
                new CheckUpdates(path, lang, fileneed, filenum, activity, cando).execute();
            else
                new CheckUpdates(path, lang, fileneed, filenum, activity, cando, true).execute();
        }
    }

    public boolean check(String[] thisnum, String[] thatnum) {
        boolean update = false;

        int[] these = {Integer.parseInt(thisnum[0]), Integer.parseInt(thisnum[1]), Integer.parseInt(thisnum[2])};
        int[] those = {Integer.parseInt(thatnum[0]), Integer.parseInt(thatnum[1]), Integer.parseInt(thatnum[2])};

        if (these[0] < those[0])
            update = true;
        else if (these[0] == those[0] && these[1] < those[1])
            update = true;
        else if (these[0] == those[0] && these[1] == those[1] && these[2] < those[2])
            update = true;

        return update;
    }
}