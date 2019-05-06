package com.recommender;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jws.soap.SOAPBinding;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.TreeMap;

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
            File file = new File(baseDir + File.separator + "result-K" + User.K + "/preTest-" + num + ".txt");
            if (file.exists()) {
                BufferedReader br         = new BufferedReader(new FileReader(baseDir + File.separator + "result-K" + User.K + "/preTest-" + num + ".txt"));
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
            int usage = User.usage.get();
            if (usage % 1000 == 0) {
                System.out.println(String.format("itemAttribute usage: %d", usage));
            }
        }

    }
}

class Main {

    // args keys
    private static final String KEY_DIR = "--dir";
    private static final String KEY_DIR_ALIAS = "-d";

    private static final String KEY_THREADS = "--threads";
    private static final String KEY_THREADS_ALIAS = "-t";

    private static final String KEY_K = "--k";
    private static final String KEY_k_ALIAS = "-k";

    private static final String KEY_HELP = "--help";
    private static final String KEY_HELP_ALIAS = "-h";

    // configuration values
    private static String baseDir = "";
    private static int threads = 5;
    private static int K = 50;

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

            User.itemAttribute = new TreeMap<>();
            br = new BufferedReader(new FileReader(baseDir + File.separator + "dataset/itemAttribute.txt"));
            while ((strLine = br.readLine()) != null) {
                String[] split = strLine.split("\\|");
                int itemId = Integer.parseInt(strLine.split("\\|")[0]);
                User.itemAttribute.put(itemId, split[1] + "|" + split[2]);
            }
        } catch (IOException e) {
            printUsageText("pre run error, check if dataset/avg.txt and dataset/usermap.txt correctly set");
            System.exit(0);
        }
    }

    public static void main(String[] args) {

        try {
            // parse input parameters
            for (int i = 0; i < args.length; i += 2) {

                String key   = args[i];
                if (key.equals(KEY_HELP) || key.equals(KEY_HELP_ALIAS)) {
                    printUsageText(null);
                    System.exit(0);
                }
                String value = args[i + 1];

                if (key.equals(KEY_DIR) || key.equals(KEY_DIR_ALIAS)) {
                    baseDir = value;
                } else if (key.equals(KEY_K) || key.equals(KEY_k_ALIAS)) {
                    K = Integer.parseInt(value);
                } else if (key.equals(KEY_THREADS) || key.equals(KEY_THREADS_ALIAS)) {
                    threads = Integer.parseInt(value);
                }
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            printUsageText(e.getMessage());
            System.exit(1);
        }

        // 不允许目录不存在
        if (baseDir.isEmpty()) {
            printUsageText("missing required parameters: -d");
            System.exit(1);
        }

        // 检查目标目录
        File file = new File(baseDir);
        if (!file.exists() || !file.isDirectory()) {
            System.out.println("Not a directory!");
            return;
        } else {
            File resultDir  = new File(baseDir + File.separator + "result-K" + K + "/");
            File datasetDir = new File(baseDir + File.separator + "dataset/");
            if (!datasetDir.exists()) {
                System.out.println("missing dataset directory!");
                return;
            }
            if (!resultDir.exists()) {
                resultDir.mkdirs();
            }
        }

        // 设置目录
        RunTest.baseDir = baseDir;
        User.baseDir = baseDir;

        // 设置线程数
        if (threads > Runtime.getRuntime().availableProcessors()) {
            System.out.println("Threads out numbers!");
            System.exit(1);
        } else {
            RunTest.NUM_THREADS = threads;
        }

        // 设置K值
        User.K = K;

        // 预读取数据
        preRun(baseDir);

        System.out.println(String.format("\n\nRunning recommender.jar with [%d] threads and matching for top [%d] most similar items...\n\n-------------------------------------------------------------\n", threads, K));

        for (int i = 0; i < RunTest.NUM_THREADS; i++) {
            new Thread(new RunTest(i)).start();
        }
    }

    private static void printUsageText(String err) {

        if (err != null) {
            // if error has been given, print it
            System.err.println("ERROR: " + err + ".\n");
        }

        System.out.println("Usage: recommender.jar " + KEY_DIR + " <directory>\n");
        System.out.println("Options:\n");
        System.out.println("    " + KEY_DIR + "\t(" + KEY_DIR_ALIAS + ")\t<directory>\t\tThe base directory where dataset existed [REQUIRED]");
        System.out.println("    " + KEY_THREADS + "\t(" + KEY_THREADS_ALIAS + ")\t<threads>\t\tThe number of threads used for this program [OPTIONAL]");
        System.out.println("    " + KEY_K+ "\t\t(" + KEY_k_ALIAS + ")\t<k>\t\t\tThe value of top K [OPTIONAL]");
        System.out.println("    " + KEY_HELP + "\t(" + KEY_HELP_ALIAS + ")\tDisplay the help text\n");
    }

}
