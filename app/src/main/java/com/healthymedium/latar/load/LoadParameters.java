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
        threadCount = 0;
        workload = Workloads.MATRIX;
    }

    public LoadParameters(String workload, long interval, int threadCount) {
        this.workload = workload;
        this.interval = interval;
        this.threadCount = threadCount;
    }

}
