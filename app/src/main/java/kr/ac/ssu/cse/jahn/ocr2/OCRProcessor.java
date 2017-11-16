package kr.ac.ssu.cse.jahn.ocr2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.ac.ssu.cse.jahn.ocr2.util.Utils;

import static android.R.attr.left;
import static android.R.attr.right;

/**
 * Created by CypressRH on 2017-11-16.
 */

public class OCRProcessor implements TessBaseAPI.ProgressNotifier
{
    public static final int MESSAGE_PREVIEW_IMAGE = 3;
    public static final int MESSAGE_END = 4;
    public static final int MESSAGE_ERROR = 5;
    public static final int MESSAGE_TESSERACT_PROGRESS = 6;
    public static final int MESSAGE_FINAL_IMAGE = 7;
    public static final int MESSAGE_UTF8_TEXT = 8;
    public static final int MESSAGE_HOCR_TEXT = 9;
    public static final int MESSAGE_LAYOUT_ELEMENTS = 10;
    public static final int MESSAGE_EXPLANATION_TEXT = 12;
    public static final String EXTRA_WORD_BOX = "word_box";
    public static final String EXTRA_Region_BOX = "ocr_box";
    private static final String TAG = OCRProcessor.class.getSimpleName();

    private RectF mWordBox = new RectF();
    private RectF mRegionBox = new RectF();
    private int mOriginalHeight;
    private int mOriginalWidth;
    private boolean mIsProcessorStarted = false;
    private boolean mStopped;
    private boolean mCompleted;

    private Messenger mMessenger;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private TessBaseAPI mTess;

    public OCRProcessor(final Messenger messenger)
    {
        mIsProcessorStarted = true;
    }

    private boolean initTessApi(String tessDir, String lang, int ocrMode)
    {
        mTess = new TessBaseAPI(OCRProcessor.this);
        boolean result = mTess.init(tessDir, lang, ocrMode);
        if (!result) {
            sendMessage(MESSAGE_ERROR, R.string.error_tess_init);
            return false;
        }
        mTess.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "ﬀﬁﬂﬃﬄﬅﬆ");
        return true;
    }

    @Override
    public void onProgressValues(TessBaseAPI.ProgressValues progressValues)
    {
        final int percent = progressValues.getPercent();
        mWordBox = new RectF(progressValues.getCurrentWordRect());
        mRegionBox = new RectF(progressValues.getCurrentRect());
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_WORD_BOX, mWordBox);
        b.putParcelable(EXTRA_Region_BOX, mRegionBox);
        sendMessage(MESSAGE_TESSERACT_PROGRESS, percent, b);
    }

    private void sendMessage(int what)
    {
        sendMessage(what, 0, 0, null, null);
    }

    private void sendMessage(int what, int arg1, int arg2)
    {
        sendMessage(what, arg1, arg2, null, null);
    }

    private void sendMessage(int what, String string)
    {
        sendMessage(what, 0, 0, string, null);
    }

    private void sendMessage(int what, String string, int accuracy)
    {
        sendMessage(what, accuracy, 0, string, null);
    }

    private void sendMessage(int what, long nativeTextPix)
    {
        sendMessage(what, 0, 0, nativeTextPix, null);
    }

    private void sendMessage(int what, int arg1)
    {
        sendMessage(what, arg1, 0, null, null);
    }

    private void sendMessage(int what, Bitmap previewBitmap)
    {
        sendMessage(what, 0, 0, previewBitmap, null);
    }


    private void sendMessage(int what, int arg1, Bundle b)
    {
        sendMessage(what, arg1, 0, null, b);
    }

    private synchronized void sendMessage(int what, int arg1, int arg2, Object object, Bundle b)
    {
        if (mIsProcessorStarted && !mStopped)
        {

            Message m = Message.obtain();
            m.what = what;
            m.arg1 = arg1;
            m.arg2 = arg2;
            m.obj = object;
            m.setData(b);
            try
            {
                mMessenger.send(m);
            } catch (RemoteException ignore)
            {
                ignore.printStackTrace();
            }
        }
    }
    /*
     *width, height는 프리뷰용
     */
    public void doOCR(final Context context, final String lang, final Pix textpix){//}, int width, int height) {
        if (textpix == null)
            throw new IllegalArgumentException("Source pix must be non-null");
        //mPreviewHeightUnScaled = height;
        //mPreviewWidthUnScaled = width;
        mOriginalHeight = textpix.getHeight();
        mOriginalWidth = textpix.getWidth();

        mExecutorService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final String tessDir = Utils.getTessDir(context);

                    long nativeTextPix = textpix.getNativePix();
                    mOriginalHeight = textpix.getHeight();
                    mOriginalWidth = textpix.getWidth();
                    sendMessage(MESSAGE_EXPLANATION_TEXT, R.string.progress_ocr);
                    sendMessage(MESSAGE_FINAL_IMAGE, nativeTextPix);
                    synchronized (OCRProcessor.this)
                    {
                        if (mStopped)
                            return;

                        final String ocrLanguages = determineOcrLanguage(lang);
                        int ocrMode = determineOcrMode(lang);
                        if (!initTessApi(tessDir, ocrLanguages, ocrMode)) return;

                        mTess.setPageSegMode(PageSegMode.PSM_AUTO);
                        mTess.setImage(pixText);
                    }
                    String hocrText = mTess.getHOCRText(0);
                    int accuracy = mTess.meanConfidence();
                    final String utf8Text = mTess.getUTF8Text();

                    if (utf8Text.isEmpty())
                    {
                        Log.i(TAG, "No words found. Looking for sparse text.");
                        mTess.setPageSegMode(PageSegMode.PSM_SPARSE_TEXT);
                        mTess.setImage(pixText);
                        hocrText = mTess.getHOCRText(0);
                        accuracy = mTess.meanConfidence();
                    }

                    synchronized (OCRProcessor.this)
                    {
                        if (mStopped)
                            return;
                        String htmlText = mTess.getHtmlText();
                        if (accuracy == 95)
                            accuracy = 0;

                        sendMessage(MESSAGE_HOCR_TEXT, hocrText, accuracy);
                        sendMessage(MESSAGE_UTF8_TEXT, htmlText, accuracy);
                    }

                } finally
                {
                    if (mTess != null)
                    {
                        synchronized (OCRProcessor.this)
                        {
                            mTess.end();
                        }
                    }
                    mCompleted = true;
                    sendMessage(MESSAGE_END);
                }
            }
        });
    }
    public synchronized void cancel()
    {
        if (mTess != null)
        {
            if (!mCompleted)
                Log.i(TAG, "OCR cancelled");
            mTess.stop();
        }
        mStopped = true;
    }


}
