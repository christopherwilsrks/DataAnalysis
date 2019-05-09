package com.recommender;

import com.recommender.utils.ArrayIndexComparator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.recommender.RunTest.isPercent;
import static com.recommender.RunTest.resultDirPath;

public class User {

    public               int    K          = 10;
    public static        double PERCENT    = .1;
    private final static int    TEST_COUNT = 6;
    public final static  int    NUM_USERS  = 19835;
    public final static  int    NUM_ITEMS  = 624961;

    public static String baseDir;

    // 当前运行的userId
    private int                                 userId;
    // 该userId的评分总数
    private int                                 count;
    // 该userId的项目评分
    private Map<Integer, Integer>               ratings;
    // 下标 对 itemId 的映射
    private Integer[]                           mapItems;
    // 考虑 itemAttribute 的相似度矩阵
    private Double[][]                          sim_matrix_attribute;
    // 不考虑 itemAttribute 的相似度矩阵
    private Double[][]                          sim_matrix;
    // 每个 item 对应的 用户评分
    private Map<Integer, Map<Integer, Integer>> mapItemUserScore;

    // 全体域 item 平均分
    public static Double[]             itemAVG;
    // 全体域 user 平均分
    public static Double[]             userAVG;
    // itemAttribute
    public static Map<Integer, String> itemAttribute;

    // 表示使用 itemAttribute 的次数
    public static AtomicInteger usage = new AtomicInteger(0);

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
        if (isPercent) {
            K = (int) Math.round(count * PERCENT);
        } else {
            K = RunTest.K;
        }
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
        if (count <= 3000) {
            sim_matrix_attribute = new Double[count + TEST_COUNT][count + TEST_COUNT];
            sim_matrix = new Double[count + TEST_COUNT][count + TEST_COUNT];
            for (int row = 0; row < count + TEST_COUNT; row++) {
                for (int col = row; col < count + TEST_COUNT; col++) {
                    Double val = -1., val_attribute = -1.;
                    if (row != col && (row < count || col < count)) {
                        // 判断这两个items是否能够通过itemAttribute文件求出相似度
                        if (isSame(mapItems[row], mapItems[col])) {
                            usage.getAndAdd(1);
                            val_attribute = 1.;
                        } else {
                            Map<Integer, Integer> map1 = mapItemUserScore.get(mapItems[row]);
                            Map<Integer, Integer> map2 = mapItemUserScore.get(mapItems[col]);
                            val = val_attribute = cal_sim(map1, map2);
                        }
                    }
                    if (row < count) {
                        sim_matrix[row][col] = col < count ? val : -1.;
                        sim_matrix[col][row] = val;
                        sim_matrix_attribute[row][col] = col < count ? val_attribute : -1.;
                        sim_matrix_attribute[col][row] = val_attribute;
                    } else {
                        sim_matrix[row][col] = sim_matrix[col][row] = sim_matrix_attribute[row][col] = sim_matrix_attribute[col][row] = -1.;
                    }
                }
            }
        }
    }

    // 判断两个 item 的 itemAttribute 是否一致
    private boolean isSame(int item1, int item2) {
        String s1 = itemAttribute.get(item1);
        if (s1 == null) {
            return false;
        }
        String s2 = itemAttribute.get(item2);
        if (s2 == null) {
            return false;
        }
        return s1.equals(s2) && !s1.contains("None") && !s2.contains("None");
    }

    // 查询所有对 item 评分过的 user 和 score
    private void queryItem() throws IOException {

        Integer[] items_clone = mapItems.clone();

        Arrays.sort(items_clone);

        LineNumberReader lr  = new LineNumberReader(new FileReader(baseDir + File.separator + "dataset/inverse_item.txt"));
        LineNumberReader lrt = new LineNumberReader(new FileReader(baseDir + File.separator + "dataset/itemmap.txt"));

        for (Integer itemId : items_clone) {
            readLineNum(lrt, itemId);
            String[] map_line = lrt.readLine().split("\\|");
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

        lrt.close();
        lr.close();
    }

    // 计算两个 item 的相似性
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

    // 工具类，将lr指向指定行数
    private static void readLineNum(LineNumberReader lr, int lineNum) throws IOException {

        while (lr.getLineNumber() != lineNum) {
            lr.readLine();
        }
    }

    // 求当前 item 的 top K 近邻
    public Double[][][] top_k(int map_item_id) throws IOException {

        Double[] sim_arr;
        Double[] sim_arr_attribute;
        // 对于大矩阵，n大于10000而言，实时计算sim_matrix
        if (count > 3000) {
            sim_arr = new Double[count + TEST_COUNT];
            sim_arr_attribute = new Double[count + TEST_COUNT];
            for (int col = 0; col < count + TEST_COUNT; col++) {
                Double val           = -1.;
                Double val_attribute = -1.;
                if (map_item_id != col && (map_item_id < count || col < count)) {
                    if (isSame(mapItems[map_item_id], mapItems[col])) {
                        usage.getAndAdd(1);
                        val_attribute = 1.;
                    } else {
                        Map<Integer, Integer> map1 = mapItemUserScore.get(mapItems[map_item_id]);
                        Map<Integer, Integer> map2 = mapItemUserScore.get(mapItems[col]);
                        val_attribute = val = cal_sim(map1, map2);
                    }
                }
                if (map_item_id < count) {
                    sim_arr_attribute[col] = col < count ? val_attribute : -1.;
                    sim_arr[col] = col < count ? val : -1.;
                } else {
                    sim_arr_attribute[col] = sim_arr[col] = -1.;
                }
            }
        } else {
            sim_arr = sim_matrix[map_item_id];
            sim_arr_attribute = sim_matrix_attribute[map_item_id];
        }
        ArrayIndexComparator comparator = new ArrayIndexComparator(sim_arr);
        Integer[]            indexArray = comparator.createIndexArray();
        Arrays.sort(indexArray, comparator);

        ArrayIndexComparator comparator_attribute = new ArrayIndexComparator(sim_arr_attribute);
        Integer[]            indexArray_attribute = comparator_attribute.createIndexArray();
        Arrays.sort(indexArray_attribute, comparator_attribute);

        int k;
        if (isPercent) {
            k = K;
        } else {
            // 这里防止 top k 越界
            k = Math.min(K, count);
        }
        Double[][][] top_k = new Double[2][k][3];
        for (int i = 0; i < k; i++) {
            // 写入 不计算 itemAttribute 的top k
            top_k[0][i][0] = Double.valueOf(mapItems[indexArray[i]]);
            top_k[0][i][1] = sim_arr[indexArray[i]];
            top_k[0][i][2] = Double.valueOf(ratings.get(mapItems[indexArray[i]]));
            // 写入 计算 itemAttribute 的top k
            top_k[1][i][0] = Double.valueOf(mapItems[indexArray_attribute[i]]);
            top_k[1][i][1] = sim_arr_attribute[indexArray_attribute[i]];
            top_k[1][i][2] = Double.valueOf(ratings.get(mapItems[indexArray_attribute[i]]));
        }

        return top_k;
    }

    // 预测评分
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

    // 将结果写入文件
    public void process(int num) throws IOException {

        BufferedWriter trainW           = new BufferedWriter(new FileWriter(resultDirPath + File.separator + "preTrain-" + num + ".txt", true), 10 * 1024 * 1024);
        BufferedWriter trainW_attribute = new BufferedWriter(new FileWriter(resultDirPath + "-itemAttribute/" + File.separator + "preTrain-" + num + ".txt", true), 10 * 1024 * 1024);
        BufferedWriter testW_attribute  = new BufferedWriter(new FileWriter(resultDirPath + "-itemAttribute/" + File.separator + "preTest-" + num + ".txt", true));
        BufferedWriter testW            = new BufferedWriter(new FileWriter(resultDirPath + File.separator + "preTest-" + num + ".txt", true));

        String output = userId + "|" + count + "\n";
        trainW.write(output);
        trainW_attribute.write(output);

        testW.write(userId + "|" + "6\n");

        for (int i = 0; i < count + TEST_COUNT; i++) {
            Double[][][] top_k       = top_k(i);
            double       p           = predictItem(i, top_k[0]);
            double       p_attribute = predictItem(i, top_k[1]);
            double       r           = i < count ? ratings.get(mapItems[i]) : -1;
            if (i < count) {
                trainW_attribute.write(mapItems[i] + "|" + p_attribute + "|" + r + "\n");
                trainW.write("|" + p + "|" + r + "\n");
            } else {
                testW.write(mapItems[i] + "|" + p + "\n");
            }
        }
        testW_attribute.flush();
        trainW_attribute.flush();
        trainW.flush();
        testW.flush();
        trainW.close();
        testW.close();
    }

}
