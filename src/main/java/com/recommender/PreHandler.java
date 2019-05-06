package com.recommender;

import com.recommender.utils.JedisUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.*;

/**
 * @author christ
 * 2019/5/6 21:49
 **/
@Component
public class PreHandler {

    public static String baseDir = "../";

    private static void addItemUserScore() throws IOException {

        Jedis jedis = new Jedis("127.0.0.1", 6379);

        BufferedReader br = new BufferedReader(new FileReader(baseDir + File.separator + "dataset/inverse_item.txt"));
        String strLine;
        int itemId = 0;
        while ((strLine = br.readLine()) != null) {
            if (!strLine.contains("|")) {
                itemId = Integer.parseInt(strLine);
                continue;
            }
            String[] line   = strLine.split("\\|");
            int      userId = Integer.parseInt(line[0]);
            int      score  = Integer.parseInt(line[1]);
            if (!jedis.set(itemId + "_" + userId, String.valueOf(score)).equals("OK")) {
                System.out.println(itemId + "_" + userId);
            }

        }

        jedis.close();
        br.close();

    }

    private static void testGetItemUserScore(int itemId, int userId) {
        Jedis jedis = JedisUtil.getJedis();
        String s    = jedis.get(itemId + "_" + userId);
        System.out.println(s);
        jedis.close();
    }

    public static void main(String[] args) throws IOException {
//        addItemUserScore();
        ApplicationContext actx  = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml");
        long               start = System.currentTimeMillis();
        testGetItemUserScore(9, 68);
        System.out.println("duration: " + (System.currentTimeMillis() - start) / 1000. + "s");
    }

}
