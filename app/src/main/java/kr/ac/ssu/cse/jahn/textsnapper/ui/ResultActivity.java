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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.WriteFile;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import kr.ac.ssu.cse.jahn.textsnapper.R;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.IOCRService;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.IOCRServiceCallback;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.ImageSource;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.OCRProcessor;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.OCRService;
import kr.ac.ssu.cse.jahn.textsnapper.util.PrefUtils;
import kr.ac.ssu.cse.jahn.textsnapper.util.TranslateHelper;
import kr.ac.ssu.cse.jahn.textsnapper.util.Utils;

public class ResultActivity extends AppCompatActivity
{
    private final static String TAG = ResultActivity.class.getSimpleName();
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
        setContentView(R.layout.activity_result);

        /**
         * 내부 객체 참조
         */
        mText = (EditText) findViewById(R.id.resultText);
        mImageView = (ImageView) findViewById(R.id.resultImage);
        ImageView save = (ImageView) findViewById(R.id.save);
        final ImageView translate = (ImageView) findViewById(R.id.translate);
        ImageView cancel = (ImageView) findViewById(R.id.cancel);

        /**
         * 객체에 대한 기본 리스너 설정
         */
        save.setOnTouchListener(Utils.imageTouchEventListener);
        translate.setOnTouchListener(Utils.imageTouchEventListener);
        cancel.setOnTouchListener(Utils.imageTouchEventListener);

        /**
         * 각 객체의 특수 리스너 설정
         */
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 저장버튼 눌렀을 때의 행동
                 */
                String text = mText.getText().toString();
                String path = Utils.getRealPathFromUri(getApplicationContext(), getIntent().getData());
                path = Utils.convertPathToTxt(path);
                Log.d("DEBUG9", path);

                BufferedReader br = null;
                BufferedWriter bw = null;
                try {
                    br = new BufferedReader(new StringReader(text));
                    bw = new BufferedWriter(new FileWriter(path));
                    String buf;

                    while( (buf = br.readLine()) != null ) {
                        bw.write(buf);
                        bw.newLine();
                    }
                    bw.flush();
                    Toast.makeText(getApplicationContext(), "저장 성공!", Toast.LENGTH_LONG).show();
                }catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "저장 실패", Toast.LENGTH_LONG).show();
                    Log.d("DEBUG9", e.toString());
                }finally{
                    Utils.close(br);
                    Utils.close(bw);
                }
            }
        });

        translate.setOnClickListener(new View.OnClickListener() {
            boolean isTranslated = false;
            String originalText = mText.getText().toString();
            /**
             * 번역버튼 눌렀을 때의 행동
             */
            @Override
            public void onClick(View v) {
                String currentText = mText.getText().toString();
                if(!isTranslated) {
                    boolean isEng = PrefUtils.isEng(getApplicationContext());
                    final TranslateHelper translateHelper = TranslateHelper.getInstance(isEng, currentText);
                    originalText = currentText;
                    Runnable translateRunnable = new Runnable() {
                        @Override
                        public void run() {
                            mText.setText(translateHelper.getResultText());
                            translate.setImageResource(R.drawable.undo);
                            isTranslated = true;
                        }
                    };
                    translateHelper.setTranslateRunnable(translateRunnable);
                    translateHelper.start();
                } else {
                    mText.setText(originalText);
                    translate.setImageResource(R.drawable.translate);
                    isTranslated = false;
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Cancel Area 또는 Cancel 버튼 눌렀을 때의 행동
                 */
                finish();
            }
        });

        Intent intent = new Intent(this, OCRService.class);
        //테스트코드
        Log.e(TAG, "Activity started");
        Uri screenshotUri = getIntent().getData();//getParcelableExtra("screenshot");
        if (screenshotUri!=null)
        {
            Picasso.with(this).load(screenshotUri).into(mImageView);
            Log.e(TAG,"image loaded");
        }
        else
            Log.e(TAG,"image not loaded");

        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    protected void start()
    {
        Intent source = getIntent();
        source.putExtra("lang", PrefUtils.getLanguage(this));
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
