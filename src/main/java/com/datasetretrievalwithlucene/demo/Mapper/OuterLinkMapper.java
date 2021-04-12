package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.OuterLink;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OuterLinkMapper {
    @Select("select * from outerlink3")
    List<OuterLink> getAll();
}
