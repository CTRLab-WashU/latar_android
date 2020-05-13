package com.healthymedium.latar.load;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SyntheticLoad {

    List<Worker> threads = new ArrayList<>();
    LoadParameters params;

    public void start(@NonNull LoadParameters params) {
        this.params = params;

        for(int i=0;i<params.threadCount;i++){
            Worker thread = new Worker(
                    "workload-"+i,
                    Workloads.get(params.workload),
                    params.interval);
            threads.add(thread);
        }

        for(Worker thread : threads){
            thread.start();
        }

    }

    public LoadResult stop() {

        List<WorkerResult> results = new ArrayList<>();

        for(Worker thread : threads){
            thread.shutdown();
        }

        for(Worker thread : threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(Worker thread : threads){
            results.add(thread.collect());
        }

        threads.clear();
        return new LoadResult(results);
    }





}
