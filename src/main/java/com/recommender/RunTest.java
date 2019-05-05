package com.recommender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

import static com.recommender.User.NUM_ITEMS;
import static com.recommender.User.NUM_USERS;
import static org.apache.commons.lang3.StringUtils.center;

public class RunTest extends Thread {

    public static int    NUM_THREADS = 10;
    static        String baseDir;

    private final int num;

    RunTest(int num) {
        this.num = num;
    }

    @Override
    public void run() {
        int start_user_id = (NUM_USERS / NUM_THREADS) * num;
        int end_user_id   = num != NUM_USERS - 1 ? start_user_id + NUM_USERS / NUM_THREADS : NUM_USERS;

        try {
            File file = new File(baseDir + File.separator + "result/preTest-" + num + ".txt");
            if (file.exists()) {
                BufferedReader br         = new BufferedReader(new FileReader(baseDir + File.separator + "result/preTest-" + num + ".txt"));
                String         strLine;
                int            line_count = 0;
                while ((strLine = br.readLine()) != null) {
                    line_count++;
                }
                start_user_id += line_count / 7;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int i = start_user_id; i < end_user_id; i++) {
            System.out.println(String.format("Thread [%s] starts user [%s] ...", center(String.valueOf(num), 3), center(String.valueOf(i), 5)));
            long start = System.currentTimeMillis();
            try {
                User user = new User(i);
                user.process(num);
                user = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            DecimalFormat df      = new DecimalFormat("0.00");
            String        end     = df.format((System.currentTimeMillis() - start) / 1000.);
            double        p       = 100. * (i - start_user_id) / (end_user_id - start_user_id);
            String        process = df.format(p) + "%";
            System.out.println(String.format("Thread [%s] finish user [%s] in [%s], now [%s]", center(String.valueOf(num), 3), center(String.valueOf(i), 5), center(end + 's', 7), center(process, 8)));
        }

    }
}

class Main {

    private static void preRun(String baseDir) {
        try {
            // 读取 item 的平均值
            User.itemAVG = new Double[NUM_ITEMS];
            BufferedReader br = new BufferedReader(new FileReader(baseDir + File.separator + "dataset/avg.txt"));
            String         strLine;
            int            i  = 0;
            while ((strLine = br.readLine()) != null) {
                User.itemAVG[i++] = Double.valueOf(strLine);
            }

            // 读取 user 的平均值
            User.userAVG = new Double[NUM_USERS];
            br = new BufferedReader(new FileReader(baseDir + File.separator + "dataset/usermap.txt"));
            i = 0;
            while ((strLine = br.readLine()) != null) {
                User.userAVG[i++] = Double.valueOf(strLine.split("\\|")[3]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        File file = new File(args[0]);
        if (args.length >= 2 && !args[1].equals("")) {
            if (Integer.valueOf(args[1]) > Runtime.getRuntime().availableProcessors()) {
                System.out.println("Threads out numbers!");
                return;
            } else {
                RunTest.NUM_THREADS = Integer.valueOf(args[1]);
            }
        }
        if (!file.exists() || !file.isDirectory()) {
            System.out.println("Not a directory!");
            return;
        } else {
            File resultDir  = new File(args[0] + File.separator + "result/");
            File datasetDir = new File(args[0] + File.separator + "dataset/");
            if (!datasetDir.exists()) {
                System.out.println("missing dataset directory!");
                return;
            }
            if (!resultDir.exists()) {
                resultDir.mkdirs();
            }
        }
        RunTest.baseDir = args[0];
        User.baseDir = args[0];
        preRun(args[0]);
        for (int i = 0; i < RunTest.NUM_THREADS; i++) {
            new Thread(new RunTest(i)).start();
        }
    }
}
