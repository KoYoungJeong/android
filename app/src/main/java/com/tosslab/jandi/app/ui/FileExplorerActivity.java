package com.tosslab.jandi.app.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.files.FileExplorerItem;
import com.tosslab.jandi.app.lists.files.FileExplorerItemListAdapter;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FileExplorerActivity extends ListActivity {
    private File currentDir;
    private FileExplorerItemListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = Environment.getExternalStorageDirectory();
        fill(currentDir);
    }

    private void fill(File f)
    {
        File[] dirs = f.listFiles();
        this.setTitle("Current Dir: " + f.getName());
        List<FileExplorerItem> dir = new ArrayList<FileExplorerItem>();
        List<FileExplorerItem> fls = new ArrayList<FileExplorerItem>();
        try {
            for(File ff: dirs) {
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);

                // ignore .xx file
                if (ff.getName().startsWith(".")) {
                    continue;
                }

                if (ff.isDirectory()) {
                    File[] fbuf = ff.listFiles();
                    int buf = 0;
                    if (fbuf != null) {
                        buf = fbuf.length;
                    }
                    else {
                        buf = 0;
                    }
                    String num_item = String.valueOf(buf);
                    if (buf == 0) {
                        num_item = num_item + " item";
                    } else {
                        num_item = num_item + " items";
                    }
                    //String formated = lastModDate.toString();

                    dir.add(new FileExplorerItem(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), "tmp_directory_icon"));
                } else {
                    fls.add(new FileExplorerItem(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "tmp_file_icon"));
                }
            }
        } catch(Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase(Environment.getExternalStorageDirectory().getName())) {
            dir.add(0,new FileExplorerItem("..", "Parent Directory", "", f.getParent(), "tmp_directory_up"));
        }

        adapter = new FileExplorerItemListAdapter(FileExplorerActivity.this, R.layout.item_file, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        FileExplorerItem o = adapter.getItem(position);
        if (o.getImage().equalsIgnoreCase("tmp_directory_icon")||o.getImage().equalsIgnoreCase("tmp_directory_up")) {
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else {
            onFileClick(o);
        }
    }
    private void onFileClick(FileExplorerItem o)
    {
        Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("GetPath",currentDir.toString());
        intent.putExtra("GetFileName",o.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
