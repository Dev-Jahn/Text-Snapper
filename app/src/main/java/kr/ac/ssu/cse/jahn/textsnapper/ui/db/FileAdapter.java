package kr.ac.ssu.cse.jahn.textsnapper.ui.db;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import kr.ac.ssu.cse.jahn.textsnapper.R;

import static kr.ac.ssu.cse.jahn.textsnapper.R.id.fileDateTextView;
import static kr.ac.ssu.cse.jahn.textsnapper.R.id.fileImageView;
import static kr.ac.ssu.cse.jahn.textsnapper.R.id.fileNameTextView;
import static kr.ac.ssu.cse.jahn.textsnapper.R.id.fileSizeTextView;

/**
 * Created by ArchSlave on 2017-12-07.
 */

public class FileAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<Item> mList;
    LayoutInflater mLayoutInflater;

    class ViewHolder {
        ImageView fileImageView;
        TextView fileNameTextView;
        TextView fileSizeTextView;
        TextView fileDateTextView;
    }

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
        ViewHolder viewHolder = null;

        if(itemView == null) {
            itemView = mLayoutInflater.inflate(R.layout.list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.fileImageView = (ImageView)itemView.findViewById(fileImageView);
            viewHolder.fileNameTextView = (TextView)itemView.findViewById(fileNameTextView);
            viewHolder.fileSizeTextView = (TextView)itemView.findViewById(fileSizeTextView);
            viewHolder.fileDateTextView = (TextView)itemView.findViewById(fileDateTextView);

            itemView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)itemView.getTag();
        }

        Item curItem = mList.get(position);

        File curFile = new File(curItem.getFilePath());
        if(curFile.exists()) {
            Bitmap fileBitmapImage;
            if(curFile.length() > 1024 * 1024) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                fileBitmapImage = BitmapFactory.decodeFile(curFile.getAbsolutePath(), options);
            } else {
                fileBitmapImage = BitmapFactory.decodeFile(curFile.getAbsolutePath());
            }
            int width = viewHolder.fileImageView.getLayoutParams().width;
            int height = viewHolder.fileImageView.getLayoutParams().height;
            Bitmap resizeImage = Bitmap.createScaledBitmap(fileBitmapImage, width, height, true);
            viewHolder.fileImageView.setImageBitmap(resizeImage);
            viewHolder.fileNameTextView.setText(curItem.getFileName());
            viewHolder.fileSizeTextView.setText(curItem.getFileSize());
            viewHolder.fileDateTextView.setText(curItem.getFileDate());

        } else {
            mList.remove(curItem);
        }
        return itemView;
    }
}
