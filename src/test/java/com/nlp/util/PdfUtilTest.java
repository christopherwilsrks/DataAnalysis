package com.nlp.util;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class PdfUtilTest {

    @Test
    public void extractTXT() {

        try {
            PdfReader reader  = new PdfReader("C:\\Users\\christ\\Documents\\Homework\\nlp\\CCKS评测任务5数据及说明\\人事变动训练数据\\人事变动_pdf_训练\\000002-万科A-关于监事辞职的公告.pdf");
            int       pageNum = reader.getNumberOfPages();//获得页数
            for (int i = 1; i <= pageNum; i++) {// 只能从第1页开始读
                String text = PdfTextExtractor.getTextFromPage(reader, i).replaceAll("\n \n", "").replaceAll("(?<=\\S)\n(?=\\S)", "").trim();
                System.out.println(text);
            }
        } catch (IOException ex) {
            Logger.getLogger(PdfUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testRex() {

        String a = "abc\nd";
        String s = a.replaceAll("(?<=\\S)\n(?=\\S)", "");
        System.out.println(s);


    }

}