package com.sjx.xm;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.google.gson.Gson;
import com.sjx.xm.bean.HisTrackList;
import com.sjx.xm.bean.TrajectBean;
import com.sjx.xm.utils.HandleConstants;
import com.sjx.xm.utils.TrackMapTools;
import com.sjx.xm.utils.TrajectoryEventMsg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author：KevinLiu
 * @E-mail:KevinLiu9527@163.com
 * @time 2017/7/11 14:24
 * 备注：  轨迹播放
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private Gson gson;
    private MapView trackMV;
    private BaiduMap trackBaiduMap;
    private TrackMapTools trackMapTools;
    private Button startBT, pauseBT, speedUpBT, slowDownBT;
    private ImageView satellite;
    private CheckBox gpsImg;
    // 间隔
    private int interval = 1000;
    List<HisTrackList> playPos = new ArrayList<>();
    List<HisTrackList> allPos = new ArrayList<>();
    private boolean isAllLoc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidget();
        context = this;
        gson = new Gson();
        initMap();
        initView();
    }

    /**
     * 初始化地图
     */
    private void initMap() {
        trackMV.showZoomControls(false);
        trackMV.showScaleControl(false);
        trackBaiduMap = trackMV.getMap();
        trackMapTools = new TrackMapTools(this, trackMV);
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        trackMV = (MapView) findViewById(R.id.track_MV);
        startBT = (Button) findViewById(R.id.start_bt);
        startBT.setOnClickListener(this);
        pauseBT = (Button) findViewById(R.id.pause_bt);
        pauseBT.setOnClickListener(this);
        speedUpBT = (Button) findViewById(R.id.speed_up_bt);
        speedUpBT.setOnClickListener(this);
        slowDownBT = (Button) findViewById(R.id.slow_down_bt);
        slowDownBT.setOnClickListener(this);
        satellite = (ImageView) findViewById(R.id.imageView);
        satellite.setOnClickListener(this);
        gpsImg = (CheckBox) findViewById(R.id.imageView2);
        gpsImg.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void TrackEventMsg(TrajectoryEventMsg eventMsg) {
        TrajectoryEventMsg.TarckOrder tarckOrder = eventMsg.getTarckOrder();
        switch (tarckOrder.getOrder()) {
            case HandleConstants.PLAY_TRACK:
                break;
            case HandleConstants.PAUSE_TRACK:
                break;
            case HandleConstants.RESET_TRACK:
                break;
            case HandleConstants.STOP_TRACK:
                break;
        }
    }

    @Override
    protected void onPause() {
        trackMV.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        trackMV.onDestroy();
        trackMapTools.stopTrackMap();
        trackMapTools = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        trackMV.onResume();
        super.onResume();
    }

    /**
     * 初始化数据
     */
    private void initView() {
        try {
            StringBuffer sbuffer = new StringBuffer();
            BufferedReader rader = new BufferedReader(
                    new InputStreamReader(
                            context.getClassLoader().getResourceAsStream("assets/tracjt.json")));
            String text = "";
            while (null != (text = rader.readLine())) {
                sbuffer.append(text);
            }
            TrajectBean trajectBean = gson.fromJson(sbuffer.toString(), TrajectBean.class);
            String msg = trajectBean.getMsg();
            String time = trajectBean.getTime();
            //添加数据
            List<HisTrackList> hisTrackList = trajectBean.getPos();
            if (null != hisTrackList && hisTrackList.size() > 2) {
                allPos = hisTrackList;
                initLines();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化线
     */
    private void initLines() {
        if (isAllLoc == true) {
            playPos.clear();
            playPos.addAll(allPos);
        } else {
            playPos.clear();
            gpsPos();
        }
        trackMapTools.playTrajectoryLine(playPos);
    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_bt://开始
                trackMapTools.playTrajectoryMarker(HandleConstants.PLAY_TRACK);
                break;
            case R.id.pause_bt://暂停
                trackMapTools.playTrajectoryMarker(HandleConstants.PAUSE_TRACK);
                break;
            case R.id.speed_up_bt://加速
                if (interval <= 500) {
                    Toast.makeText(MainActivity.this, "已经达到最大速度!", Toast.LENGTH_SHORT).show();
                    return;
                }
                interval -= 500;
                trackMapTools.setSpeed(interval);
                break;
            case R.id.slow_down_bt://减速
                if (interval > 2000) {
                    Toast.makeText(MainActivity.this, "已经达到最小速度!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                interval += 500;
                trackMapTools.setSpeed(interval);
                break;
            case R.id.imageView://卫星图
                if (trackBaiduMap.getMapType() == BaiduMap.MAP_TYPE_NORMAL)
                    trackBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                else trackBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.imageView2://GPS
                if (gpsImg.isChecked() == true) {
                    //卫星和GPS点
                    isAllLoc = true;
                    gpsImg.setText("全部");
                } else {
                    //GPS点
                    isAllLoc = false;
                    gpsImg.setText("GPS点");
                }
                trackBaiduMap.clear();
                initLines();
                break;
        }
    }

    /**
     * 筛选GPS点
     */
    private void gpsPos() {
        for (int i = 0; i < allPos.size(); i++) {
            HisTrackList his = allPos.get(i);
            if ("0".equals(his.getMethod())) {
                playPos.add(his);
            }
        }
    }


}
