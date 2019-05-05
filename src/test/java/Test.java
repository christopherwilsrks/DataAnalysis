import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.nlp.util.PdfUtil;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.recommender.User.NUM_USERS;

public class Test {

    @org.junit.Test
    public void testReader() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("../dataset/usermap.txt"));
        String strLine;
        int count = 0;
        while ((strLine = br.readLine()) != null) {

            count += Integer.valueOf(strLine.split("\\|")[2]);

        }

        System.out.println(count / NUM_USERS);
    }

}
