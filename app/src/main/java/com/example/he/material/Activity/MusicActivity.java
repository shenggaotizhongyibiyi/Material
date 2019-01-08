package com.example.he.material.Activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.he.material.MODLE.GEDAN.Songs;
import com.example.he.material.MODLE.Music;
import com.example.he.material.R;
import com.example.he.material.Service.MyService;
import com.example.he.material.Controler.Utils;

import java.io.Serializable;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.internal.Util;

public class MusicActivity extends AppCompatActivity {

    private boolean isplaying = true;//播放标志
    public static String TAG = "MUSIC";
    private ImageButton mBack;
    private ImageButton mplay;
    private ImageButton mnext;
    private static SeekBar mSeekBar;
    private static TextView tv_progress;
    private static TextView tv_total;
    private static TextView music_title;
    private CircleImageView mCircleImageView;
    private TextView mTextView;
    private static MyService mService;
    private LinearLayout music_main;


    private Intent mIntent;
    private ServiceConnection mConn;
    private int mProgress;
    private int position_current;//当前播放位置
    private ObjectAnimator animator;
    private ViewPager mViewPager;
    //music对象属性
    private int id;
    private String name;
    private String imagepath;
    private int imageId;
    private String path;
    private String singer;
    private int size;
    private List<Music> mMusics_Local;
    private List<Songs> mMusics_Internet;
    private int click_item;
    private Toolbar mToolbar;
    private int State_from;


    @SuppressLint("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.musicplay);
        music_main = (LinearLayout) findViewById((R.id.music_main));
        music_title = (TextView) findViewById(R.id.music_title);
        mToolbar = (Toolbar) findViewById(R.id.mytoolbar_music);
        mBack = (ImageButton) findViewById(R.id.back);
        mplay = (ImageButton) findViewById(R.id.play);
        mnext = (ImageButton) findViewById(R.id.next);
        mSeekBar = (SeekBar) findViewById(R.id.sb);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        tv_total = (TextView) findViewById(R.id.tv_total);
        mCircleImageView = (CircleImageView) findViewById(R.id.a_1);
        mTextView = (TextView) findViewById(R.id.a_2);


        //接受从fragment传送过来的bundle（点击的位置）和music集合
        Intent intent_data = getIntent();
        mIntent = new Intent(this, MyService.class);
        //区分从local 和 internet 发送来的Intent
        if (intent_data.getStringExtra("from").equals("Local")) {
            State_from = 0;
            Bundle mbundle = intent_data.getBundleExtra("data");
            click_item = mbundle.getInt("itemId");//点击的位置编号
            Log.i(TAG, "点击的位置编号lcoal" + click_item);
            position_current = click_item;//当前播放位置等于点击位置
            mMusics_Local = (List<Music>) getIntent().getSerializableExtra("music");
            Log.i(TAG, "positionlocal" + click_item);
            //将数据传给service
            mIntent.putExtra("from", "Local");
            mIntent.putExtra("position", click_item);
            mIntent.putExtra("music", (Serializable) mMusics_Local);
            startService(mIntent);
            music_title.setText(mMusics_Local.get(position_current).getName());
            mCircleImageView.setImageBitmap(Utils.getArtAlbum(this, mMusics_Local.get(position_current).getImageId()));
        } else{
            State_from = 1;
            Bundle mbundle = intent_data.getBundleExtra("data");
            click_item = mbundle.getInt("itemId");//点击的位置编号
            Log.i(TAG, "点击的位置编号internet" + click_item);
            position_current = click_item;//当前播放位置等于点击位置
            mMusics_Internet = (List<Songs>) getIntent().getSerializableExtra("song");
            Log.i(TAG, "positioninternet" + click_item);
            //将数据传给service
            mIntent.putExtra("from", "Internet");
            mIntent.putExtra("position", click_item);
            mIntent.putExtra("song", (Serializable) mMusics_Internet);
            startService(mIntent);
            music_title.setText(mMusics_Internet.get(position_current).getTitle());
            System.out.print(mMusics_Internet.get(position_current).getTitle() + mMusics_Internet.get(position_current).getUrl());
            if (mCircleImageView != null) {
                Glide.with(this).load(mMusics_Internet.get(position_current).getPic()).into(mCircleImageView);
            }

        }

        // mCircleImageView.setImageBitmap(Utils.getArtAlbum(this,mMusics.get(position_current).getImageId()));
        //Glide.with(this).load(Utils.getArtAlbum(this,mMusics.get(position_current).getId()).into(mCircleImageView);

        //与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
        //通过这个IBinder对象，实现activity和Service的交互。
        mConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyService.MyBinder binder = (MyService.MyBinder) service;
                mService = binder.getService();
                //可以通过mService调用service中方法
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        //绑定服务
        bindService(mIntent, mConn, Service.BIND_AUTO_CREATE);
        //动画
        initanimator();


        //当从主activity切换至musicactivity时，歌曲自动开始播放，动画开始
        animator.start();
        mplay.setBackground(getResources().getDrawable(R.drawable.pause));


        //为播放键设置点击监听事件
        mplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isplaying = !isplaying;
                if (isplaying) {
                    mService.play();
                    mplay.setBackground(getResources().getDrawable(R.drawable.pause));
                    if (animator.isPaused()) {
                        animator.resume();
                    } else {
                        animator.start();
                    }
                } else {
                    mService.pause();
                    mplay.setBackground(getResources().getDrawable(R.drawable.play));
                    animator.pause();
                }

            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCircleImageView.setVisibility(View.INVISIBLE);
                mTextView.setVisibility(View.VISIBLE);
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setVisibility(View.INVISIBLE);
                mCircleImageView.setVisibility(View.VISIBLE);
            }
        });
        mnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (State_from == 0) {
                    if (position_current < (mMusics_Local.size() - 1)) {
                        position_current++;//如果选择下一首，那么当前播放位置加一
                        mCircleImageView.setImageBitmap(Utils.getArtAlbum(MusicActivity.this, mMusics_Local.get(position_current).getImageId()));
                        music_title.setText(mMusics_Local.get(position_current).getName());
                        animator.start();
                        mService.next();
                    } else {
                        Toast.makeText(MusicActivity.this, "已经是最后一首了", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (position_current < (mMusics_Internet.size() - 1)) {
                        position_current++;//如果选择下一首，那么当前播放位置加一

                        Glide.with(MusicActivity.this)
                                .load(mMusics_Internet.get(position_current).getPic())
                                .into(mCircleImageView);
                        music_title.setText(mMusics_Internet.get(position_current).getTitle());
                        animator.start();
                        mService.next();
                    } else {
                        Toast.makeText(MusicActivity.this, "已经是最后一首了", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (State_from == 0) {
                    if (position_current < (mMusics_Local.size() - 1)) {
                        position_current--;//如果选择上一首，那么当前播放位置加一
                        mCircleImageView.setImageBitmap(Utils.getArtAlbum(MusicActivity.this, mMusics_Local.get(position_current).getImageId()));
                        music_title.setText(mMusics_Local.get(position_current).getName());
                        animator.start();
                        mService.back();
                    } else {
                        Toast.makeText(MusicActivity.this, "已经是第一首了", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (position_current < (mMusics_Internet.size() - 1)) {
                        position_current--;//如果选择上一首，那么当前播放位置减一
                        Glide.with(MusicActivity.this).load(mMusics_Internet.get(position_current).getPic()).into(mCircleImageView);
                        music_title.setText(mMusics_Internet.get(position_current).getTitle());
                        animator.start();
                        mService.back();
                    } else {
                        Toast.makeText(MusicActivity.this, "已经是第一首了", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mProgress = mSeekBar.getProgress();
                mService.setProgress(mProgress);
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    @SuppressLint("HandlerLeak")
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            //歌曲的总时长(毫秒)
            int duration = bundle.getInt("duration");
            //歌曲的当前进度(毫秒)
            int currentPostition = bundle.getInt("currentPosition");
            //刷新滑块的进度
            mSeekBar.setMax(duration);
            mSeekBar.setProgress(currentPostition);

            //歌曲的总时长
            int minute = duration / 1000 / 60;
            int second = duration / 1000 % 60;
            String strMinute = null;
            String strSecond = null;
            //如果歌曲的时间中的分钟小于10
            if (minute < 10) {
                //在分钟的前面加一个0
                strMinute = "0" + minute;
            } else {
                strMinute = minute + "";
            }
            //如果歌曲的时间中的秒钟小于10
            if (second < 10) {
                //在秒钟前面加一个0
                strSecond = "0" + second;
            } else {
                strSecond = second + "";
            }
            tv_total.setText(strMinute + ":" + strSecond);

            //歌曲当前播放时长
            minute = currentPostition / 1000 / 60;
            second = currentPostition / 1000 % 60;
            //如果歌曲的时间中的分钟小于10
            if (minute < 10) {

                //在分钟的前面加一个0
                strMinute = "0" + minute;
            } else {
                strMinute = minute + "";
            }
            //如果歌曲的时间中的秒钟小于10
            if (second < 10) {
                //在秒钟前面加一个0
                strSecond = "0" + second;
            } else {
                strSecond = second + "";
            }
            tv_progress.setText(strMinute + ":" + strSecond);

        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConn);
    }

    public void initanimator() {
        animator = ObjectAnimator.ofFloat(mCircleImageView, "rotation", 0f, 359f);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(Animation.INFINITE);

    }

}