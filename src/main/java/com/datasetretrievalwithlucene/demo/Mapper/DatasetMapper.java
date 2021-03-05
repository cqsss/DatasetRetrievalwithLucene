package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.Dataset;
import com.datasetretrievalwithlucene.demo.Bean.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetMapper {

    @Select("select * from metadata")
    List<Dataset> getAll();
    @Select("select * from metadata where dataset_id=#{dataset_id}")
    Dataset getByDatasetId(@Param("dataset_id") int dataset_id);
}
