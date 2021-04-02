package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.Query;
import com.datasetretrievalwithlucene.demo.Mapper.QueryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService {
    private final QueryMapper queryMapper;

    public QueryService(QueryMapper queryMapper) {
        this.queryMapper = queryMapper;
    }

    public List<Query> getAll() {
        return queryMapper.getAll();
    }

    public Query getQueryById(int query_id) {
        return queryMapper.getQueryById(query_id);
    }

    public int getQueryCount() {
        return queryMapper.getQueryCount();
    }
}
