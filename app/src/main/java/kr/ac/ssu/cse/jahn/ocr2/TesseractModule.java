package kr.ac.ssu.cse.jahn.ocr2;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TesseractModule extends Service
{
    private TessBaseAPI mTess;  //OCR 엔진 객체
    Bitmap image;   //인식에 사용할 이미지
    String datappath="";    //traineddata 폴더 경로
    String lang = "eng";    // 인식할 언어


    public TesseractModule()
    {
    }
    private void processCommand(Intent intent)
    {

        try
        {
            Uri photoUri = (Uri)intent.getParcelableExtra("photouri");
            if (photoUri==null)
                throw new Exception("can't get parcelable from intent");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("ERROR",e.getMessage());
        }
    }
    private void preprocess(Bitmap bitmap)
    {

    }
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent==null)
            return Service.START_NOT_STICKY;
        else
            processCommand(intent);
           return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
