package com.datasetretrievalwithlucene.demo.util;

import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class RelevanceRanking {
    private static Directory directory;
    private static IndexReader indexReader;
    private static IndexSearcher indexSearcher;
    private static Map<String, Double> wT;
    private static Map<String, Double> wO;
    private static Map<String, Double> wU;
    private static Map<Pair<String, String>, Long> fieldTermFreq;
    private static Map<String, List<String>> fieldContent;
    private static Map<String, Long> fieldDocLength;

    private static void init() {
        try {
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getCollectionStatistics(List<String> tokens) {
        try {
            wT = new HashMap<>();
            wO = new HashMap<>();
            wU = new HashMap<>();
            fieldTermFreq = new HashMap<>();
            double base = 0.0;
            for (Map.Entry jsonObject : GlobalVariances.getFSDMBoostWeights().entrySet()) {
                String field = jsonObject.getKey().toString();
                Double w = Double.parseDouble(jsonObject.getValue().toString());
                base += w;
                for (String token : tokens) {
                    fieldTermFreq.put(new Pair<>(field, token), indexReader.totalTermFreq(new Term(field, new BytesRef(token))));
                }
            }
            for (Map.Entry jsonObject : GlobalVariances.getFSDMBoostWeights().entrySet()) {
                String field = jsonObject.getKey().toString();
                Double w = Double.parseDouble(jsonObject.getValue().toString()) / base;
                wT.put(field, w);
                wO.put(field, w);
                wU.put(field, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getDocumentStatistics(Integer doc_id) {
        try {
            fieldContent = new HashMap<>();
            fieldDocLength = new HashMap<>();

            for (Object jsonObject : GlobalVariances.getFSDMBoostWeights().keySet()) {
                String field = jsonObject.toString();
                Document document = indexReader.document(doc_id);
                fieldContent.put(field, Statistics.getTokens(document.get(field)));
                Terms terms = indexReader.getTermVector(doc_id, field);
                if (terms != null) fieldDocLength.put(field, terms.getSumTotalTermFreq());
                else fieldDocLength.put(field, 0L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Double getTF_T(Integer doc_id, String field, String qi) {
        double res = 0.0;
        try {
            Terms terms = indexReader.getTermVector(doc_id, field);
            BytesRef bytesRef = new BytesRef(qi);
            if (terms != null) {
                TermsEnum termsEnum = terms.iterator();
                if (termsEnum.seekExact(bytesRef))
                    res = (double) termsEnum.totalTermFreq();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(qi + " TF_T: " + res);
        return res;
    }

    private static Double getTF_O(String field, String qi1, String qi2) {
        double res = 0.0;
        List<String> content = fieldContent.get(field);
        for (int i = 0; i + 1 < content.size(); i++) {
            if (qi1.equals(content.get(i)) && qi2.equals(content.get(i + 1)))
                res += 1.0;
        }
        //System.out.println(qi1 + " " + qi2 + " TF_O: " + res);
        return res;
    }

    private static Double getTF_U(String field, String qi1, String qi2) {
        double res = 0.0;
        List<String> content = fieldContent.get(field);
        for (Integer i = 0; i + GlobalVariances.FSDMUWindowSize <= content.size(); i++) {
            Set<String> window = new HashSet<>();
            for (Integer j = 0; j < GlobalVariances.FSDMUWindowSize; j++) {
                window.add(content.get(i + j));
            }
            if (window.contains(qi1) && window.contains(qi2))
                res += 1.0;
        }
        //System.out.println(qi1 + " " + qi2 + " TF_U: " + res);
        return res;
    }

    private static Double getFSDM_T(Integer doc_id, List<String> queries) {
        double res = 0.0;
        try {
            for (String qi : queries) {
                double tmp = 0.0;
                for (Object jsonObject : GlobalVariances.getFSDMBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    if (wT.get(field) == 0.0) continue;
                    double miu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi));
                    double Dj = (double) fieldDocLength.get(field);
                    tmp += wT.get(field) * (getTF_T(doc_id, field, qi) + miu * cf / Cj) / (Dj + miu);
                }
                //System.out.println("T: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("FSDM_T: " + res);
        return res;
    }

    private static Double getFSDM_O(List<String> queries) {
        double res = 0.0;
        try {
            for (int i = 0; i + 1 < queries.size(); i++) {
                double tmp = 0.0;
                String qi1 = queries.get(i);
                String qi2 = queries.get(i + 1);
                for (Object jsonObject : GlobalVariances.getFSDMBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    if (wO.get(field) == 0.0) continue;
                    double miu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi1)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi1));
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi2)))
                        cf = Math.min(cf, (double) fieldTermFreq.get(new Pair<>(field, qi2)));
                    double Dj = (double) fieldDocLength.get(field);
                    tmp += wO.get(field) * (getTF_O(field, qi1, qi2) + miu * cf / Cj) / (Dj + miu);
                }
                //System.out.println("O: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("FSDM_O: " + res);
        return res;
    }

    private static Double getFSDM_U(List<String> queries) {
        double res = 0.0;
        try {
            for (int i = 0; i + 1 < queries.size(); i++) {
                double tmp = 0.0;
                String qi1 = queries.get(i);
                String qi2 = queries.get(i + 1);
                for (Object jsonObject : GlobalVariances.getFSDMBoostWeights().keySet()) {
                    String field = jsonObject.toString();
                    if (wU.get(field) == 0.0) continue;
                    double miu = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
                    double Cj = (double) indexReader.getSumTotalTermFreq(field);
                    double cf = 0.0;
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi1)))
                        cf = (double) fieldTermFreq.get(new Pair<>(field, qi1));
                    if (fieldTermFreq.containsKey(new Pair<>(field, qi2)))
                        cf = Math.min(cf, (double) fieldTermFreq.get(new Pair<>(field, qi2)));
                    double Dj = (double) fieldDocLength.get(field);
                    tmp += wU.get(field) * (getTF_U(field, qi1, qi2) + miu * cf / Cj) / (Dj + miu);
                }
                //System.out.println("U: " + tmp);
                res += Math.log(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("FSDM_U: " + res);
        return res;
    }

    /**
     * 计算FSDM分数
     * @param doc_id
     * @param tokens
     * @return
     */
    public static Double FSDM(Integer doc_id, List<String> tokens) {
        Double lambdaT = 0.8;
        Double lambdaO = 0.1;
        Double lambdaU = 0.1;
        getCollectionStatistics(tokens);
        getDocumentStatistics(doc_id);
        return lambdaT * getFSDM_T(doc_id, tokens) +
                lambdaO * getFSDM_O(tokens) +
                lambdaU * getFSDM_U(tokens);
    }

    /**
     * 计算BM25分数
     * @param doc_id
     * @param field
     * @param tokens
     * @return
     */
    public static Double BM25(Integer doc_id, String field, List<String> tokens) {
        double score = 0.0;
        double k1 = 1.2;
        double b = 0.75;
        try {
            double N = (double) indexReader.getDocCount(field);
            Terms terms = indexReader.getTermVector(doc_id, field);
            double D = 0.0;
            if (terms != null)
                D = (double) terms.getSumTotalTermFreq();
            double avgdl = (double) indexReader.getSumTotalTermFreq(field) / (double) indexReader.getDocCount(field);
            //System.out.println("doc_id: " + doc_id);
            for (String token : tokens) {
                BytesRef bytesRef = new BytesRef(token);
                double n = (double) indexReader.docFreq(new Term(field, bytesRef));
                double idf = Math.log((N - n + 0.5) / (n + 0.5) + 1);
                double f = 0.0;
                if (terms != null) {
                    TermsEnum termsIterator = terms.iterator();
                    if (termsIterator.seekExact(bytesRef)) f = (double) termsIterator.totalTermFreq();
                }
                double tmp = idf * (f * (k1 + 1.0) / (f + k1 * (1.0 - b + b * D / avgdl)));
                score += tmp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(score);
        return score;
    }

    /**
     * 计算TFIDF分数
     * @param doc_id
     * @param field
     * @param tokens
     * @return
     */
    public static Double TFIDF(Integer doc_id, String field, List<String> tokens) {
        double score = 0.0;
        try {
            double docCount = (double) indexReader.getDocCount(field);
            Terms terms = indexReader.getTermVector(doc_id, field);
            double D = 0.0;
            if (terms != null)
                D = (double) terms.getSumTotalTermFreq();
            double lengthNorm = 1.0 / (Math.sqrt(D) + 1.0);
            //System.out.println("doc_id: " + doc_id);
            for (String token : tokens) {
                BytesRef bytesRef = new BytesRef(token);
                double docFreq = (double) indexReader.docFreq(new Term(field, bytesRef));
                double idf = Math.log((docCount + 1.0) / (docFreq + 1.0)) + 1.0;
                double tf = 0.0;
                if (terms != null) {
                    TermsEnum termsIterator = terms.iterator();
                    if (termsIterator.seekExact(bytesRef)) tf = Math.sqrt((double) termsIterator.totalTermFreq());
                }
                double tmp = lengthNorm * idf * tf;
                score += tmp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(score);
        return score;
    }

    /**
     * 调用DPR脚本
     * @param query
     */
    public static void DPR(String query) {
        Process proc = null;
        try {
            String[] argv = new String[]{
                    GlobalVariances.python_interpreter,
                    GlobalVariances.dense_retriever,
                    GlobalVariances.model_file,
                    GlobalVariances.qa_dataset,
                    GlobalVariances.ctx_datatsets,
                    GlobalVariances.encoded_ctx_files,
                    GlobalVariances.out_file
            };
            File query_file = new File(GlobalVariances.query_tsv);
            if (!query_file.exists()) {
                query_file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(query_file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(query + "\t" + "['1','1']");
            bufferedWriter.close();
            proc = Runtime.getRuntime().exec(argv);
            //System.err.println("proc:"+proc);
            BufferedReader in = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                //System.out.println(line);
            }
            in.close();
            proc.waitFor();
            proc.destroy();
            //System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到根据BM25分数排序后的列表
     * @param query
     * @return
     */
    public static List<Pair<Integer, Double>> BM25RankingList(String query) {
        init();
        query=query.replaceAll("\\p{P}"," ");
        List<Pair<Integer, Double>> BM25ScoreList = new ArrayList<>();
        try {
            String[] fields = GlobalVariances.queryFields;
            double[] weights = GlobalVariances.BM25BoostWeights;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            Query parsedQuery = queryParser.parse(query);
            TopDocs docsSearch = indexSearcher.search(parsedQuery, GlobalVariances.HitSize);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            List<String> queryTokens = Statistics.getTokens(query);
            for (ScoreDoc si : scoreDocs) {
                Integer docID = si.doc;
                Document document = indexReader.document(docID);
                Integer datasetID = Integer.parseInt(document.get("dataset_id"));
                Double score = 0.0;
//                    System.out.println("dataset_id: " + document.get("dataset_id") + ", score: " + si.score);
//                    Explanation e = indexSearcher.explain(parsedQuery, si.doc);
//                    System.out.println("Explanation： \n" + e);
                for (int i = 0; i < fields.length; i++) {
                    score += BM25(docID, fields[i], queryTokens) * weights[i];
                }
                BM25ScoreList.add(new Pair<>(datasetID, score));
            }
            BM25ScoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BM25ScoreList;
    }

    /**
     * 得到根据TFIDF分数排序后的列表
     * @param query
     * @return
     */
    public static List<Pair<Integer, Double>> TFIDFRankingList(String query) {
        init();
        query=query.replaceAll("\\p{P}"," ");
        List<Pair<Integer, Double>> TFIDFScoreList = new ArrayList<>();
        try {
            String[] fields = GlobalVariances.queryFields;
            double[] weights = GlobalVariances.TFIDFBoostWeights;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            Query parsedQuery = queryParser.parse(query);
            TopDocs docsSearch = indexSearcher.search(parsedQuery, GlobalVariances.HitSize);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            List<String> queryTokens = Statistics.getTokens(query);
            for (ScoreDoc si : scoreDocs) {
                Integer docID = si.doc;
                Document document = indexReader.document(docID);
                Integer datasetID = Integer.parseInt(document.get("dataset_id"));
                Double score = 0.0;
//                    System.out.println("dataset_id: " + document.get("dataset_id") + ", score: " + si.score);
//                    Explanation e = indexSearcher.explain(parsedQuery, si.doc);
//                    System.out.println("Explanation： \n" + e);
                for (int i = 0; i < fields.length; i++) {
                    score += TFIDF(docID, fields[i], queryTokens) * weights[i];
                }
                TFIDFScoreList.add(new Pair<>(datasetID, score));
            }
            TFIDFScoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TFIDFScoreList;
    }

    /**
     * 得到根据FSDM分数排序后的列表
     * @param query
     * @return
     */
    public static List<Pair<Integer, Double>> FSDMRankingList(String query) {
        init();
        query=query.replaceAll("\\p{P}"," ");
        List<Pair<Integer, Double>> FSDMScoreList = new ArrayList<>();
        try {
            String[] fields = GlobalVariances.queryFields;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            Query parsedQuery = queryParser.parse(query);
            TopDocs docsSearch = indexSearcher.search(parsedQuery, GlobalVariances.HitSize);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            List<String> queryTokens = Statistics.getTokens(query);
            for (ScoreDoc si : scoreDocs) {
                int docID = si.doc;
                Document document = indexReader.document(docID);
                Integer datasetID = Integer.parseInt(document.get("dataset_id"));
                Double score = 0.0;
//                    System.out.println("dataset_id: " + document.get("dataset_id") + ", score: " + si.score);
//                    Explanation e = indexSearcher.explain(parsedQuery, si.doc);
//                    System.out.println("Explanation： \n" + e);
                score = RelevanceRanking.FSDM(docID, queryTokens);
                FSDMScoreList.add(new Pair<>(datasetID, score));
            }
            FSDMScoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FSDMScoreList;
    }

    /**
     * 得到根据DPR分数排序后的列表
     * @param query
     * @return
     */
    public static List<Pair<Integer, Double>> DPRRankingList(String query) {
        query=query.replaceAll("\\p{P}"," ");
        DPR(query);
        List<Pair<Integer, Double>> DPRScoreList = new ArrayList<>();
        try {
            File result_file = new File(GlobalVariances.out_file_path);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(result_file));
            String line;
            int datasetID;
            double score;
            while ((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                String[] split_line = line.split("\t");
                datasetID = Integer.parseInt(split_line[0]);
                score = Double.parseDouble(split_line[1]);
                DPRScoreList.add(new Pair<>(datasetID, score));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DPRScoreList;
    }

    public static List<Pair<Integer, Double>> RankingList(String query, Integer algorithm_sel) {
        init();
        List<Pair<Integer, Double>> BM25ScoreList = new ArrayList<>();
        List<Pair<Integer, Double>> TFIDFScoreList = new ArrayList<>();
        List<Pair<Integer, Double>> FSDMScoreList = new ArrayList<>();
        try {
            String[] fields = GlobalVariances.queryFields;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            Query parsedQuery = queryParser.parse(query);
            TopDocs docsSearch = indexSearcher.search(parsedQuery, 500);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            for (ScoreDoc si : scoreDocs) {
                Integer docID = si.doc;
                Document document = indexReader.document(docID);
                Integer datasetID = Integer.parseInt(document.get("dataset_id"));
                if (datasetID > 311)
                    datasetID -= 221261;
                Double score = 0.0;
//                    System.out.println("dataset_id: " + document.get("dataset_id") + ", score: " + si.score);
//                    Explanation e = indexSearcher.explain(parsedQuery, si.doc);
//                    System.out.println("Explanation： \n" + e);
                for (String field : fields) {
                    score += BM25(docID, field, Statistics.getTokens(query));
                }
                BM25ScoreList.add(new Pair<>(datasetID, score));
                score = 0.0;
                for (String field : fields) {
                    score += RelevanceRanking.TFIDF(docID, field, Statistics.getTokens(query));
                }
                TFIDFScoreList.add(new Pair<>(datasetID, score));
                score = RelevanceRanking.FSDM(docID, Statistics.getTokens(query));
                FSDMScoreList.add(new Pair<>(datasetID, score));
            }
            BM25ScoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            TFIDFScoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            FSDMScoreList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (algorithm_sel) {
            case 0:
                return BM25ScoreList;
            case 1:
                return TFIDFScoreList;
            case 2:
                return FSDMScoreList;
            default:
                throw new IllegalStateException("Unexpected value: " + algorithm_sel);
        }
    }
}
