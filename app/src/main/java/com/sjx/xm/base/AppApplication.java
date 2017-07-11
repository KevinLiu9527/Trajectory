package com.sjx.xm.base;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * 项目名称：Trajectory
 * 创建人：KevinLiu   E-mail:KevinLiu9527@163.com
 * 创建时间：2017/7/11 11:04
 * 描述：应用类
 */
public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
