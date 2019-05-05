package com.nlp.util;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.nlp.util.PdfUtil.extractTXT;
import static org.apache.commons.lang3.StringUtils.center;

public class PdfUtil {

    /**
     * 提取文本
     **/
    public static void extractTXT(String file) {
        try {
            PdfReader reader  = new PdfReader(file);
            int       pageNum = reader.getNumberOfPages();//获得页数
            for (int i = 1; i <= pageNum; i++) {// 只能从第1页开始读
                System.out.println(PdfTextExtractor.getTextFromPage(reader, i));
            }
        } catch (IOException ex) {
            Logger.getLogger(PdfUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 提取部分页面文本
     *
     * @param file      pdf文件路径
     * @param startPage 起始页数
     * @param endPage   结束页数
     */
    public static void extractTXT(String file, int startPage, int endPage) {
        try {
            PdfReader reader = new PdfReader(file);
            for (int i = startPage; i <= endPage; i++) {
                System.out.println(PdfTextExtractor.getTextFromPage(reader, i));
            }
        } catch (IOException ex) {
            Logger.getLogger(PdfUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 保存文本内容
     *
     * @param file     pdf文件路径
     * @param savePath 文本保存路径
     */
    public static void extractTXT(String file, String savePath) {
        try {
            PdfReader reader  = new PdfReader(file);
            int       pageNum = reader.getNumberOfPages();//获得页数
            //创建一个输出流
            Writer writer = new OutputStreamWriter(new FileOutputStream(savePath));
            for (int i = 1; i <= pageNum; i++) {// 只能从第1页开始读
                String text = PdfTextExtractor.getTextFromPage(reader, i).replaceAll("\n \n", "").replaceAll("(?<=\\S)\n(?=\\S)", "").trim();
                writer.write(text);
            }
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(PdfUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 保存部分页面文本
     *
     * @param file      pdf文件路径
     * @param startPage 起始页数
     * @param endPage   结束页数
     * @param savePath  文本保存路径
     */
    public static void extractTXT(String file, int startPage,
                                  int endPage, String savePath) {
        try {
            PdfReader reader = new PdfReader(file);
            //创建一个输出流
            Writer writer = new OutputStreamWriter(new FileOutputStream(savePath));
            for (int i = startPage; i <= endPage; i++) {
                System.out.println(PdfTextExtractor.getTextFromPage(reader, i));
            }
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(PdfUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) {
        String file      = "F:\\pdf\\2013\\000608_阳光股份_2013年年度报告(更新后)_1.pdf";
        String savePath  = "E:\\result2.txt";
        long   startTime = System.currentTimeMillis();
        extractTXT(file, savePath);
        long endTime = System.currentTimeMillis();
        System.out.println("读写所用时间为：" + (endTime - startTime) + "ms");
    }
}

class PdfMain {
    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        String savePath = "C:\\Users\\christ\\Documents\\Homework\\nlp\\CCKS评测任务5数据及说明\\人事变动训练数据\\人事变动_txt_训练\\";

        List<File> fileList = new FileUtil(".pdf").getFileList("C:\\Users\\christ\\Documents\\Homework\\nlp\\CCKS评测任务5数据及说明\\人事变动训练数据\\人事变动_pdf_训练");

        for (int i = 0; i < fileList.size(); i++) {

            File file = fileList.get(i);
            String filename = file.getName().replace(".pdf", ".txt");

            extractTXT(file.getAbsolutePath(), savePath + filename);
            DecimalFormat df      = new DecimalFormat("0.00");
            double        p       =  100. * i / fileList.size();
            String        process = df.format(p) + "%";
            System.out.println(String.format("now finish [%d], process [%s]", i, center(process, 8)));

        }

        System.out.println((System.currentTimeMillis() - start) / 1000. + "s");

    }
}