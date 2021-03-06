package com.example.he.material.Fragment_List;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.example.he.material.Activity.SongSheetActivity;
import com.example.he.material.Adapter.MainAdapter;
import com.example.he.material.MODLE.Data;
import com.example.he.material.MODLE.JsonRootBean;
import com.example.he.material.MODLE.Song;
import com.example.he.material.R;
import com.example.he.material.Utils.GlideImageLoader;
import com.google.gson.Gson;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.youth.banner.Banner;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * project: Material
 * author : Android研发部_姓名
 * date : 2019/1/15
 * time : 14:09
 * email : 企业邮箱
 * note : 说明
 */
public class MainFragment extends Fragment {


    public static MainFragment newInstance() {
        MainFragment newFragment = new MainFragment();
        Bundle bundle = new Bundle();
        newFragment.setArguments(bundle);
        return newFragment;

    }

    private XRecyclerView mXRlist;
    private Banner banner;
    private MainAdapter mainAdapter;
    private ArrayList<Data> mTempList;
    private List<Data> mSheetList;
    private OkHttpClient client;
    private JsonRootBean mJsonData;
    private NestedScrollView scrollView;
    private List<Data> temp;
    private int index = 10;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        client = new OkHttpClient.Builder().build();
        List<Drawable> images = new ArrayList<>();
        images.add(getResources().getDrawable(R.drawable.caomei));
        images.add(getResources().getDrawable(R.drawable.chengzi));
        images.add(getResources().getDrawable(R.drawable.xiangjiao));
        mXRlist = view.findViewById(R.id.main_recycler);



        mSheetList = new ArrayList<>();
        mTempList = new ArrayList<>();
        mainAdapter = new MainAdapter(mSheetList, getContext(), new MainAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(getContext(), SongSheetActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("CLICK_SONG_SHEET_POSITION", mSheetList.get(position));
                intent.putExtra("DATA", bundle);
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        requestSongList();
        View header =  LayoutInflater.from(getContext()).inflate(R.layout.item_main_head, null,false);
        banner=header.findViewById(R.id.banner);
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(images);
        banner.setDelayTime(4500);
        banner.setBannerAnimation(Transformer.ScaleInOut);
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                Toast.makeText(getContext(), "第" + position + "个", Toast.LENGTH_SHORT).show();
            }
        });
        //banner设置方法全部调用完毕时最后调用
        banner.start();
        mXRlist.addHeaderView(header);
        mXRlist.setAdapter(mainAdapter);
        mXRlist.setLayoutManager(layoutManager);
        mXRlist.setLoadingMoreProgressStyle(ProgressStyle.Pacman);
        mXRlist.setHasFixedSize(true);
        mXRlist.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                if (index + 10 < mTempList.size()) {
                    for (int i = index; i < index + 10; i++) {
                        mSheetList.add(mTempList.get(i));
                    }
                    index = index + 10;
                    mainAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "已经到底了", Toast.LENGTH_LONG).show();
                }
                mXRlist.loadMoreComplete();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void requestSongList() {
        Request request = new Request.Builder()
                .url("https://api.bzqll.com/music/netease/hotSongList?key=579621905&cat=全部&limit=100&offset=0")
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    final String str = response.body().string();
                    Gson gson = new Gson();
                    mJsonData = gson.fromJson(str, JsonRootBean.class);
                    temp = mJsonData.getData();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (temp != null && mTempList != null) {
                                mTempList.clear();
                                mSheetList.clear();
                                mTempList.addAll(temp);
                                for (int i = 0; i < index + 10; i++) {
                                    mSheetList.add(mTempList.get(i));
                                }
                                mainAdapter.notifyDataSetChanged();
                            }
                        }
                    });

                }

            }
        });

    }
}
