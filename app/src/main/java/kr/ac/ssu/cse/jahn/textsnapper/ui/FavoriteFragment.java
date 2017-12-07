package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import kr.ac.ssu.cse.jahn.textsnapper.R;
import kr.ac.ssu.cse.jahn.textsnapper.ui.src.FileAdapter;
import kr.ac.ssu.cse.jahn.textsnapper.ui.src.FileDatabase;
import kr.ac.ssu.cse.jahn.textsnapper.ui.src.Item;

/**
 * Created by ArchSlave on 2017-12-07.
 */

public class FavoriteFragment extends Fragment {

    FileAdapter adapter;
    Context context;
    ListView mListView;
    ArrayList<Item> mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test, container, false);
        mListView = (ListView)view.findViewById(R.id.listView);
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

                mPopup.getMenuInflater().inflate(R.menu.menu_favorite_pop_up, mPopup.getMenu());

                mPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FileDatabase database = FileDatabase.getInstance(context);
                        switch(item.getItemId()) {
                            // 별명 설정
                            case R.id.setTitleMenu :
                                Intent intent;

                                break;
                            // 즐겨찾기 해제
                            case R.id.setFavoriteMenu :
                                database.delete("file='"+curItem.getFilePath()+"'", null);
                                updateAdapterList();
                                adapter.notifyDataSetChanged();
                                break;
                            // 파일 삭제
                            case R.id.deleteMenu :
                                File delFile = new File(curItem.getFilePath());
                                delFile.delete();
                                database.delete("file='"+curItem.getFilePath()+"'", null);
                                updateAdapterList();
                                adapter.notifyDataSetChanged();
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
        mList.clear();
        String[] columns = {"filename, file"};
        FileDatabase database = FileDatabase.getInstance(context);

        Cursor cursor = database.query(columns, null, null, null, null, null);

        if(cursor != null) {
            while (cursor.moveToNext()) {
                File file = new File(cursor.getString(1));
                String fileUnit = "Bytes";
                long fileSize = file.length();
                if (fileSize > 1024) {
                    fileSize /= 1024;
                    fileUnit = "KB";
                    if (fileSize > 1024) {
                        fileSize /= 1024;
                        fileUnit = "MB";
                    }
                }
                Date lastModDate = new Date(file.lastModified());
                DateFormat formatter = DateFormat.getDateTimeInstance();
                String modDate = formatter.format(lastModDate);

                mList.add(new Item(cursor.getString(0), fileSize + fileUnit, modDate, file.getAbsolutePath()));
            }
            cursor.close();
        }
    }
}