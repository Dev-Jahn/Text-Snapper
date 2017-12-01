package kr.ac.ssu.cse.jahn.textsnapper.UI;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by ArchSlave on 2017-11-05.
 */

public class Utils {

    public static boolean canDrawOverlays(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(context);
        }
    }
}
