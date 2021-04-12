package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class regexTest {
    @Test
    public void testRegEx(){
        try {
            String str = "Supporting materials \"STATISTICAL MECHANICS FOR METABOLIC NETWORKS IN STEADY-STATE GROWTH\"";
            System.out.println(str.replaceAll("\\p{P}", " "));
            System.out.println(Statistics.getTokens(str.replaceAll("\\p{P}", " ")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testReplaceHTMLTag() {
        String content = "<p>NNDSS - Table IV. Tuberculosis - 2016.This Table includes total number of cases reported in the United States, by region and by states, in accordance with the current method of displaying MMWR data. Data on United States will exclude counts from US territories. Footnote: C.N.M.I.: Commonwealth of Northern Mariana Islands. U: Unavailable. -: No reported cases. N: Not reportable. NN: Not Nationally Notifiable Cum: Cumulative year-to-date counts. Min: Minimum. Max: Maximum. * Case counts for reporting year 2015 and 2016 are provisional and subject to change. For further information on interpretation of these data, see <a href=\"http://wwwn.cdc.gov/nndss/document/ProvisionalNationaNotifiableDiseasesSurveillanceData20100927.pdf\">http://wwwn.cdc.gov/nndss/document/ProvisionalNationaNotifiableDiseasesS...</a> Data for TB are displayed quarterly.</p>";
        content = content.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", "");
        System.out.println(content);
    }
}