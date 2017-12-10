package kr.ac.ssu.cse.jahn.textsnapper.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Created by ArchSlave on 2017-11-05.
 */

public class Utils {
    /**
     * PATH
     */
    public final static String TAG = Utils.class.getSimpleName();
    public static final String APP_PATH = Environment.getExternalStorageDirectory().toString() + "/TextSnapper/";
    public static final String DATA_PATH = APP_PATH + "tessdata/";
    public static final String CAMERA_PATH = APP_PATH + "camera/";
    public static final String EDIT_PATH = APP_PATH + "edit/";


    public final static String EXTERNAL_APP_DIRECTORY = "TextSnapper";
    //오버레이 권한 확인
    public static boolean canDrawOverlays(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(context);
        }
    }

    public static ImageView.OnTouchListener imageTouchEventListener = new ImageView.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView) v;
                    // overlay 색상 설정
                    view.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    view.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    // overlay 색상 제거
                    view.getDrawable().clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return false;
        }
    };

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
                    Log.v("TAG",permission+"권한 요청");
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

    public static File saveScreenShot(Bitmap bitmap) {
        final String imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Screenshots/";
        final String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        final String fileName = "Screenshot_"+timeStamp+".png";
        final String imagePath = imageDir+fileName;
        File dir = new File(imageDir);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(imagePath);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Log.e(TAG,"Screenshot saved at"+imagePath);
        return file;
    }

    public static int getStatusBarHeight(Resources r) {
        int height;

        Resources myResources = r;
        int idStatusBarHeight = myResources.getIdentifier(
                "status_bar_height", "dimen", "android");
        if (idStatusBarHeight > 0) {
            height = r.getDimensionPixelSize(idStatusBarHeight);
        }else{
            height = 0;
        }

        return height;
    }
}
