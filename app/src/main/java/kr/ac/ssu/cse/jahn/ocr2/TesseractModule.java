package kr.ac.ssu.cse.jahn.ocr2;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TesseractModule extends Service
{
    Bitmap image;   //인식에 사용할 이미지
    private TessBaseAPI mTess;
    String datappath="";    //traineddata 폴더 경로
    String lang = "eng";    // 인식할 언어


    public TesseractModule()
    {
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
