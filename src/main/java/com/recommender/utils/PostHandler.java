package com.recommender.utils;

import java.io.*;

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
        if (files == null) {
            System.out.println("wrong directory");
            System.exit(1);
        }
        for (File file : files) {
            if (!file.getName().startsWith("preTrain")) {
                continue;
            }
            BufferedReader br     = new BufferedReader(new FileReader(file));
            String         strLine;
            int count;
            while ((strLine = br.readLine()) != null) {
                String[] split = strLine.split("\\|");
                count = Integer.parseInt(split[1]);
                while (count-- != 0) {
                    split = br.readLine().split("\\|");
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
        if (args.length != 1) {
            System.out.println("invalid arguments");
            System.exit(1);
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("directory not existed");
            System.exit(1);
        }
        RMSE(args[0]);
    }
}