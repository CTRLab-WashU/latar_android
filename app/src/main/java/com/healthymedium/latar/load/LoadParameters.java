package com.healthymedium.latar.load;

// when period equals zero, the action runs continuously
// when threadCount equals zero, threadCount is set to two for every processor core
//
//
public class LoadParameters {

    public long interval;
    public int threadCount;
    public String workload;

    public LoadParameters() {
        interval = 0;
        workload = Workloads.MATRIX;
        threadCount = Runtime.getRuntime().availableProcessors()*2;
    }

    public LoadParameters(String workload, long interval, int threadCount) {
        this.workload = workload;
        this.interval = interval;
        this.threadCount = threadCount;

        if(threadCount==0){
            this.threadCount = Runtime.getRuntime().availableProcessors()*2;
        }
    }

}
