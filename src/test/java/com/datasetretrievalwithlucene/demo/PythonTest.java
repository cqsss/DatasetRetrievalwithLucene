package com.datasetretrievalwithlucene.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootTest
public class PythonTest {
    @Test
    public void testPython(){
        Process proc = null;
        try {
            String a = "111";
            String b = "222";
            String c = "333";
            String d = "444";
            String[] argv = new String[] { "python", "G:\\DatasetRetrievalwithLucene\\src\\main\\resources\\py\\test.py", a, b, c, d };
            proc = Runtime.getRuntime().exec(argv);
            System.err.println("proc:"+proc);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),"GBK"));
            System.err.println("in:"+in);
            String line;
//        System.err.println(in.lines().count());
            while ((line = in.readLine()) != null) {
                System.out.println("line:"+line);
            }
            in.close();
            proc.waitFor();
            System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
