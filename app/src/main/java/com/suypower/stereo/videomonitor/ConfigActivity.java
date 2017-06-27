package com.suypower.stereo.videomonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class ConfigActivity extends Activity {

    private ImageView btnreturn;
    private ListView listView;
    private Myadpter myadpter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        /**
         * 返回
         */
        btnreturn = (ImageView)findViewById(R.id.btnreturn);
        btnreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listView = (ListView)findViewById(R.id.listview);


        myadpter = new Myadpter();
        listView.setAdapter(myadpter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i)
                {
                    case 0:


                        final AlertDialog.Builder builder=new AlertDialog.Builder(ConfigActivity.this);
                        builder.setTitle("提示");
                        builder.setMessage("确定删除数据吗？");
                        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                SuyDB.getSuyDB().getDb().delete("video",null,null);
                                File mainfile = new File(Environment.getExternalStorageDirectory() + "/录像");
                                File[] files=mainfile.listFiles();
                                float filesize=0;
                                for (File file:files)
                                {
                                    file.delete();
                                }

//                                mainfile = new File(Environment.getExternalStorageDirectory() + "/录像");
//                                files=mainfile.listFiles();
//                                for (File file:files)
//                                {
//                                    file.delete();
//                                }

                                myadpter.notifyDataSetChanged();
                                dialogInterface.dismiss();

                            }
                        });
                        builder.setNegativeButton("取消",null);
                        AlertDialog alertDialog=builder.create();
                        alertDialog.show();


                        break;
                    case 2:
                        System.exit(0);

                        break;
                }
            }
        });


    }




    class Myadpter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            TextView name,info;
            LayoutInflater layoutInflater= LayoutInflater.from(ConfigActivity.this);
            view = layoutInflater.inflate(R.layout.config_cell,null);
            name = (TextView)view.findViewById(R.id.name);
            info = (TextView) view.findViewById(R.id.info);
            switch (i)
            {
                case 0:
                    name.setText("清除数据");
                    File mainfile = new File(Environment.getExternalStorageDirectory() + "/录像");
                    File[] files=mainfile.listFiles();
                    float filesize=0;
                    for (File file:files)
                    {
                        filesize += file.length();
                    }

//                    mainfile = new File(Environment.getExternalStorageDirectory() + "/录像/声音");
//                    files=mainfile.listFiles();
//                    for (File file:files)
//                    {
//                        filesize += file.length();
//                    }


                    filesize = filesize/1024/1024;
                    String s;
                    if (filesize<1)
                        s = new java.text.DecimalFormat("#0.00 KB").format(filesize);
                    else
                        s = new java.text.DecimalFormat("#0.00 MB").format(filesize);
                    info.setText(s);


                    SuyDB.getSuyDB().getDb().delete("video",null,null);

                    break;
                case 2:
                    name.setText("退出APP");
                    info.setText("");
                    break;
                case 1:
                    name.setText("软件版本");
                    try {
                        PackageManager manager = getPackageManager();
                        PackageInfo verinfo = manager.getPackageInfo(getPackageName(), 0);
                        info.setText("V" + verinfo.versionName);
                    }
                    catch (Exception e)
                    {e.printStackTrace();}
                    break;
            }

            return view;
        }
    }

}
