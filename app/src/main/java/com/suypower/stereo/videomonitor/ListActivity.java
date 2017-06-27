package com.suypower.stereo.videomonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends Activity {
    private ImageView btnreturn;
    private ListView listView;
    private List<Map<String, String>> mapList;
    private Myadpter myadpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        /**
         * 返回
         */
        btnreturn = (ImageView) findViewById(R.id.btnreturn);
        btnreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        listView = (ListView) findViewById(R.id.listview);

        mapList = new ArrayList<>();

        Cursor cursor = SuyDB.getSuyDB().getDb().rawQuery("select * from video order by createdt desc", null);

        while (cursor.moveToNext()) {

            Map<String, String> map = new HashMap<>();
            map.put("uuid", cursor.getString(0));
            map.put("state", String.valueOf(cursor.getInt(1)));
            map.put("duration", String.valueOf(cursor.getInt(2)));
            map.put("createdt", cursor.getString(3));
            mapList.add(map);
        }
        cursor.close();

        myadpter = new Myadpter();
        listView.setAdapter(myadpter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, String> map = mapList.get(i);
                Intent intent=new Intent(ListActivity.this,VideoActivity.class);
                intent.putExtra("url",Environment.getExternalStorageDirectory() + "/录像/" + map.get("uuid") + ".mp4");
                startActivity(intent);

            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int ii, long l) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定删除数据吗？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Map<String, String> map = mapList.get(ii);
                        File file = new File(Environment.getExternalStorageDirectory() + "/录像/" + map.get("uuid") + ".mp4");
                        file.delete();
                        file = new File(Environment.getExternalStorageDirectory() + "/录像/" + map.get("uuid") + ".mp3");
                        file.delete();
                        SuyDB.getSuyDB().getDb().delete("video","uuid = ?",new String[]{map.get("uuid")});
                        mapList.remove(ii);
                        myadpter.notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("取消",null);
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
                return false;
            }
        });

    }

    class Myadpter extends BaseAdapter {
        @Override
        public int getCount() {
            return mapList.size();
        }

        @Override
        public Object getItem(int i) {
            return mapList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            TextView name, state, duration;
            LayoutInflater layoutInflater = LayoutInflater.from(ListActivity.this);
            view = layoutInflater.inflate(R.layout.task_cell, null);
            name = (TextView) view.findViewById(R.id.name);
            state = (TextView) view.findViewById(R.id.state);
            duration = (TextView) view.findViewById(R.id.duration);


            Map<String, String> map = mapList.get(i);
            name.setText(map.get("uuid"));
            duration.setText(map.get("duration") + " " + "秒");
            if (map.get("state").equals("1")) {
                state.setText("已经处理");
                state.setTextColor(getResources().getColor(R.color.green1));
            } else {
                state.setText("正在处理");
                state.setTextColor(getResources().getColor(R.color.red1));
            }
            return view;
        }
    }

}
