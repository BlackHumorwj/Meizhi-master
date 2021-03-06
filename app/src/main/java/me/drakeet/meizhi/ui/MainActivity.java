/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.drakeet.meizhi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.model.ConflictAlgorithm;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.meizhi.App;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.data.MeizhiData;
import me.drakeet.meizhi.data.entity.Gank;
import me.drakeet.meizhi.data.entity.Meizhi;
import me.drakeet.meizhi.data.休息视频Data;
import me.drakeet.meizhi.func.OnMeizhiTouchListener;
import me.drakeet.meizhi.ui.adapter.MeizhiListAdapter;
import me.drakeet.meizhi.ui.base.SwipeRefreshBaseActivity;
import me.drakeet.meizhi.util.AlarmManagers;
import me.drakeet.meizhi.util.Dates;
import me.drakeet.meizhi.util.Once;
import me.drakeet.meizhi.util.PreferencesLoader;
import me.drakeet.meizhi.util.Toasts;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

public class MainActivity extends SwipeRefreshBaseActivity {

    private static final int PRELOAD_SIZE = 6;

    @Bind(R.id.rv_meizhi)
    RecyclerView mRecyclerView;

    private MeizhiListAdapter mMeizhiListAdapter;
    private List<Meizhi>      mMeizhiList;
    private boolean mIsFirstTimeTouchBottom = true;
    private int     mPage                   = 1;
    private boolean mMeizhiBeTouched;


    @Override
    protected int provideContentViewId() {
        return R.layout.activity_main;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mMeizhiList = new ArrayList<>();

        //获取缓存数据
        QueryBuilder query = new QueryBuilder(Meizhi.class);
        query.appendOrderDescBy("publishedAt");
        query.limit(0, 10);
        mMeizhiList.addAll(App.sDb.query(query));


        setupRecyclerView();
        setupUmeng();
        AlarmManagers.register(this);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        new Handler().postDelayed(() -> setRefresh(true), 358);

        //加载数据 更新UI
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefresh(true);
            }
        },358);

        loadData(true);
    }

    /**
     * 设置友盟相关信息
     */
    private void setupUmeng() {
        UmengUpdateAgent.update(this);
        UmengUpdateAgent.setDeltaUpdate(false);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
    }

    /**
     * 设置RecycleView 的样式
     */
    private void setupRecyclerView() {
        //设置GridView 布局样式
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mMeizhiListAdapter = new MeizhiListAdapter(this, mMeizhiList);
        mRecyclerView.setAdapter(mMeizhiListAdapter);
        /*new Once(this).show("tip_guide_6", () -> {
            Snackbar.make(mRecyclerView, getString(R.string.tip_guide), Snackbar.LENGTH_INDEFINITE).setAction(R.string.i_know, v -> {
            }).show();
        });*/

        new Once(this).show("tip_guide_6", new Once.OnceCallback() {
            @Override
            public void onOnce() {
                Snackbar.make(mRecyclerView, getString(R.string.tip_guide), Snackbar.LENGTH_INDEFINITE).setAction(R.string.i_know, v -> {
                }).show();
            }
        });


        mRecyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        mMeizhiListAdapter.setOnMeizhiTouchListener(getOnMeizhiTouchListener());
    }


    /**
     * 获取服务数据
     *
     * @param clean 清除来自数据库缓存或者已有数据。
     */
    private void loadData(boolean clean) {
        mLastVideoIndex = 0;
        // @formatter:off
        Subscription s = Observable

                .zip(sGankIO.getMeizhiData(mPage), sGankIO.get休息视频Data(mPage), new Func2<MeizhiData, 休息视频Data, MeizhiData>() {
                    @Override
                    public MeizhiData call(MeizhiData meizhiData, 休息视频Data 休息视频Data) {
                        return createMeizhiDataWith休息视频Desc(meizhiData,休息视频Data);
                    }
                })
                //合并多个数据流，然后发送(Emit)最终合并的数据
                //.zip(sGankIO.getMeizhiData(mPage), sGankIO.get休息视频Data(mPage), this::createMeizhiDataWith休息视频Desc)//


                .map(new Func1<MeizhiData, List<Meizhi>>() {
                    @Override
                    public List<Meizhi> call(MeizhiData meizhiData) {
                        return meizhiData.results;
                    }
                })
                //事件对象的直接变换 一对一的转换 一个对象转为另一个对象
                //.map(meizhiData -> meizhiData.results)//


                .flatMap(new Func1<List<Meizhi>, Observable<Meizhi>>() {
                    @Override
                    public Observable<Meizhi> call(List<Meizhi> meizhis) {

                        return Observable.from(meizhis);
                    }
                })
                //将事件对象变换 返回一个新的Observable
                //.flatMap(Observable::from)//


                .toSortedList(new Func2<Meizhi, Meizhi, Integer>() {
                    @Override
                    public Integer call(Meizhi meizhi, Meizhi meizhi2) {
                        return meizhi2.publishedAt.compareTo(meizhi.publishedAt);
                    }
                })
                //排序
               // .toSortedList((meizhi1, meizhi2) -> meizhi2.publishedAt.compareTo(meizhi1.publishedAt))//




                .doOnNext(new Action1<List<Meizhi>>() {
                    @Override
                    public void call(List<Meizhi> meizhis) {
                        saveMeizhis(meizhis);
                    }
                })
                //保存/缓存网络结果
               // .doOnNext(this::saveMeizhis)//


                //指定线程
                .observeOn(AndroidSchedulers.mainThread())//


                // onCompleted 或者 onError 后被调用
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setRefresh(false);
                    }
                })
                //.finallyDo(() -> setRefresh(false))//




                .subscribe(new Action1<List<Meizhi>>() {
                    @Override
                    public void call(List<Meizhi> meizhis) {
                        if (clean) {
                            mMeizhiList.clear();
                        }
                        mMeizhiList.addAll(meizhis);
                        mMeizhiListAdapter.notifyDataSetChanged();
                        setRefresh(false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        loadError(throwable);
                    }
                });
                //观察者订阅被观察者  将被观察者的回调数据进行处理
               /* .subscribe(meizhis -> {
                    if (clean) {
                        mMeizhiList.clear();
                    }
                    mMeizhiList.addAll(meizhis);
                    mMeizhiListAdapter.notifyDataSetChanged();
                    setRefresh(false);
                }, throwable -> loadError(throwable));*/
        // @formatter:on
        addSubscription(s);
    }


    /**
     * 错误处理
     *
     * @param throwable
     */
    private void loadError(Throwable throwable) {
        throwable.printStackTrace();
        Snackbar.make(mRecyclerView, R.string.snap_load_fail, Snackbar.LENGTH_LONG).setAction(R.string.retry, v -> {
            requestDataRefresh();
        }).show();
    }


    private void saveMeizhis(List<Meizhi> meizhis) {
        App.sDb.insert(meizhis, ConflictAlgorithm.Replace);
    }


    /**
     *  将zip获取的数据进行合并处理
     * @param data
     * @param love
     * @return
     */
    private MeizhiData createMeizhiDataWith休息视频Desc(MeizhiData data, 休息视频Data love) {
        for (Meizhi meizhi : data.results) {
            meizhi.desc = meizhi.desc + " " + getFirstVideoDesc(meizhi.publishedAt, love.results);
        }
        return data;
    }


    private int mLastVideoIndex = 0;


    private String getFirstVideoDesc(Date publishedAt, List<Gank> results) {
        String videoDesc = "";
        for (int i = mLastVideoIndex; i < results.size(); i++) {
            Gank video = results.get(i);
            if (video.publishedAt == null)
                video.publishedAt = video.createdAt;
            if (Dates.isTheSameDay(publishedAt, video.publishedAt)) {
                videoDesc = video.desc;
                mLastVideoIndex = i;
                break;
            }
        }
        return videoDesc;
    }


    private void loadData() {
        loadData(/* clean */false);
    }


    private OnMeizhiTouchListener getOnMeizhiTouchListener() {

        return new OnMeizhiTouchListener() {
            @Override
            public void onTouch(View v, View meizhiView, View card, Meizhi meizhi) {
                if (meizhi == null)
                    return;
                if (v == meizhiView && !mMeizhiBeTouched) {
                    mMeizhiBeTouched = true;

                    Picasso.with(MainActivity.this).load(meizhi.url).fetch(new Callback() {

                        @Override
                        public void onSuccess() {
                            mMeizhiBeTouched = false;
                            startPictureActivity(meizhi, meizhiView);
                        }


                        @Override
                        public void onError() {
                            mMeizhiBeTouched = false;
                        }
                    });
                } else if (v == card) {
                    startGankActivity(meizhi.publishedAt);
                }
            }
        };

       /* return (v, meizhiView, card, meizhi) -> {
            if (meizhi == null)
                return;
            if (v == meizhiView && !mMeizhiBeTouched) {
                mMeizhiBeTouched = true;

                Picasso.with(this).load(meizhi.url).fetch(new Callback() {

                    @Override
                    public void onSuccess() {
                        mMeizhiBeTouched = false;
                        startPictureActivity(meizhi, meizhiView);
                    }


                    @Override
                    public void onError() {
                        mMeizhiBeTouched = false;
                    }
                });
            } else if (v == card) {
                startGankActivity(meizhi.publishedAt);
            }
        };*/
    }


    private void startGankActivity(Date publishedAt) {
        Intent intent = new Intent(this, GankActivity.class);
        intent.putExtra(GankActivity.EXTRA_GANK_DATE, publishedAt);
        startActivity(intent);
    }


    private void startPictureActivity(Meizhi meizhi, View transitView) {
        Intent intent = PictureActivity.newIntent(MainActivity.this, meizhi.url, meizhi.desc);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, transitView, PictureActivity.TRANSIT_PIC);
        try {
            ActivityCompat.startActivity(MainActivity.this, intent, optionsCompat.toBundle());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            startActivity(intent);
        }
    }


    @Override
    public void onToolbarClick() {
        mRecyclerView.smoothScrollToPosition(0);
    }


    @OnClick(R.id.main_fab)
    public void onFab(View v) {
        if (mMeizhiList != null && mMeizhiList.size() > 0) {
            startGankActivity(mMeizhiList.get(0).publishedAt);
        }
    }


    @Override
    public void requestDataRefresh() {
        super.requestDataRefresh();
        mPage = 1;
        loadData(true);
    }


    private void openGitHubTrending() {
        String url = getString(R.string.url_github_trending);
        String title = getString(R.string.action_github_trending);
        Intent intent = WebActivity.newIntent(this, url, title);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_notifiable);
        initNotifiableItemState(item);
        return true;
    }


    private void initNotifiableItemState(MenuItem item) {
        PreferencesLoader loader = new PreferencesLoader(this);
        item.setChecked(loader.getBoolean(R.string.action_notifiable, true));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_trending:
                openGitHubTrending();
                return true;
            case R.id.action_notifiable:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                PreferencesLoader loader = new PreferencesLoader(this);
                loader.saveBoolean(R.string.action_notifiable, isChecked);
                Toasts.showShort(isChecked ? R.string.notifiable_on : R.string.notifiable_off);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * RecyclerView 设置滚动监听
     *
     * @param layoutManager
     * @return
     */
    RecyclerView.OnScrollListener getOnBottomListener(StaggeredGridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                //是否到底部了
                boolean isBottom = layoutManager.findLastCompletelyVisibleItemPositions(new int[2])[1] >= mMeizhiListAdapter.getItemCount() - PRELOAD_SIZE;
                //上拉加载更多
                if (!mSwipeRefreshLayout.isRefreshing() && isBottom) {
                    //第一次时不调用
                    if (!mIsFirstTimeTouchBottom) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        mPage += 1;
                        loadData();
                    } else {
                        mIsFirstTimeTouchBottom = false;
                    }
                }
            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
