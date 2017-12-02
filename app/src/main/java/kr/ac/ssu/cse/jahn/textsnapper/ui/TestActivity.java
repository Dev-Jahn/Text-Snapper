package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import kr.ac.ssu.cse.jahn.textsnapper.R;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.ImageSource;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.OCRService;

public class TestActivity extends AppCompatActivity
{
    private final static String TAG = TestActivity.class.getSimpleName();
    protected ImageView mImageView;
    protected EditText mText;
    protected Uri mPhotoUri;
    protected ImageSource mImageSource;

    private boolean _Connected=false;
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.e(TAG, "onServiceConnected");
            _Connected=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.e(TAG, "onServiceDisconnected");
            _Connected=false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mText = (EditText) findViewById(R.id.editText);
        mImageView = (ImageView)findViewById(R.id.imageView);

        Intent caller = getIntent();
        mPhotoUri = caller.getData();
        mImageSource = (ImageSource)caller.getSerializableExtra("imagesource");

        Intent intent = new Intent(this, OCRService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        if (_Connected)
        {
            intent.setDataAndType(mPhotoUri,"image/*");
            intent.putExtra("imagesource", mImageSource);
            startService(intent);
        }
    }

    @Override
    protected void onDestroy()
    {
        unbindService(mConnection);
        super.onDestroy();
    }
}
