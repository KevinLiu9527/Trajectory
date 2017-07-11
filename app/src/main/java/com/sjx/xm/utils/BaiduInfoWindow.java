package com.sjx.xm.utils;

import android.view.View;

import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.sjx.xm.bean.HisTrackList;

import java.io.Serializable;

/**
 * 项目名称：Trajectory
 * 创建人：KevinLiu   E-mail:KevinLiu9527@163.com
 * 创建时间：2017/7/11 11:46
 * 描述：pop框提示
 */
public class BaiduInfoWindow implements Serializable {
    private static final long serialVersionUID = -1470992082595368113L;
    private InfoWindow mInfoWindow;

    private View contentView;

    private LatLng latLng;

    private HisTrackList deviceBean;

    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public HisTrackList getDeviceBean() {
        return deviceBean;
    }

    public void setDeviceBean(HisTrackList deviceBean) {
        this.deviceBean = deviceBean;
    }

    public InfoWindow getmInfoWindow() {
        return mInfoWindow;
    }

    public void setmInfoWindow(InfoWindow mInfoWindow) {
        this.mInfoWindow = mInfoWindow;
    }

    public View getContentView() {
        return contentView;
    }

    public void setContentView(View contentView) {
        this.contentView = contentView;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
