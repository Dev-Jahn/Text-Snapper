package kr.ac.ssu.cse.jahn.ocr2;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Pixa;

import java.util.ArrayList;

import static kr.ac.ssu.cse.jahn.ocr2.OCRProcessor.EXTRA_WORD_BOX;
import static kr.ac.ssu.cse.jahn.ocr2.OCRProcessor.MESSAGE_EXPLANATION_TEXT;
import static kr.ac.ssu.cse.jahn.ocr2.OCRProcessor.MESSAGE_FINAL_IMAGE;
import static kr.ac.ssu.cse.jahn.ocr2.OCRProcessor.MESSAGE_LAYOUT_ELEMENTS;
import static kr.ac.ssu.cse.jahn.ocr2.OCRProcessor.MESSAGE_PREVIEW_IMAGE;
import static kr.ac.ssu.cse.jahn.ocr2.OCRProcessor.MESSAGE_TESSERACT_PROGRESS;

/**
 * Created by CypressRH on 2017-11-16.
 */

public class ProgressHandler extends Handler
{
    private String hocrString;
    private String utf8String;
    //private long layoutPix;
    private int mPreviewWith;
    private int mPreviewHeight;

    private boolean mHasStartedOcr = false;

    public void handleMessage(Message msg) {
        switch (msg.what) {

        case MESSAGE_EXPLANATION_TEXT: {
            setToolbarMessage(msg.arg1);
            break;
        }
        case MESSAGE_TESSERACT_PROGRESS: {
            if (!mHasStartedOcr) {
                mAnalytics.sendScreenView("Ocr");
                mHasStartedOcr = true;
            }
            int percent = msg.arg1;
            Bundle data = msg.getData();
            mImageView.setProgress(percent,
                    (RectF) data.getParcelable(EXTRA_WORD_BOX),
                    (RectF) data.getParcelable(EXTRA_OCR_BOX));
            break;
        }
        case MESSAGE_PREVIEW_IMAGE: {
            mPreviewHeight = ((Bitmap) msg.obj).getHeight();
            mPreviewWith = ((Bitmap) msg.obj).getWidth();
            mImageView.setImageBitmapResetBase((Bitmap) msg.obj, true, 0);
            break;
        }
        case MESSAGE_FINAL_IMAGE: {
            long nativePix = (long) msg.obj;

            if (nativePix != 0) {
                mFinalPix = new Pix(nativePix);
            }
            break;
        }

        case MESSAGE_LAYOUT_ELEMENTS: {
            Pair<Long, Long> longLongPair = (Pair<Long, Long>) msg.obj;
            long nativePixaText = longLongPair.first;
            long nativePixaImages = longLongPair.second;
            final Pixa texts = new Pixa(nativePixaText, 0, 0);
            final Pixa images = new Pixa(nativePixaImages, 0, 0);
            ArrayList<Rect> boxes = images.getBoxRects();
            ArrayList<RectF> scaledBoxes = new ArrayList<>(boxes.size());
            float xScale = (1.0f * mPreviewWith) / mOriginalWidth;
            float yScale = (1.0f * mPreviewHeight) / mOriginalHeight;
            // scale the to the preview image space
            for (Rect r : boxes) {
                scaledBoxes.add(new RectF(r.left * xScale, r.top * yScale,
                        r.right * xScale, r.bottom * yScale));
            }
            mImageView.setImageRects(scaledBoxes);
            boxes = texts.getBoxRects();
            scaledBoxes = new ArrayList<>(boxes.size());
            for (Rect r : boxes) {
                scaledBoxes.add(new RectF(r.left * xScale, r.top * yScale,
                        r.right * xScale, r.bottom * yScale));
            }
            mImageView.setTextRects(scaledBoxes);

            mButtonStartOCR.setVisibility(View.VISIBLE);
            mButtonStartOCR.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    int[] selectedTexts = mImageView
                            .getSelectedTextIndexes();
                    int[] selectedImages = mImageView
                            .getSelectedImageIndexes();
                    if (selectedTexts.length > 0
                            || selectedImages.length > 0) {
                        mImageView.clearAllProgressInfo();


                        mOCR.startOCRForComplexLayout(OCRActivity.this,
                                mOcrLanguage, texts,
                                images, selectedTexts, selectedImages);
                        mButtonStartOCR.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                R.string.please_tap_on_column,
                                Toast.LENGTH_LONG).show();
                    }

                }
            });
            mAnalytics.sendScreenView("Pick Columns");

            setToolbarMessage(R.string.progress_choose_columns);

            break;
        }
        case OCR.MESSAGE_HOCR_TEXT: {
            this.hocrString = (String) msg.obj;
            mAccuracy = msg.arg1;
            break;
        }
        case OCR.MESSAGE_UTF8_TEXT: {
            this.utf8String = (String) msg.obj;
            break;
        }
        case OCR.MESSAGE_END: {
            saveDocument(mFinalPix, hocrString, utf8String, mAccuracy);
            break;
        }
        case OCR.MESSAGE_ERROR: {
            Toast.makeText(getApplicationContext(), getText(msg.arg1), Toast.LENGTH_LONG).show();
            break;
        }
        }
    }
}
