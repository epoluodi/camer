package com.suypower.stereo.videomonitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Stereo on 2017/3/29.
 */

public class MenuView {

    private View menuview;
    private Boolean IsShow;
    private ImageView play,stop,list,setting;
    private MenuInterFace menuInterFace;


    public MenuView(Context context,MenuInterFace menuInterFace)
    {
        this.menuInterFace=menuInterFace;
        menuview = LayoutInflater.from(context).inflate(R.layout.menu_layout,null);
        play = (ImageView)menuview.findViewById(R.id.play);
        stop = (ImageView)menuview.findViewById(R.id.stop);
        list = (ImageView)menuview.findViewById(R.id.list);
        setting = (ImageView)menuview.findViewById(R.id.setting);
        play.setOnClickListener(onClickListener);
        stop.setOnClickListener(onClickListener);
        list.setOnClickListener(onClickListener);
        setting.setOnClickListener(onClickListener);


        IsShow = false;
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId())
            {
                case R.id.play:
                    menuInterFace.OnMenuItem(1);
                    break;
                case R.id.stop:
                    menuInterFace.OnMenuItem(2);
                    break;
                case R.id.list:
                    menuInterFace.OnMenuItem(3);
                    break;
                case R.id.setting:
                    menuInterFace.OnMenuItem(4);
                    break;

            }
        }
    };

    public View getMenuView()
    {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        menuview.setLayoutParams(layoutParams);

        IsShow = true;
        return menuview;
    }

    public Boolean getShow() {
        return IsShow;
    }

    public void setShow(Boolean show) {
        IsShow = show;
    }

    public void setPlayState(boolean b)
    {
        play.setEnabled(!b);
        stop.setEnabled(b);
    }



    public interface MenuInterFace
    {
        void OnMenuItem(int i);
    }
}
