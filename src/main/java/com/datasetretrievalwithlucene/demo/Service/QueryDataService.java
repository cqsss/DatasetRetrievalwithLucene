package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.QueryData;
import com.datasetretrievalwithlucene.demo.Mapper.QueryDataMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryDataService {
    private final QueryDataMapper queryDataMapper;

    public QueryDataService(QueryDataMapper queryDataMapper) {
        this.queryDataMapper = queryDataMapper;
    }

    public List<QueryData> getAll() {
        return queryDataMapper.getAll();
    }

    public QueryData getQueryDataById(int query_data_id) {
        return queryDataMapper.getQueryDataById(query_data_id);
    }

    public int getMaxId() {
        return queryDataMapper.getMaxId();
    }

    public int getQueryDataIdByQueryIdAndDatasetId(int query_id, int dataset_id) {
        return queryDataMapper.getQueryDataIdByQueryIdAndDatasetId(query_id, dataset_id);
    }
}
