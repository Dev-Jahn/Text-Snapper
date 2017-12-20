package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;

import kr.ac.ssu.cse.jahn.textsnapper.R;
import kr.ac.ssu.cse.jahn.textsnapper.ui.db.FileAdapter;
import kr.ac.ssu.cse.jahn.textsnapper.ui.db.FileDatabase;
import kr.ac.ssu.cse.jahn.textsnapper.ui.db.Item;
import kr.ac.ssu.cse.jahn.textsnapper.util.Utils;

/**
 * Created by ArchSlave on 2017-11-16.
 */

public class RecentFilesFragment extends Fragment {

    private static FileAdapter adapter;
    private Context context;
    private ListView mListView;
    private static ArrayList<Item> mList;
    private ScreenshotObserver observer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recentfiles, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mList = new ArrayList<Item>();

        /**
         * 파일을 클릭했을 때에 대한 처리과정
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item curItem = mList.get(position);
                File curFile = new File(Utils.convertPathToTxt(curItem.getFilePath()));
                Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", curFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "text/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("DEBUG5", "Longclick");
                final Item curItem = mList.get(position);

                PopupMenu mPopup = new PopupMenu(context, view, Gravity.RIGHT);

                mPopup.getMenuInflater().inflate(R.menu.menu_file_pop_up, mPopup.getMenu());

                mPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FileDatabase database = FileDatabase.getInstance(context);
                        switch (item.getItemId()) {
                            // 파일 열기
                            case R.id.select:
                                File curFile = new File(Utils.convertPathToTxt(curItem.getFilePath()));
                                Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", curFile);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "text/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(intent);
                                break;
                            case R.id.setFavoriteMenu:
                                if(FavoriteFragment.isAlreadyAdded(curItem) == false) {
                                    ContentValues addRowValue = new ContentValues();
                                    addRowValue.put("filename", curItem.getFileName());
                                    addRowValue.put("file", curItem.getFilePath());
                                    database.insert(addRowValue);
                                    FavoriteFragment.addFavorite(curItem);
                                }
                                break;

                            case R.id.deleteMenu:
                                File delFile = new File(curItem.getFilePath());
                                delFile.delete();
                                database.delete("file='"+curItem.getFilePath()+"'", null);
                                mList.remove(curItem);
                                adapter.notifyDataSetChanged();
                                FavoriteFragment.deleteFile(curItem);
                                break;
                        }
                        return true;
                    }
                });

                mPopup.show();

                // click 이벤트로 안넘어갈 것이기 때문에 true, 변경 금지.
                return true;
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity().getApplicationContext();
        Handler handler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message msg)
            {
                Intent i = new Intent();
                i.setComponent(new ComponentName(context.getPackageName(),context.getPackageName()+".ResultActivity"));
                Log.e("DEBUG8", "파일생성 감지: "+msg.getData().getString("path"));
                updateAdapterList();
                adapter.notifyDataSetChanged();
            }
        };
        observer = new ScreenshotObserver(Utils.EDIT_PATH, handler);
        observer.startWatching();

        updateAdapterList();
        adapter = new FileAdapter(context, mList);
        mListView.setAdapter(adapter);
    }

    public void updateAdapterList() {
        File curDir = new File(Utils.EDIT_PATH);
        mList.clear();
        File[] curFiles = curDir.listFiles();

        try {
            for (File curFile : curFiles) {
                Date lastModDate = new Date(curFile.lastModified());
                DateFormat formatter = DateFormat.getDateTimeInstance();
                String modDate = formatter.format(lastModDate);

                if (!curFile.isDirectory()) {
                    String fileUnit = "Bytes";
                    long fileSize = curFile.length();
                    if (fileSize > 1024) {
                        fileSize /= 1024;
                        fileUnit = "KB";
                        if (fileSize > 1024) {
                            fileSize /= 1024;
                            fileUnit = "MB";
                        }
                    }
                    mList.add(new Item(curFile.getName(), fileSize + fileUnit, modDate, curFile.getAbsolutePath()));
                }
            }
        } catch (Exception e) {
            Log.d("EXCEPTION", "Exception : " + e);
        }
        Collections.sort(mList);
    }

    public void onResume() {
        super.onResume();
        updateAdapterList();
        adapter.notifyDataSetChanged();
    }
    /**
     * 주의! Fragment 상호 교류를 위해 어쩔 수 없이 채택한 코드
     * Fragment 외 호출 금지
     */
    protected static void deleteFile(Item newItem) {
        mList.remove(newItem);
        adapter.notifyDataSetChanged();
    }
}