package kr.ac.ssu.cse.jahn.textsnapper.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ArchSlave on 2017-11-05.
 */

public class Utils {
    public final static String TAG = Utils.class.getSimpleName();
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TextSnapper/";

    //오버레이 권한 확인
    public static boolean canDrawOverlays(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(context);
        }
    }

    public static void request(Activity context, String... permissions)
    {
        //모든 권한이 있는지 확인
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for(String permission:permissions)
        {
            if(ContextCompat.checkSelfPermission(context, permission)==PackageManager.PERMISSION_GRANTED)
                Log.v("TAG",permission+"권한 있음");
            else
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(context, permission))
                {
                    Log.v("TAG", permission + "권한 설명필요");
                }
                else
                {
                    ActivityCompat.requestPermissions(context, permissions, 1);
                    Log.v("TAG",permission+"권한 있음");
                }
            }
        }
    }

    public static boolean makeAppDir()
    {

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/", DATA_PATH + "photo/" };
        for (String path : paths)
        {
            File dir = new File(path);
            if (!dir.exists())
            {
                if (!dir.mkdirs())
                {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return false;
                }
                else
                    Log.v(TAG, "Created directory " + path + " on sdcard");
            }
        }
        return true;
    }

    public static void copyTessdata(final AssetManager assetManager, String... langs)
    {
        final boolean allcopied = true;
        for (final String lang:langs)
        {
            if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists())
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                            //GZIPInputStream gin = new GZIPInputStream(in);
                            OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/" + lang + ".traineddata");

                            // Transfer bytes from in to out
                            byte[] buf = new byte[1024];
                            int len;
                            //while ((lenf = gin.read(buff)) > 0)
                            // {
                            while ((len = in.read(buf)) > 0)
                            {
                                out.write(buf, 0, len);
                            }
                            in.close();
                            //gin.close();
                            out.close();

                            Log.v(TAG, "Copied " + lang + " traineddata");

                        }catch (IOException e)
                        {
                            Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
                        }
                    }
                }).start();
            }
        }
    }

    public static String getTessDir(final Context appContext) {
        String tessDir = PrefUtils.getTessDir(appContext);
        if (tessDir == null) {
            return new File(Environment.getExternalStorageDirectory(), EXTERNAL_APP_DIRECTORY).getPath() + "/";
        } else {
            return tessDir;
        }
    }
}
