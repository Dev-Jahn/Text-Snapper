package kr.ac.ssu.cse.jahn.ocr2.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by CypressRH on 2017-11-16.
 */

public class Utils
{
    public final static String EXTERNAL_APP_DIRECTORY = "TextSnapper";
    public static String getTessDir(final Context appContext) {
        String tessDir = PrefUtils.getTessDir(appContext);
        if (tessDir == null) {
            return new File(Environment.getExternalStorageDirectory(), EXTERNAL_APP_DIRECTORY).getPath() + "/";
        } else {
            return tessDir;
        }
    }
}
