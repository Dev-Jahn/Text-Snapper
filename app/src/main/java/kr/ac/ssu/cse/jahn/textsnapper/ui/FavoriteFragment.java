package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.ContentValues;
import android.content.Context;
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
import java.util.Iterator;

import kr.ac.ssu.cse.jahn.textsnapper.R;
import kr.ac.ssu.cse.jahn.textsnapper.ui.db.FileAdapter;
import kr.ac.ssu.cse.jahn.textsnapper.ui.db.FileDatabase;
import kr.ac.ssu.cse.jahn.textsnapper.ui.db.Item;
import kr.ac.ssu.cse.jahn.textsnapper.util.RenameDialog;

/**
 * Created by ArchSlave on 2017-12-07.
 */

public class FavoriteFragment extends Fragment {

    private static FileDatabase database;
    private static FileAdapter adapter;
    private Context context;
    private ListView mListView;
    private static ArrayList<Item> mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        mListView = (ListView)view.findViewById(R.id.listView);
        mList = new ArrayList<Item>();

        /**
         * 파일을 클릭했을 때에 대한 처리
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item curItem = mList.get(position);
                /**
                 * Activity로 넘기기 바랍니다.
                 */
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Item curItem = mList.get(position);

                PopupMenu mPopup = new PopupMenu(context, view, Gravity.RIGHT);

                mPopup.getMenuInflater().inflate(R.menu.menu_favorite_pop_up, mPopup.getMenu());

                mPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final FileDatabase database = FileDatabase.getInstance(context);
                        switch(item.getItemId()) {
                            // 별명 설정
                            case R.id.setTitleMenu :
                                final RenameDialog mDialog = new RenameDialog(getActivity());
                                mDialog.setTitle("Rename");
                                mDialog.setOkClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ContentValues updateRowValue = new ContentValues();
                                        String nickname = mDialog.getEditTextContent();
                                        updateRowValue.put("filename", nickname);
                                        database.update(updateRowValue, "file='"+curItem.getFilePath()+"'", null);
                                        mList.get(mList.indexOf(curItem)).setFileName(nickname);
                                        adapter.notifyDataSetChanged();
                                        mDialog.cancel();
                                    }
                                });
                                mDialog.setCancelClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDialog.cancel();
                                    }
                                });
                                mDialog.show();

                                break;
                            // 즐겨찾기 해제
                            case R.id.setFavoriteMenu :
                                database.delete("file='"+curItem.getFilePath()+"'", null);
                                mList.remove(curItem);
                                adapter.notifyDataSetChanged();
                                break;
                            // 파일 삭제
                            case R.id.deleteMenu :
                                File delFile = new File(curItem.getFilePath());
                                database.delete("file='"+curItem.getFilePath()+"'", null);
                                mList.remove(curItem);
                                curItem.setFileName(delFile.getName());
                                delFile.delete();
                                adapter.notifyDataSetChanged();
                                RecentFilesFragment.deleteFile(curItem);
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
        context = getActivity();
        database = FileDatabase.getInstance(context);
        updateAdapterList();
        adapter = new FileAdapter(context, mList);
        mListView.setAdapter(adapter);
    }

    public void updateAdapterList() {
        String[] columns = {"filename, file"};

        Cursor cursor = database.query(columns, null, null, null, null, null);

        if(cursor != null) {
            while (cursor.moveToNext()) {
                File file = new File(cursor.getString(1));
                if (file.exists()) {
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
                } else {
                    database.delete("file='"+file.getAbsolutePath()+"'", null);
                }
            }
            cursor.close();
        }
    }

    /**
     * 주의! Fragment 상호 교류를 위해 어쩔 수 없이 채택한 코드
     * Fragment 외 호출 금지
     */
    protected static void addFavorite(Item newItem) {
        mList.add(newItem);
        adapter.notifyDataSetChanged();
    }

    protected static boolean isAlreadyAdded(Item newItem) {
        return mList.contains(newItem);
    }

    protected static void deleteFile(Item delItem) {

        Iterator<Item> iterator = mList.iterator();
        while( iterator.hasNext() ) {
            Item i = iterator.next();
            if(i.getFilePath().equals(delItem.getFilePath())) {
                i.setFileName(delItem.getFileName());
                break;
            }
        }
        mList.remove(delItem);
        adapter.notifyDataSetChanged();
    }
}