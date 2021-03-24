package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DPRTest {
    @Test
    public void testDPR() {
        RelevanceRanking.DPR("Video surveillance market size worldwide 2016-2025");
    }
    @Test
    public void testDPRRankingList() {
        System.out.println(RelevanceRanking.DPRRankingList("Video surveillance market size worldwide 2016-2025"));
    }
}
