package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.WriteFile;

import kr.ac.ssu.cse.jahn.textsnapper.R;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.IOCRService;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.IOCRServiceCallback;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.ImageSource;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.OCRProcessor;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.OCRService;

public class TestActivity extends AppCompatActivity
{
    private final static String TAG = TestActivity.class.getSimpleName();
    protected ImageView mImageView;
    protected EditText mText;
    protected Uri mPhotoUri;
    protected ImageSource mImageSource;
    protected IOCRService mBinder = null;
    protected Pix mFinalPix;
    private boolean _binded = false;


    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.e(TAG, "Service Connected");
            mBinder = IOCRService.Stub.asInterface(service);
            _binded = true;
            start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.e(TAG, "Service disconnected");
            mBinder = null;
            _binded = false;
        }
    };

    IOCRServiceCallback mCallback = new IOCRServiceCallback()
    {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException
        {

        }

        @Override
        public void sendResult(Message msg) throws RemoteException
        {
            switch (msg.what)
            {
            case OCRProcessor.MESSAGE_FINAL_IMAGE:
                long nativePix = (long) msg.obj;
                if (nativePix != 0)
                {
                    mFinalPix = new Pix(nativePix);
                    mImageView.setImageBitmap(WriteFile.writeBitmap(mFinalPix));
                }
                break;

            case OCRProcessor.MESSAGE_UTF8_TEXT:
                mText.setText((String) msg.obj);
                break;

            }
        }

        @Override
        public IBinder asBinder()
        {
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mText = (EditText) findViewById(R.id.editText);
        mImageView = (ImageView) findViewById(R.id.imageView);


        Intent intent = new Intent(this, OCRService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    protected void start()
    {
        Intent source = getIntent();
        source.putExtra("lang", "kor");
        //main action
        try
        {
            mBinder.setCallback(mCallback);
            mBinder.startOCR(source);
        }
        catch (RemoteException e)
        {
            Log.e(TAG, "RemoteException");
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(mConnection);
    }
}