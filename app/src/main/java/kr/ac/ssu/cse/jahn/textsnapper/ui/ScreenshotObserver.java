package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

        /**
         * 편집된 파일 생성시 핸들러로 메시지전송
         */
        Message msg = Message.obtain(handler);
        Bundle b = new Bundle();
        switch (event)
        {
        case FileObserver.CREATE:
            /**
             * 임시파일 생성을 필터링
             */
            if(path.matches("[a-zA-Z0-9_-]+.jpg.t"))
            {
                Log.v(TAG, "임시파일"+path);
                return;
            }
            b.putString("path", path);
            b.putInt("state", FileObserver.CREATE);
            msg.setData(b);
            handler.sendMessage(msg);
            break;
        }
    }
}
