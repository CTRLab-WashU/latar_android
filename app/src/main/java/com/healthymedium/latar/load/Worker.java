package com.healthymedium.latar.load;

import org.joda.time.DateTime;

import java.util.ArrayList;

class Worker extends Thread {

    private ArrayList<Long> deltas = new ArrayList();
    public DateTime start;
    public DateTime stop;
    private boolean running = true;

    private Workload workload;
    private long interval;

    Worker(String name, Workload workload, long interval) {
        super(name);
        this.workload = workload;
        this.interval = interval;
        System.out.println("creating " + getName());
    }

    public void start () {
        System.out.println("starting " + getName());
        super.start();
    }

    public void run() {
        System.out.println("running " + getName() );

        long before;
        long after;
        long next;

        long delta;

        if(workload!=null){
            workload.setup();
        } else {
            running = false;
        }

        start = DateTime.now();

        while(running) {

            before = System.currentTimeMillis();
            workload.run();
            after = System.currentTimeMillis();

            // add delta to stack
            delta = (after - before);
            deltas.add(delta);

            next = (before + interval);
            after =  System.currentTimeMillis();  // update 'after' to account for insertion time in sleep calc

            if(after<next) {
                // sleep until start of next interval
                try {
                    sleep(next-after);
                } catch (InterruptedException e) {
                    System.out.println(getName() + " interrupted");
                    e.printStackTrace();
                }
            }
        }

        stop = DateTime.now();
        System.out.println(getName()  + " stopped");
    }

    public void shutdown () {
        running = false;
        System.out.println("stopping " + getName());
    }

    public WorkerResult collect () {
        return new WorkerResult(start, stop, deltas);
    }

}
