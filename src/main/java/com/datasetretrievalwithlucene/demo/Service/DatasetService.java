package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.Dataset;
import com.datasetretrievalwithlucene.demo.Mapper.DatasetMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetService {
    private final DatasetMapper datasetMapper;

    public DatasetService(DatasetMapper datasetMapper) {
        this.datasetMapper = datasetMapper;
    }

    public List<Dataset> getAll() {return datasetMapper.getAll();}
    public Dataset getByDatasetId(int dataset_id) {
        return datasetMapper.getByDatasetId(dataset_id);
    }
}
