package com.healthymedium.latar.load;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.regex.Pattern;

import Jama.Matrix;

public class Workloads {

    public final static String REGEX = "regex";
    public final static String MATRIX = "matrix";
    public final static String SERIALIZATION = "serialization";

    public static Workload get(String workload) {
        if(workload=="regex") {
            return regex();
        }
        if(workload=="matrix") {
            return matrix();
        }
        if(workload=="serialization") {
            return serialization();
        }
        return null;
    }

    public static Workload regex() {

        return new Workload() {

            Pattern pattern;
            String string;

            @Override
            public void setup() {
                pattern = Pattern.compile("");
                string = "";
            }

            @Override
            public void run() {
                pattern.split(string);
            }
        };
    }

    // 1 - Add a constant value to all elements of a matrix.
	// 2 - Multiply a matrix by a constant.
	// 3 - Multiply a matrix by a vector.
	// 4 - Multiply a matrix by a matrix.
	// 5 - Add a constant value to all elements of a matrix.
    public static Workload matrix() {

        final int size = 200;

        return new Workload() {

            Matrix matrix1;
            Matrix matrix2;
            Matrix matrix3;

            Matrix vector;

            void resetMatrix(Matrix matrix) {
                matrix.timesEquals(0);
            }

            @Override
            public void setup() {
                matrix1 = new Matrix(size,size,0);

                matrix2 = new Matrix(size,size,0);
                for(int i=0; i<size; i++){
                    for(int j=0; j<size; j++) {
                        matrix2.set(i,j,i*j);
                    }
                }

                matrix3 = new Matrix(size,size,3.14159);

                vector = new Matrix(size,1,0);
                for(int i=0; i<size; i++){
                    vector.set(i,0,size-i);
                }

            }

            @Override
            public void run() {
                resetMatrix(matrix1);
                matrix1.plusEquals(matrix3);
                matrix1.timesEquals(13.2);

                matrix1.times(vector);
                matrix1.times(matrix2);

                matrix1 = matrix1.plus(matrix3);
            }
        };
    }

    public static Workload serialization(){

        return new Workload() {

            Gson gson;
            Matrix matrix = new Matrix(2000,2000,3.14159);

            @Override
            public void setup() {
                gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
            }

            @Override
            public void run() {
                gson.toJson(matrix);
            }
        };
    }

}
