package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.app.Activity;
import android.app.Application;

import ly.img.android.PESDK;

/**
 * Created by CypressRH on 2017-12-10.
 */

public class MyApplication extends Application
{
    public static Activity rootActivity;

    @Override
    public void onCreate()
    {
        super.onCreate();
        PESDK.init(this);
    }
}
