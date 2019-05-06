package com.recommender;

import java.io.*;

import static com.recommender.RunTest.NUM_THREADS;

/**
 * @author christ
 * 2019/5/4 16:11
 **/
public class PostHandler {

    private static void RMSE(String pathName) throws IOException {

        double top = .0;
        int C = 0;
        File   resultDir = new File(pathName);
        File[] files = resultDir.listFiles();
        for (File file : files) {
            if (!file.getName().startsWith("preTrain-0")) {
                continue;
            }
//            if (!file.getName().startsWith("result-train-0")) {
//                continue;
//            }
            BufferedReader br     = new BufferedReader(new FileReader(file));
            String         strLine;
            int count;
            while ((strLine = br.readLine()) != null) {
                String[] split = strLine.split("\\|");
                count = Integer.parseInt(split[1]);
                while (count-- != 0) {
                    strLine = br.readLine();
                    split   = strLine.split("\\|");
                    double predict = Double.parseDouble(split[1]);
                    int rating  = (int) Double.parseDouble(split[2]);
                    top += Math.pow(rating - predict, 2);
                    C++;
                }
            }
            br.close();
        }
        System.out.println("RMSE: " + Math.pow(top / C, .5));
    }

    public static void main(String[] args) throws IOException {
        RMSE("../result-K50");
    }
}
