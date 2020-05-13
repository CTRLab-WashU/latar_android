package com.healthymedium.latar.load;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SyntheticLoadTest {

    @Test
    public void test() {

        LoadParameters parameters = new LoadParameters(
                Workloads.MATRIX,
                5,
                10);
        SyntheticLoad load = new SyntheticLoad();

        load.start(parameters);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LoadResult result = load.stop();

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        System.out.println(gson.toJson(result));

    }

}