package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.Annotation;
import com.datasetretrievalwithlucene.demo.Mapper.AnnotationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnotationService {
    private final AnnotationMapper annotationMapper;

    public AnnotationService(AnnotationMapper annotationMapper) {
        this.annotationMapper = annotationMapper;
    }

    public List<Annotation> getAll() {
        return annotationMapper.getAll();
    }

    public Annotation getAnnotation(int user_id, int query_id, int dataset_id) {
        return annotationMapper.getAnnotation(user_id, query_id, dataset_id);
    }

    public boolean searchAnnotation(int user_id, int query_id, int dataset_id) {
        if (getAnnotation(user_id, query_id, dataset_id) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void insertAnnotation(Annotation annotation) {
        annotationMapper.insertAnnotation(annotation);
    }

    public void updateRatingById(int annotation_id, int rating, String annotation_time) {
        annotationMapper.updateRatingById(annotation_id, rating, annotation_time);
    }

    public List<Integer> getRating(int query_id, int dataset_id) {
        return annotationMapper.getRating(query_id, dataset_id);
    }

    public void updateReasonById(int annotation_id, String reason) {
        annotationMapper.updateReasonById(annotation_id, reason);
    }
}
