package com.wavlite.WLAndroid;

import java.util.Date;

/**
 * Created by javen on 11/10/15.
 */
public class TrialPeriodTimer  {

    private long startDate;
    private long enddate;
    private Boolean isTrialOver;


    public boolean getIsTrialOver() {

        if (isTrialOver == null) {
            return true;
        } else {
            return isTrialOver;
        }

    }

    public void setIsTrialOver(boolean isTrialOver) {
        this.isTrialOver = isTrialOver;
    }

    public long getEnddate() {
        return enddate;
    }

    public void setStartDate(Date startDate) {

        this.startDate = startDate.getTime();

    }
    public void setEndDate(Date startDate) {

        enddate = startDate.getTime() + 2419000000L;
    }
}
