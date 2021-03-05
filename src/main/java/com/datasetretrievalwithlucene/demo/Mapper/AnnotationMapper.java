package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.Annotation;
import com.datasetretrievalwithlucene.demo.Bean.Dataset;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationMapper {

    @Select("select * from annotation")
    List<Annotation> getAll();

}
