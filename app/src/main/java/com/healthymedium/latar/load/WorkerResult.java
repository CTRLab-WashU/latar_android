package com.healthymedium.latar.load;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class WorkerResult {

    public Long min;
    public Long max;
    public Long sum;
    public Long count;

    public Double avg;
    public Double variance;
    public Double stddeviation;

    public String startTime;
    public String stopTime;
    public Long duration;

    public String timeUtilization;

    transient List<Long> deltas;

    public WorkerResult(DateTime start, DateTime stop, List<Long> deltas) {
        duration = stop.getMillis() - start.getMillis();
        startTime = start.toString();
        stopTime = stop.toString();
        this.deltas = deltas;

        count = Long.valueOf(deltas.size());
        if(count==0){
            return;
        }

        sum = Long.valueOf(0);
        max = deltas.get(0);
        min = deltas.get(0);

        for(Long val : deltas){
            if(val>max){
                max = val;
            }
            if(val<min){
                min = val;
            }
            sum += val;
        }

        avg = Double.valueOf((sum))/count;

        double sumv = 0;
        for(Long val : deltas){
            sumv += pow((val - avg), 2);
        }

        variance = sumv / count;
        stddeviation = sqrt(variance);

        timeUtilization = (Float.valueOf(sum)/(duration)) * 100 + " %";
    }
}
