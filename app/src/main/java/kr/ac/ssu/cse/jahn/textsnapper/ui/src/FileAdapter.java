package kr.ac.ssu.cse.jahn.textsnapper.ui.src;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.ac.ssu.cse.jahn.textsnapper.R;

/**
 * Created by ArchSlave on 2017-12-07.
 */

public class FileAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<Item> mList;
    LayoutInflater mLayoutInflater;

    public FileAdapter(Context context, ArrayList<Item> list) {
        mContext = context;
        mList = list;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Item getItem(int position) {
        return mList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View itemView = convertView;

        if(itemView == null) {
            itemView = mLayoutInflater.inflate(R.layout.list_item, null);
        }

        ImageView imageView = (ImageView)itemView.findViewById(R.id.imageView);
        TextView fileNameTextView = (TextView)itemView.findViewById(R.id.fileNameTextView);
        TextView fileSizeTextView = (TextView)itemView.findViewById(R.id.fileSizeTextView);
        TextView fileDateTextView = (TextView)itemView.findViewById(R.id.fileDateTextView);

        Item curItem = mList.get(position);

        if(imageView != null)
            imageView.setImageResource(R.drawable.image_gallery);
        fileNameTextView.setText(curItem.getFileName());
        fileSizeTextView.setText(curItem.getFileSize());
        fileDateTextView.setText(curItem.getFileDate());


        return itemView;
    }
}
