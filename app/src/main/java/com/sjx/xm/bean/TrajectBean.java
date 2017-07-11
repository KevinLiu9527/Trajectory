package com.sjx.xm.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 项目名称：Trajectory
 * 创建人：KevinLiu   E-mail:KevinLiu9527@163.com
 * 创建时间：2017/7/11 11:32
 * 描述：
 */
public class TrajectBean implements Serializable {

    private static final long serialVersionUID = 870453759717295093L;
    private List<HisTrackList> pos;

    private String result;

    private String msg;

    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<HisTrackList> getPos() {
        return pos;
    }

    public void setPos(List<HisTrackList> pos) {
        this.pos = pos;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "GetHisTrack [pos=" + pos + ", result=" + result + ", msg="
                + msg + "]";
    }
}
