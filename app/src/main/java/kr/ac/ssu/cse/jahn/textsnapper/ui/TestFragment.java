package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import kr.ac.ssu.cse.jahn.textsnapper.ui.src.FileAdapter;
import kr.ac.ssu.cse.jahn.textsnapper.ui.src.FileDatabase;
import kr.ac.ssu.cse.jahn.textsnapper.ui.src.Item;
import kr.ac.ssu.cse.jahn.textsnapper.util.Utils;

/**
 * Created by ArchSlave on 2017-12-06.
 */

public class TestFragment extends Fragment {

    FileAdapter adapter;
    Context context;
    ListView mListView;
    ArrayList<Item> mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mList = new ArrayList<Item>();

        /**
         * 파일을 클릭했을 때에 대한 처리
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item curItem = mList.get(position);
                Log.d("DEBUG5", "Click");
                /**
                 * Activity로 넘기기 바랍니다.
                 */
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
                        switch (item.getItemId()) {
                            case R.id.setFavoriteMenu:
                                FileDatabase database = FileDatabase.getInstance(context);
                                ContentValues addRowValue = new ContentValues();
                                addRowValue.put("filename", curItem.getFileName());
                                addRowValue.put("file", curItem.getFilePath());
                                long insertRecordId = database.insert(addRowValue);
                                break;

                            case R.id.deleteMenu:
                                break;
                        }
                        return true;
                    }
                });

                mPopup.show();

                // click 이벤트로 안넘어갈 것이기 때문에 true, 변경 금지
                return true;
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity().getApplicationContext();
        updateAdapterList();
        adapter = new FileAdapter(context, mList);
        mListView.setAdapter(adapter);
    }

    public void updateAdapterList() {
        File curDir = new File(Utils.DATA_PATH);

        File[] curFiles = curDir.listFiles();

        ArrayList<Item> file = new ArrayList<Item>();
        try {
            for (File curFile : curFiles) {
                Log.d("FileIO", curFile.getAbsolutePath());
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
                    file.add(new Item(curFile.getName(), fileSize + fileUnit, modDate, curFile.getAbsolutePath()));
                }
            }
        } catch (Exception e) {
            Log.d("EXCEPTION", "Exception : " + e);
        }
        Collections.sort(file);
        mList = file;
    }
}
