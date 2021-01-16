package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.IndexFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class IndexWriterTest {
    @Test
    public void testIndexWriter(){
        IndexFactory indexFactory = new IndexFactory();
        indexFactory.init(GlobalVariances.store_Dir, GlobalVariances.commit_limit, GlobalVariances.globeAnalyzer);
        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setTokenized(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStoreTermVectorOffsets(true);
        fieldType.setStoreTermVectorPayloads(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

        Document document = new Document();
        document.add(new Field("test", "aaa", fieldType));
        indexFactory.commitDocument(document);
        indexFactory.closeIndexWriter();

    }
}
