package com.recommender;

import java.io.*;

import static com.recommender.RunTest.NUM_THREADS;

/**
 * @author christ
 * 2019/5/4 16:11
 **/
public class PostHandler {

    private static void post() throws IOException {

        BufferedWriter bw     = new BufferedWriter(new FileWriter("../result/result.txt", true));
        int            userId = -1;
        for (int i = NUM_THREADS; i < NUM_THREADS + 1; i++) {
            BufferedReader br    = new BufferedReader(new FileReader("../result/preTest-" + i + ".txt"));
            String         strLine;
            int            index = 6;
            while ((strLine = br.readLine()) != null) {
                if (index == 6) {
                    userId = Integer.parseInt(strLine.split("\\|")[0]);
                    bw.write(userId + "|6\n");
                    index = 0;
                    continue;
                }
                String[] split   = strLine.split("\\|");
                int      item_id = Integer.parseInt(split[0]);
                double   predict = Double.parseDouble(split[1]);
                int      rating  = (int) (Math.round(predict / 10.0) * 10);
                bw.write(item_id + "  " + rating + "\n");
                if (userId % 50 == 0 && index == 0) {
                    bw.flush();
                }
                index++;
            }
            br.close();
            bw.flush();
        }
        bw.close();
    }

    private static void RMSE() throws IOException {

        double top = .0;
        int C = 0;
        for (int i = 0; i < 1; i++) {
            BufferedReader br     = new BufferedReader(new FileReader("../result/preTrain-" + i + ".txt"));
            String         strLine;
            int count;
            while ((strLine = br.readLine()) != null) {
                String[] split = strLine.split("\\|");
                count = Integer.parseInt(split[1]);
                while (count-- != 0) {
                    strLine = br.readLine();
                    split   = strLine.split("\\|");
//                    int      predict = (int) (Math.round(Double.parseDouble(split[1]) / 100) * 10);
                    double predict = Double.parseDouble(split[1]);
                    int rating  = (int) Double.parseDouble(split[2]);
//                    if (Math.abs(predict - rating) <= 20) {
                        top += Math.pow(rating - predict, 2);
                        C++;
//                    }
                }
            }
            br.close();
        }
        System.out.println("RMSE: " + Math.pow(top / C, .5));
    }

    public static void main(String[] args) throws IOException {
//        post();
        RMSE();
    }
}
