package kr.ac.ssu.cse.jahn.textsnapper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by CypressRH on 2017-11-16.
 */

public class PrefUtils
{
    public final static String PREFERENCES_ALIGNMENT_KEY = "text_alignment";
    public final static String PREFERENCES_TEXT_SIZE_KEY = "text_size";
    private final static String PREFERENCES_TRAINING_DATA_DIR = "training_data_dir";
    public final static String PREFERENCES_OCR_LANG = "ocr_language";
    public final static String PREFERENCES_KEY = "text_preferences";

    public static SharedPreferences getPreferences(Context applicationContext)
    {
        return applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public static String getAppDir(Context appContext)
    {
        SharedPreferences prefs = getPreferences(appContext);
        return prefs.getString(PREFERENCES_TRAINING_DATA_DIR, null);
    }

    private static SharedPreferences getDefaultPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isEng(Context context) {
        SharedPreferences pref = getDefaultPreferences(context);
        return pref.getString("ocrSelect", "English").equals("English");
    }

    public static boolean isFixed(Context context) {
        SharedPreferences pref = getDefaultPreferences(context);
        return pref.getBoolean("floatingButtonLocation", false);
    }

    public static boolean isAvailable(Context context) {
        SharedPreferences pref = getDefaultPreferences(context);
        return pref.getBoolean("floatingButtonUse", true);
    }
}
