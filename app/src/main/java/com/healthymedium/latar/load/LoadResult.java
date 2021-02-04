package com.healthymedium.latar.load;

import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class LoadResult {

    public List<WorkerResult> threads;

    public Long min;
    public Long max;
    public Long sum;
    public Long count;
    public Long threadCount;

    public Double avg;
    public Double variance;
    public Double stddeviation;

    public LoadResult(List<WorkerResult> results) {
        threads = results;
        threadCount = Long.valueOf(results.size());

        count = Long.valueOf(0);
        for(WorkerResult val : results){
            count += val.count;
        }

        if(count==0){
            return;
        }

        sum = Long.valueOf(0);
        max = results.get(0).max;
        min = results.get(0).max;

        for(WorkerResult result : results){
            if(result.max>max){
                max = result.max;
            }
            if(result.min<min){
                min = result.min;
            }
            for(Long val : result.deltas) {
                sum += val;
            }
        }

        avg = Double.valueOf((sum)) / count;

        double sumv = 0;
        for(WorkerResult result : results) {
            for(Long val : result.deltas) {
                sumv += pow((val - avg), 2);
            }
        }

        variance = sumv / count;
        stddeviation = sqrt(variance);

    }

}
