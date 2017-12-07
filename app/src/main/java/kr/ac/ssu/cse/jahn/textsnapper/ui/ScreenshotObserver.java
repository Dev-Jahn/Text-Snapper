package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;

/**
 * Created by CypressRH on 2017-12-04.
 */

public class ScreenshotObserver extends FileObserver
{
    private static final String TAG = ScreenshotObserver.class.getSimpleName();
    private final String root;
    private Handler handler;

    public ScreenshotObserver(String path, Handler handler)
    {
        super(path);
        this.root = path;
        this.handler = handler;
    }
    @Override
    public void onEvent(int event, String path)
    {
        Message msg = Message.obtain(handler);
        Bundle b = new Bundle();
        switch (event)
        {
        case FileObserver.CREATE:
            b.putString("path", path);
            b.putInt("state", FileObserver.CREATE);
            msg.setData(b);
            handler.sendMessage(msg);
            break;
        }
    }
}
