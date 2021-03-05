package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.Annotation;
import com.datasetretrievalwithlucene.demo.Bean.Dataset;
import com.datasetretrievalwithlucene.demo.Mapper.AnnotationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnotationService {
    private final AnnotationMapper annotationMapper;

    public AnnotationService(AnnotationMapper annotationMapper) {
        this.annotationMapper = annotationMapper;
    }

    public List<Annotation> getAll() {return annotationMapper.getAll();}
}
