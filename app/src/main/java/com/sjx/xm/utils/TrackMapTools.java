package com.sjx.xm.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.sjx.xm.R;
import com.sjx.xm.bean.HisTrackList;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 项目名称：Trajectory
 * 创建人：KevinLiu   E-mail:KevinLiu9527@163.com
 * 创建时间：2017/7/11 11:45
 * 描述：历史轨迹处理类
 */
public class TrackMapTools implements OnGetGeoCoderResultListener, BaiduMap.OnMarkerClickListener {

    private Context context = null;
    private LayoutInflater mInflater = null;
    private GeoCoder geoCoder = null;
    private BitmapDescriptor baiduPoliceBitmap = null;
    private BitmapDescriptor baiduPoliceLineBitmap = null;
    private Polyline polyLine;
    private LatLng frontLatlng;
    private ArrayMap<Integer, BaiduInfoWindow> trackInfoWindows = new ArrayMap<>();
    private BaiduInfoWindow nowShowBaiduInfoWindow;
    public int TIME_INTERVAL = 1000;

    //默认
    private TextView markerPopTimeTV;
    private TextView markerPopDirectionTV;
    private TextView markerPopSpeedTV;
    private TextView markerPopStayedTV;

    private HandlerThread trackThread = null;
    private Handler trackHandler = null;
    // 0:初始状态,1:播放,2：暂停，3：停止
    private int nowState = 0;

    private int playIndex = -1;

    private Thread playTrackThread = null;
    private BaiduMap baiduMap;

    /**
     * 构造方法
     */
    public TrackMapTools(final Context context, MapView trackMV) {
        this.context = context;
        this.baiduMap = trackMV.getMap();
        mInflater = LayoutInflater.from(context);
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);
        baiduPoliceBitmap = BitmapDescriptorFactory.fromResource(R.mipmap.track_marker_icon);
        baiduPoliceLineBitmap = BitmapDescriptorFactory.fromAsset("icon_road_blue_arrow.png");
        baiduMap.setOnMarkerClickListener(this);
        nowState = HandleConstants.RESET_TRACK;

        //
        trackThread = new HandlerThread("Track");
        trackThread.start();
        // 控制UI线程
        trackHandler = new Handler(trackThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    //开始播放
                    case HandleConstants.PLAY_TRACK:
                        if (playIndex == 0) {
                            removeMarkers();
                        }
                        frontLatlng = polyLine.getPoints().get(playIndex);
                        OverlayOptions option = new MarkerOptions().position(frontLatlng).icon(baiduPoliceBitmap);
                        Marker marker = (Marker) baiduMap.addOverlay(option);
                        trackInfoWindows.get(playIndex).setMarker(marker);

                        TrajectoryEventMsg trajectoryEventMsg = new TrajectoryEventMsg();
                        showInfoWindow(marker.getPosition());

                        TrajectoryEventMsg.TarckOrder tarckOrder = trajectoryEventMsg.getTarckOrder();
                        tarckOrder.setOrder(HandleConstants.PLAY_TRACK);
                        tarckOrder.setMaxProgress(polyLine.getPoints().size() - 1);
                        tarckOrder.setProgress(playIndex);
                        EventBus.getDefault().post(trajectoryEventMsg);
                        break;
                    case HandleConstants.PAUSE_TRACK:
                        break;
                    //停止播放
                    case HandleConstants.STOP_TRACK:

                        TrajectoryEventMsg stopTrajectoryEventMsg = new TrajectoryEventMsg();
                        stopTrajectoryEventMsg.getTarckOrder().setOrder(HandleConstants.STOP_TRACK);
                        EventBus.getDefault().post(stopTrajectoryEventMsg);
                        break;
                    //重置
                    case HandleConstants.RESET_TRACK:
                        playIndex = -1;
                        EventBus.getDefault().post(new TrajectoryEventMsg());
                        break;
                }
            }
        };
        //  给UI线程发送指令线程
        playTrackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (nowState != HandleConstants.OVER_TRACK) {
                    if (nowState == HandleConstants.PLAY_TRACK) {
                        playIndex = playIndex + 1;
                        // 播放完毕后判断
                        if (null != polyLine && polyLine.getPoints().size() <= playIndex) {
                            nowState = HandleConstants.STOP_TRACK;
                        }
                    }
                    trackHandler.sendEmptyMessage(nowState);
                    if (null != playTrackThread) {
                        try {
                            playTrackThread.sleep(TIME_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        playTrackThread.start();
    }

    /**
     * 设置播放速度
     */
    public void setSpeed(int speed) {
        TIME_INTERVAL = speed;
    }

    /**
     * 播放轨迹
     */
    public void playTrajectoryMarker(int operation) {
        nowState = operation;
        baiduMap.hideInfoWindow();
    }

    /**
     * 停止播放
     */
    public void stopTrackMap() {
        baiduMap.removeMarkerClickListener(this);
        geoCoder.destroy();
        if (baiduPoliceBitmap != null) {
            baiduPoliceBitmap.recycle();
            baiduPoliceBitmap = null;
        }
        if (trackThread != null) {
            trackThread.quitSafely();
            trackThread = null;
        }
        if (null != trackHandler) {
            trackHandler.removeCallbacksAndMessages(null);
            trackHandler = null;
        }
        trackInfoWindows.clear();
        nowState = HandleConstants.OVER_TRACK;
        playIndex = -1;
        baiduMap.clear();
        if (playTrackThread != null) {
            playTrackThread.interrupt();
            playTrackThread = null;
        }
        if (null != baiduPoliceLineBitmap) {
            baiduPoliceLineBitmap.recycle();
            baiduPoliceLineBitmap = null;
        }
    }

    /**
     * 轨迹画线
     */
    public void playTrajectoryLine(List<HisTrackList> tracks) {
        if (null == tracks || tracks.size() < 2) {
            return;
        }
        // 重置播放
        nowState = HandleConstants.RESET_TRACK;
        baiduMap.clear();
        List<LatLng> polyLines = new ArrayList<>();
        List<BaiduInfoWindow> baiduInfoWindowList = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            HisTrackList dateBean = tracks.get(i);
            Float lat = Float.valueOf(dateBean.getLat());
            Float lng = Float.valueOf(dateBean.getLng());
            LatLng latLng = gpsToBaiduCoordinate(new LatLng(lat, lng));
            // 收集InfoWindow
            BaiduInfoWindow biw = new BaiduInfoWindow();
            View view = mInflater.inflate(R.layout.map_layout_track_markerpop, null);
            biw.setContentView(view);
            biw.setLatLng(latLng);
            biw.setmInfoWindow(new InfoWindow(view, latLng, -baiduPoliceBitmap.getBitmap().getHeight()));
            biw.setDeviceBean(dateBean);
            // 更新pop内容
            updatePop(biw.getDeviceBean(), biw.getContentView());
            baiduInfoWindowList.add(biw);
            // 收集画线用点
            polyLines.add(latLng);
        }
        // 销毁Markers
        removeMarkers();
        // 清空Map集合
        trackInfoWindows.clear();
        // 收集轨迹对象
        for (int i = 0; i < baiduInfoWindowList.size(); i++) {
            trackInfoWindows.put(i, baiduInfoWindowList.get(i));
        }
        // 画线
        OverlayOptions polyline = new PolylineOptions().width(10)
                .points(polyLines).dottedLine(true).customTexture(baiduPoliceLineBitmap);
        polyLine = (Polyline) baiduMap.addOverlay(polyline);
        // 显示全部轨迹线
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < polyLines.size(); i++) {
            builder.include(polyLines.get(i));
        }
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()));
    }

    /**
     * 清空markers
     */
    private void removeMarkers() {
        if (null != trackInfoWindows && trackInfoWindows.size() > 0) {
            for (int i = 0; i < trackInfoWindows.size(); i++) {
                Marker marker = trackInfoWindows.get(i).getMarker();
                if (null != marker) {
                    marker.remove();
                }
            }
        }
    }

    /**
     * 更新pop框
     */
    private void updatePop(HisTrackList deviceBean, View contentView) {
        markerPopTimeTV = (TextView) contentView.findViewById(R.id.marker_pop_time_TV);
        markerPopDirectionTV = (TextView) contentView.findViewById(R.id.marker_pop_direction_TV);
        markerPopSpeedTV = (TextView) contentView.findViewById(R.id.marker_pop_speed_TV);
        markerPopStayedTV = (TextView) contentView.findViewById(R.id.marker_pop_stayed_TV);
        // 时间
        String time = unixTotime(Long.valueOf(deviceBean.getTime()));
        markerPopTimeTV.setText("时间: " + time);
        // 方向
        markerPopDirectionTV.setText("方向: " + direction(deviceBean.getDir()));
        // 速度
        markerPopSpeedTV.setText("速度: " + deviceBean.getSpeed() + "km/h");
        markerPopStayedTV.setText("停留时间: " + deviceBean.getStayed_time() + "分钟");
    }

    /**
     * 设备方向
     */
    private String direction(String dir) {
        int dirInt = Integer.parseInt(dir);
        if (0 <= dirInt && dirInt < 90) {
            dir = "正北";
        } else if (90 <= dirInt && dirInt < 180) {
            dir = "正东";
        } else if (180 <= dirInt && dirInt < 270) {
            dir = "正南";
        } else if (270 <= dirInt && dirInt <= 360) {
            dir = "正西";
        }
        return dir;
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        TextView addressTV = (TextView) nowShowBaiduInfoWindow.getContentView().findViewById(R.id.marker_pop_address_TV);
        addressTV.setText("地址: " + reverseGeoCodeResult.getAddress());
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(reverseGeoCodeResult.getLocation()));
        baiduMap.showInfoWindow(nowShowBaiduInfoWindow.getmInfoWindow());
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showInfoWindow(marker.getPosition());
        return true;
    }

    /**
     * 检索显示泡泡
     */
    private void showInfoWindow(LatLng latLng) {
        try {
            Set<Integer> key = trackInfoWindows.keySet();
            Iterator<Integer> iterator = key.iterator();
            while (iterator.hasNext()) {
                Integer keyInt = iterator.next();
                BaiduInfoWindow baiduInfoWindow = trackInfoWindows.get(keyInt);
                if (null != baiduInfoWindow) {
                    if (baiduInfoWindow.getMarker() == null) {
                        String s = String.valueOf(playIndex);
                    }
                    if (isSamePosition(latLng, baiduInfoWindow.getMarker().getPosition())) {
                        nowShowBaiduInfoWindow = baiduInfoWindow;
                        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断两个坐标点是否相同
     */
    private boolean isSamePosition(LatLng latlng1, LatLng latlng2) {
        if (latlng1.latitude == latlng2.latitude && latlng1.longitude == latlng2.longitude) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 时间处理
     */
    public static String unixTotime(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time * 1000));
    }

    /**
     * 获取转化的百度坐标系
     * 将GPS设备采集的原始GPS坐标转换成百度坐标
     */
    public static LatLng gpsToBaiduCoordinate(LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(sourceLatLng);
        return converter.convert();
    }

}
