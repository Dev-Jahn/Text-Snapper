package kr.ac.ssu.cse.jahn.ocr2;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.googlecode.leptonica.android.Pix;

public class OCRService extends Service
{
    public final static String EXTRA_NATIVE_PIX = "pix_pointer";
    private final static String TAG = OCRService.class.getSimpleName();

    private Bitmap image;
    private String datappath="";
    private String lang = "eng";
    private boolean mReceiverRegistered;
    private Uri photoUri;
    private OCRProcessor mProcessor;
    private Messenger mMessenger = new Messenger(new ProgressHandler());
    private AsyncTask<Void, Void, ImageLoadAsyncTask.LoadResult> mImageLoadTask;

    public OCRService()
    {
    }

    @Override
    public void onCreate()
    {
        mProcessor = new OCRProcessor(mMessenger);
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

    private void processCommand(Intent intent)
    {

        photoUri = (Uri)intent.getParcelableExtra("photouri");
        if (photoUri==null)
            Log.e(TAG,"can't get parcelable from intent");
        if (mImageLoadTask!=null)
            mImageLoadTask.cancel(true);
        final boolean skipCrop = intent.getSerializableExtra("imagesource")==ImageSource.CROP;

        registerImageLoaderReceiver();
        mImageLoadTask = new ImageLoadAsyncTask(this, skipCrop, photoUri);
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (mReceiverRegistered)
            {
                Log.i(TAG, "onReceive " + OCRService.this);
                if (intent.getAction().equalsIgnoreCase(ImageLoadAsyncTask.ACTION_IMAGE_LOADED))
                {
                    unRegisterImageLoadedReceiver();
                    final long nativePix = intent.getLongExtra(ImageLoadAsyncTask.EXTRA_PIX, 0);
                    final int statusNumber = intent.getIntExtra(ImageLoadAsyncTask.EXTRA_STATUS, ImageLoadAsyncTask.PixLoadStatus.SUCCESS.ordinal());
                    final boolean skipCrop = intent.getBooleanExtra(ImageLoadAsyncTask.EXTRA_SKIP_CROP, false);
                    startOCRProcessor(nativePix, ImageLoadAsyncTask.PixLoadStatus.values()[statusNumber], skipCrop);
                }
            }
        }
    };

    private synchronized void unRegisterImageLoadedReceiver()
    {
        if (mReceiverRegistered)
        {
            Log.i(TAG, "unRegisterImageLoadedReceiver " + mMessageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            mReceiverRegistered = false;
        }
    }
    
    private synchronized void registerImageLoaderReceiver()
    {
        if (!mReceiverRegistered)
        {
            Log.i(TAG, "registerImageLoaderReceiver " + mMessageReceiver);
            final IntentFilter intentFilter = new IntentFilter(ImageLoadAsyncTask.ACTION_IMAGE_LOADED);
            intentFilter.addAction(ImageLoadAsyncTask.ACTION_IMAGE_LOADING_START);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
            mReceiverRegistered = true;
        }
    }

    private void startOCRProcessor(long nativePix, ImageLoadAsyncTask.PixLoadStatus pixLoadStatus, boolean skipCrop)
    {
        if (pixLoadStatus == ImageLoadAsyncTask.PixLoadStatus.SUCCESS)
        {
            if (skipCrop)
            {
                final Pix pix = new Pix(nativePix);
                mProcessor.doOCR(getApplicationContext(), lang, pix);
            }
            else
            {
                Intent actionIntent = new Intent(this, CropActivity.class);
                actionIntent.putExtra(EXTRA_NATIVE_PIX, nativePix);
                //결과 확인 불가하므로 액티비티 에서 종료시 브로드캐스트 송신
                startActivity(actionIntent);
            }
        }
        else
            showFileError(pixLoadStatus);
    }

    private void preprocess(Bitmap bitmap)
    {

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
