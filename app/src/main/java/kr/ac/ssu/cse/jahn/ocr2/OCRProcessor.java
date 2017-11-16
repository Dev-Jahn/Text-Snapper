package kr.ac.ssu.cse.jahn.ocr2;

import android.content.Context;
import android.os.Messenger;
import android.util.Log;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.R.attr.width;

/**
 * Created by CypressRH on 2017-11-16.
 */

public class OCRProcessor
{
    //OCR 엔진 객체
    private TessBaseAPI mTess;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private int mOriginalHeight;
    private int mOriginalWidth;

    public OCRProcessor(final Messenger messenger)
    {

    }

    private boolean initTessApi(String tessDir, String lang, int ocrMode) {
        mTess = new TessBaseAPI(OCR.this);
        boolean result = mTess.init(tessDir, lang, ocrMode);
        if (!result) {
            sendMessage(MESSAGE_ERROR, R.string.error_tess_init);
            return false;
        }
        mTess.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "ﬀﬁﬂﬃﬄﬅﬆ");
        return true;
    }
    /*
     *width, height는 프리뷰용
     */
    public void doOCR(final Context context, final String lang, final Pix pixs){//}, int width, int height) {
        if (pixs == null)
            throw new IllegalArgumentException("Source pix must be non-null");
        //mPreviewHeightUnScaled = height;
        //mPreviewWidthUnScaled = width;
        mOriginalHeight = pixs.getHeight();
        mOriginalWidth = pixs.getWidth();

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String tessDir = Util.getTessDir(context);

                    long nativeTextPix = mNativeBinding.convertBookPage(pixs);
                    Pix pixText = new Pix(nativeTextPix);
                    mOriginalHeight = pixText.getHeight();
                    mOriginalWidth = pixText.getWidth();
                    sendMessage(MESSAGE_EXPLANATION_TEXT, R.string.progress_ocr);
                    sendMessage(MESSAGE_FINAL_IMAGE, nativeTextPix);
                    synchronized (OCR.this) {
                        if (mStopped) {
                            return;
                        }
                        final String ocrLanguages = determineOcrLanguage(lang);
                        int ocrMode = determineOcrMode(lang);
                        if (!initTessApi(tessDir, ocrLanguages, ocrMode)) return;

                        mTess.setPageSegMode(PageSegMode.PSM_AUTO);
                        mTess.setImage(pixText);
                    }
                    String hocrText = mTess.getHOCRText(0);
                    int accuracy = mTess.meanConfidence();
                    final String utf8Text = mTess.getUTF8Text();

                    if (utf8Text.isEmpty()) {
                        Log.i(LOG_TAG, "No words found. Looking for sparse text.");
                        mTess.setPageSegMode(PageSegMode.PSM_SPARSE_TEXT);
                        mTess.setImage(pixText);
                        hocrText = mTess.getHOCRText(0);
                        accuracy = mTess.meanConfidence();
                    }

                    synchronized (OCR.this) {
                        if (mStopped) {
                            return;
                        }
                        String htmlText = mTess.getHtmlText();
                        if (accuracy == 95) {
                            accuracy = 0;
                        }

                        sendMessage(MESSAGE_HOCR_TEXT, hocrText, accuracy);
                        sendMessage(MESSAGE_UTF8_TEXT, htmlText, accuracy);
                    }

                } finally {
                    if (mTess != null) {
                        synchronized (OCR.this) {
                            mTess.end();
                        }
                    }
                    mCompleted = true;
                    sendMessage(MESSAGE_END);
                }
            }
        });
    }
    public synchronized void cancel() {
        if (mTess != null) {
            if (!mCompleted) {
                mAnalytics.sendOcrCancelled();
            }
            mTess.stop();
        }
        mStopped = true;
    }

}
