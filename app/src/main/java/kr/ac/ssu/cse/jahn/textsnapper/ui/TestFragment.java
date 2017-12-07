package kr.ac.ssu.cse.jahn.textsnapper.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;

import kr.ac.ssu.cse.jahn.textsnapper.R;
import kr.ac.ssu.cse.jahn.textsnapper.ui.src.FileAdapter;
import kr.ac.ssu.cse.jahn.textsnapper.ui.src.Item;
import kr.ac.ssu.cse.jahn.textsnapper.util.Utils;

/**
 * Created by ArchSlave on 2017-12-06.
 */

public class TestFragment extends Fragment {

    Context context;
    ListView mListView;
    ArrayList<Item> mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test, container, false);
        mListView = (ListView)view.findViewById(R.id.listView);
        mList = new ArrayList<Item>();

        File curDir = new File(Utils.DATA_PATH);

        File[] curFiles = curDir.listFiles();

        ArrayList<Item> dir = new ArrayList<Item>();
        ArrayList<Item> file = new ArrayList<Item>();
        try {
            for(File curFile : curFiles) {
                Date lastModDate = new Date(curFile.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String modDate = formater.format(lastModDate);
                // 파일이 폴더인 경우
                if(curFile.isDirectory()) {
                    File[] fileBuf = curFile.listFiles();
                    int buf = 0;
                    if(fileBuf != null)
                        buf = fileBuf.length;

                    String numItem = String.valueOf(buf);
                    numItem += " 개";

                    dir.add(new Item(curFile.getName(), numItem, modDate));
                }
                // 일반 파일인 경우
                else {
                    file.add(new Item(curFile.getName(), curFile.length() + "Bytes", modDate));
                }
            }
        } catch(Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(file);
        dir.addAll(file);
        if(!curDir.getName().equalsIgnoreCase("sdcard"))
            dir.add(0, new Item("..", "Parent Directory", ""));

        mList = dir;

        return view;
    }




    @Override
    public void onResume() {
        super.onResume();
        context = getActivity().getApplicationContext();
        FileAdapter adapter = new FileAdapter(context, mList);
        mListView.setAdapter(adapter);
    }
}
