package kr.ac.ssu.cse.jahn.textsnapper.ocr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Scale;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import kr.ac.ssu.cse.jahn.textsnapper.ui.MyApplication;

import static com.googlecode.leptonica.android.ReadFile.readBitmap;
/*
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.Scale;*/

/**
 * Created by CypressRH on 2017-11-16.
 * Uri를 받아 이미지파일을 Pix타입으로 로드하기 위한 AsyncTask
 */

public class ImageLoadAsyncTask extends AsyncTask<Void, Void, ImageLoadAsyncTask.LoadResult>
{
    public enum PixLoadStatus
    {
        IMAGE_FORMAT_UNSUPPORTED, IMAGE_NOT_32_BIT, IMAGE_COULD_NOT_BE_READ, MEDIA_STORE_RETURNED_NULL, IMAGE_DOES_NOT_EXIST, SUCCESS, IO_ERROR, CAMERA_APP_NOT_FOUND, CAMERA_APP_ERROR, CAMERA_NO_IMAGE_RETURNED
    }
    class LoadResult
    {
        private final Pix mPix;
        private final PixLoadStatus mStatus;

        LoadResult(PixLoadStatus status)
        {
            mStatus = status;
            mPix = null;
        }
        LoadResult(Pix p)
        {
            mStatus = PixLoadStatus.SUCCESS;
            mPix = p;
        }
    }
    final static String EXTRA_PIX = "pix";
    final static String EXTRA_STATUS = "status";
    final static String EXTRA_SKIP_CROP = "skip_crop";
    final static String ACTION_IMAGE_LOADED = ImageLoadAsyncTask.class.getName() + ".image.loaded";
    final static String ACTION_IMAGE_LOADING_START = ImageLoadAsyncTask.class.getName() + ".image.loading.startOCR";
    private static final int MIN_PIXEL_COUNT = 3 * 1024 * 1024;
    private final boolean skipCrop;
    private final Context mContext;
    private final Uri photoUri;
    private static final String TAG = ImageLoadAsyncTask.class.getSimpleName();

    ImageLoadAsyncTask(Context context, boolean skipCrop, Uri uri) {
        mContext = context;
        this.skipCrop = skipCrop;
        this.photoUri = uri;
    }

    /**
     * 이미지 로드가 시작됨을 브로드캐스트
     */
    @Override
    protected void onPreExecute()
    {
        Log.i(TAG, "onPreExecute");
        Intent intent = new Intent(ACTION_IMAGE_LOADING_START);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * 이미지로드가 완료됨을 브로드캐스트
     */
    @Override
    protected void onPostExecute(LoadResult result)
    {
        Log.i(TAG, "onPostExecute");
        Intent intent = new Intent(ACTION_IMAGE_LOADED);
        if (result.mStatus == PixLoadStatus.SUCCESS) {
            intent.putExtra(EXTRA_PIX, result.mPix.getNativePix());
        }
        intent.putExtra(EXTRA_STATUS, result.mStatus.ordinal());
        intent.putExtra(EXTRA_SKIP_CROP, skipCrop);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * 작업 쓰레드에서 이미지 로딩
     */
    @Override
    protected LoadResult doInBackground(Void... params)
    {
        //AsyncTask가 취소됐을 때
        if (isCancelled())
        {
            Log.i(TAG, "isCancelled");
            return null;
        }

        //Picasso라이브러리로 Uri를 읽어 bitmap으로 변환.
        //leptonica로 bitmap을 pix로 변환
        Pix p=null;
        try
        {
            Thread.sleep(700);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        try {
            final Bitmap bmp = Picasso.with(MyApplication.rootActivity).load(photoUri).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).get();
            if (bmp != null) {
                p = readBitmap(bmp);
                bmp.recycle();
            }
        }
        catch (IOException ignored) {
            p = null;
        }



        if (p == null)
        {
            Log.i(TAG,"could not load image.");
            return new LoadResult(PixLoadStatus.IMAGE_FORMAT_UNSUPPORTED);
        }
        final long pixPixelCount = p.getWidth() * p.getHeight();
        if (pixPixelCount < MIN_PIXEL_COUNT)
        {
            double scale = Math.sqrt(((double) MIN_PIXEL_COUNT) / pixPixelCount);
            Pix scaledPix = Scale.scale(p, (float) scale);
            if (scaledPix.getNativePix() == 0)
                Log.i(TAG,"scaled pix is 0");
            else
            {
                p.recycle();
                p = scaledPix;
            }
        }
        return new LoadResult(p);
    }
}
