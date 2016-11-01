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

package me.drakeet.meizhi.ui.base;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import me.drakeet.meizhi.GankApi;
import me.drakeet.meizhi.DrakeetFactory;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.ui.AboutActivity;
import me.drakeet.meizhi.ui.WebActivity;
import me.drakeet.meizhi.util.Once;
import me.drakeet.meizhi.util.Toasts;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by drakeet on 8/9/15.
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * 工厂模式获取 GankApi
     */
    public static final GankApi sGankIO = DrakeetFactory.getGankIOSingleton();

    private CompositeSubscription mCompositeSubscription;


    /**
     * 初始化一组订阅对象容器 可以添加多个订阅者
     * @return 订阅者容器
     */
    public CompositeSubscription getCompositeSubscription() {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }

        return this.mCompositeSubscription;
    }

    /**
     * 添加订阅者对象到订阅容器
     * @param s 单个的订阅者对象
     */
    public void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }
        this.mCompositeSubscription.add(s);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_login:
                loginGitHub();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void loginGitHub() {
        new Once(this).show(R.string.action_github_login, () -> {
            Toasts.showLongX2(getString(R.string.tip_login_github));
        });
        String url = getString(R.string.url_login_github);
        Intent intent = WebActivity.newIntent(this, url, getString(R.string.action_github_login));
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mCompositeSubscription != null) {
            //取消订阅容器中所有的订阅对象
            this.mCompositeSubscription.unsubscribe();
        }
    }
}
