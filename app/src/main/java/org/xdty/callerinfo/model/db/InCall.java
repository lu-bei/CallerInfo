package org.xdty.callerinfo.model.db;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

public class InCall extends SugarRecord {
    String number;
    long time;
    long ringTime;
    long duration;

    @Ignore
    boolean isFetched = false;

    public InCall() {
    }

    public InCall(String number, long time, long ringTime, long duration) {
        this.number = number;
        this.time = time;
        this.ringTime = ringTime;
        this.duration = duration;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getRingTime() {
        return ringTime;
    }

    public void setRingTime(long ringTime) {
        this.ringTime = ringTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isFetched() {
        return isFetched;
    }

    public void setFetched(boolean isFetched) {
        this.isFetched = isFetched;
    }
}
