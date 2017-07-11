package com.sjx.xm.utils;

import java.io.Serializable;

/**
 * 项目名称：Trajectory
 * 创建人：KevinLiu   E-mail:KevinLiu9527@163.com
 * 创建时间：2017/7/11 11:55
 * 描述：轨迹事件处理类
 */
public class TrajectoryEventMsg implements Serializable {
    private static final long serialVersionUID = -8924727466196577828L;

    // 是否显示默认设备的轨迹
    private boolean isDefault = true;
    private String imei;

    private TarckOrder tarckOrder = new TarckOrder();

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public TarckOrder getTarckOrder() {
        return tarckOrder;
    }

    public void setTarckOrder(TarckOrder tarckOrder) {
        this.tarckOrder = tarckOrder;
    }

    public class TarckOrder implements Serializable {
        private int order = 0;
        private int maxProgress = 0;
        private int progress = 0;

        public int getOrder() {
            return order;
        }

        public int getMaxProgress() {
            return maxProgress;
        }

        public void setMaxProgress(int maxProgress) {
            this.maxProgress = maxProgress;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }
}
