package com.recommender;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.center;

public class User {

    private final static int K          = 50;
    private final static int TEST_COUNT = 6;
    public final static int NUM_USERS  = 19835;
    public final static int NUM_ITEMS  = 624961;

    public static String baseDir;

    private int                                 userId;
    private int                                 count;
    private Map<Integer, Integer>               ratings;
    private Integer[]                           mapItems;
    private Double[][]                          sim_matrix;
    private Map<Integer, Map<Integer, Integer>> mapItemUserScore;

    public static Double[] itemAVG;
    public static Double[] userAVG;

    public User(int userId) throws IOException {
        this.userId = userId;
        this.mapItemUserScore = new HashMap<>();
        this.ratings = new HashMap<>();
        userInit();
    }


    private void userInit() throws IOException {

        // 找到该 user 的所有打分
        LineNumberReader lr = new LineNumberReader(new FileReader(baseDir + File.separator + "dataset/usermap.txt"));
        readLineNum(lr, userId);
        String[] f_line   = lr.readLine().split("\\|");
        int      line_num = Integer.parseInt(f_line[1]);
        count = Integer.parseInt(f_line[2]);
        lr.close();

        lr = new LineNumberReader(new FileReader(baseDir + File.separator + "dataset/train.txt"));
        readLineNum(lr, line_num);

        // 初始化 item 和 index 映射
        mapItems = new Integer[count + TEST_COUNT];
        for (int i = 0; i < count; i++) {
            String[] line    = lr.readLine().split("\\s+");
            int      item_id = Integer.parseInt(line[0]);
            int      score   = Integer.parseInt(line[1]);
            mapItems[i] = item_id;
            ratings.put(item_id, score);
        }
        for (int i = count; i < count + TEST_COUNT; i++) {
            mapItems[i] = Integer.MAX_VALUE;
        }

        Arrays.sort(mapItems);

        lr.close();
        lr = new LineNumberReader(new FileReader(baseDir + File.separator + "dataset/test.txt"));
        readLineNum(lr, userId * 7 + 1);
        for (int i = count; i < count + TEST_COUNT; i++) {
            mapItems[i] = Integer.valueOf(lr.readLine());
        }
        lr.close();

        // 初始化 item 对应
        queryItem();

        // 对于大矩阵，n大于10000，不保存sim_matrix
        if (count < 2000) {
            sim_matrix = new Double[count + TEST_COUNT][count + TEST_COUNT];
            for (int row = 0; row < count + TEST_COUNT; row++) {
                for (int col = row; col < count + TEST_COUNT; col++) {
                    Double val = -1.;
                    if (row != col && (row < count || col < count)) {
                        Map<Integer, Integer> map1 = mapItemUserScore.get(mapItems[row]);
                        Map<Integer, Integer> map2 = mapItemUserScore.get(mapItems[col]);
                        val = cal_sim(map1, map2);
                    }
                    if (row < count) {
                        sim_matrix[row][col] = col < count ? val : -1.;
                        sim_matrix[col][row] = val;
                    } else {
                        sim_matrix[row][col] = sim_matrix[col][row] = -1.;
                    }
                }
            }
        }
    }

    // 查询所有对 item 评分过的 user 和 score
    private void queryItem() throws IOException {

        Integer[] items_clone = mapItems.clone();

        Arrays.sort(items_clone);

        LineNumberReader lr = new LineNumberReader(new FileReader(baseDir + File.separator + "dataset/inverse_item.txt"));

        for (Integer itemId : items_clone) {
            String[] map_line = FileUtils.readLines(new File(baseDir + File.separator + "dataset/itemmap.txt"), UTF_8).get(itemId).split("\\|");
            int      line_num = Integer.parseInt(map_line[1]);
            int      count    = Integer.parseInt(map_line[2]);

            readLineNum(lr, line_num);

            HashMap<Integer, Integer> mapUserScore = new HashMap<>();
            for (int i = 0; i < count; i++) {
                String[] line   = lr.readLine().split("\\|");
                int      userId = Integer.parseInt(line[0]);
                int      score  = Integer.parseInt(line[1]);
                mapUserScore.put(userId, score);
            }
            mapItemUserScore.put(itemId, mapUserScore);
        }
        lr.close();
    }

    private static Double cal_sim(Map<Integer, Integer> item1, Map<Integer, Integer> item2) throws IOException {

        // 求两map的交集
        ArrayList<Integer> retained = new ArrayList<Integer>(item1.keySet());
        retained.retainAll(item2.keySet());

        double top   = .0;
        double down1 = .0;
        double down2 = .0;
        for (Integer userId : retained) {
            double avg = userAVG[userId];

            top += (item1.get(userId) - avg) * (item2.get(userId) - avg);
            down1 += Math.pow(item1.get(userId) - avg, 2);
            down2 += Math.pow(item2.get(userId) - avg, 2);
        }
        double down = Math.pow(down1, .5) * Math.pow(down2, .5);

        return down == 0 ? -1 : top / down;
    }

    private static void readLineNum(LineNumberReader lr, int lineNum) throws IOException {

        while (lr.getLineNumber() != lineNum) {
            lr.readLine();
        }
    }

    public Double[][] top_k(int map_item_id) throws IOException {

        Double[] sim_arr;
        // 对于大矩阵，n大于10000而言，实时计算sim_matrix
        if (count > 2000) {
            sim_arr = new Double[count + TEST_COUNT];
            for (int col = 0; col < count + TEST_COUNT; col++) {
                Double val = -1.;
                if (map_item_id != col && (map_item_id < count || col < count)) {
                    Map<Integer, Integer> map1 = mapItemUserScore.get(mapItems[map_item_id]);
                    Map<Integer, Integer> map2 = mapItemUserScore.get(mapItems[col]);
                    val = cal_sim(map1, map2);
                }
                if (map_item_id < count) {
                    sim_arr[col] = col < count ? val : -1.;
                } else {
                    sim_arr[col] = -1.;
                }
            }
        } else {
            sim_arr = sim_matrix[map_item_id].clone();
        }
        ArrayIndexComparator comparator = new ArrayIndexComparator(sim_arr);
        Integer[]            indexArray = comparator.createIndexArray();
        Arrays.sort(indexArray, comparator);

        // 这里防止 top k 越界
        int k = Math.min(K, count);
        Double[][] top_k = new Double[k][3];
        for (int i = 0; i < k; i++) {
            top_k[i][0] = Double.valueOf(mapItems[indexArray[i]]);
            top_k[i][1] = sim_arr[indexArray[i]];
            top_k[i][2] = Double.valueOf(ratings.get(mapItems[indexArray[i]]));
        }

        return top_k;
    }

    private double predictItem(int map_item_id, Double[][] top_k) {

        Double avg_score = itemAVG[mapItems[map_item_id]];
        double top       = .0;
        double down      = .0;

        for (Double[] k : top_k) {
            int    item_id     = (int) k[0].doubleValue();
            Double avg_k_score = itemAVG[item_id];
            Double sim         = k[1];
            Double rating      = k[2];

            top += (rating - avg_k_score) * sim;
            down += Math.abs(sim);
        }

        return avg_score + top / down;
    }

    public void process(int num) throws IOException {

        BufferedWriter trainW = new BufferedWriter(new FileWriter(baseDir + File.separator + "result/preTrain-" + num + ".txt", true));
        BufferedWriter testW  = new BufferedWriter(new FileWriter(baseDir + File.separator + "result/preTest-" + num + ".txt", true));

        trainW.write(userId + "|" + count + "\n");
        testW.write(userId + "|" + "6\n");

        for (int i = 0; i < count + TEST_COUNT; i++) {
            double p = predictItem(i, top_k(i));
            double r = i < count ? ratings.get(mapItems[i]) : -1;
            if (i < count) {
                trainW.write(mapItems[i] + "|" + p + "|" + r + "\n");
            } else {
                testW.write(mapItems[i] + "|" + p + "\n");
            }
        }
        trainW.flush();
        testW.flush();
        trainW.close();
        testW.close();

    }

    Double getSim(int item1, int item2) {

        System.out.println(mapItems[item1] + " " + mapItems[item2]);
        return sim_matrix[item1][item2];

    }

}

class UserMain {

    public static void main(String[] args) {

        int start_user = Integer.parseInt(args[0]);
        int end_user = Integer.parseInt(args[1]);

        int num = 10;

        long start = System.currentTimeMillis();
        try {
            for (int i = start_user; i < end_user; i++) {
                System.out.println(String.format("Thread [%s] starts user [%s] ...", center(String.valueOf(num), 3), center(String.valueOf(i), 5)));
                User user = new User(i);
                user.process(num);
                DecimalFormat df  = new DecimalFormat("0.00");
                String        end = df.format((System.currentTimeMillis() - start) / 1000.);
                System.out.println(String.format("Thread [%s] finish user [%s] in [%s]", center(String.valueOf(num), 3), center(String.valueOf(i), 5), center(end + 's', 7)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
