package com.sjx.xm.bean;

import java.io.Serializable;

/**
 * @author：KevinLiu
 * @E-mail:KevinLiu9527@163.com
 * @time 2017/7/11 15:55
 * 备注：历史轨迹内容
 */
public class HisTrackList implements Serializable {

    private static final long serialVersionUID = 1969768784139171948L;
    private String lng;
    private String lat;
    private String time;
    /*速度 */
    private String speed;
    /*模式 GPS:0; 基站:1; wifi:2、3; */
    private String method;
    /*停留时间*/
    private String stayed_time;
    /*车位方向0:正北方向90: 正东方向180: 正南方向270: 正西方向*/
    private String dir;

    private String address;
    private String satellites;//卫星个数
    private String signal;//手机信号强度

    public String getSatellites() {
        return satellites;
    }

    public void setSatellites(String satellites) {
        this.satellites = satellites;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStayed_time() {
        return stayed_time;
    }

    public void setStayed_time(String stayed_time) {
        this.stayed_time = stayed_time;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

}
