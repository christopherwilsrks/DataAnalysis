import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static com.recommender.User.NUM_USERS;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ItemAttribute {

    private static final String baseDir = "../";
    private static Double[] userAVG;
    private static Map<Integer, String> itemAttribute;


    @org.junit.Test
    public void testReader() throws IOException {

        int itemId1 = 14;
        int itemId2 = 20488;
//        int itemId2 = 476248;
        LineNumberReader lr = new LineNumberReader(new FileReader(baseDir + File.separator + "dataset/inverse_item.txt"));

        Double sim = cal_sim(getMapUserScore(lr, itemId1), getMapUserScore(lr, itemId2));
        System.out.println(String.format("sim: %3f", sim));

        lr.close();
    }

    @Test
    public void findMatches() throws IOException{

        String end = "97491";

        BufferedReader br = new BufferedReader(new FileReader(baseDir + File.separator + "dataset/itemAttribute.txt"));
        String strLine;

        ArrayList<Integer> items = new ArrayList<>();

        while ((strLine = br.readLine()) != null) {

            if (strLine.endsWith(end)) {
                items.add(Integer.valueOf(strLine.split("\\|")[0]));
            }

        }

        for (Integer item2 : items) {

            Integer item1 = items.get(0);
            if (item1.equals(item2)) {
                continue;
            }

            LineNumberReader lr = new LineNumberReader(new FileReader(baseDir + File.separator + "dataset/inverse_item.txt"));
            Map<Integer, Integer> score = getMapUserScore(lr, item1);
            Double sim = cal_sim(score, getMapUserScore(lr, item2));
            if (sim != -1.) {
                System.out.println(String.format("item1: %d, item2: %d, sim: %3f", item1, item2, sim));
                System.exit(0);
            }

        }

    }

    @Before
    public void preRun() {
        userAVG = new Double[NUM_USERS];
        try {
            BufferedReader br = new BufferedReader(new FileReader(baseDir + File.separator + "dataset/usermap.txt"));
            int i             = 0;
            String strLine;
            while ((strLine = br.readLine()) != null) {
                userAVG[i++] = Double.valueOf(strLine.split("\\|")[3]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        itemAttribute = new TreeMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(baseDir + File.separator + "dataset/itemAttribute.txt"));
            int i             = 0;
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] split = strLine.split("\\|");
                int itemId = Integer.parseInt(strLine.split("\\|")[0]);
                itemAttribute.put(itemId, split[1] + "|" + split[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Double cal_sim(Map<Integer, Integer> item1, Map<Integer, Integer> item2) throws IOException {

        // 求两map的交集
        ArrayList<Integer> retained = new ArrayList<Integer>(item1.keySet());
        retained.retainAll(item2.keySet());
        if (retained.size() == 0) {
            return -1.;
        }
        System.out.println(Arrays.toString(retained.toArray()));

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

    private static Map<Integer, Integer> getMapUserScore(LineNumberReader lr, int itemId) throws IOException{

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

        return mapUserScore;

    }

    private static void readLineNum(LineNumberReader lr, int lineNum) throws IOException {

        while (lr.getLineNumber() != lineNum) {
            lr.readLine();
        }
    }

}
