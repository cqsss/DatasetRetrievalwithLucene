package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.Comment;
import com.datasetretrievalwithlucene.demo.Mapper.CommentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentMapper commentMapper;

    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public List<Comment> getAll() {
        return commentMapper.getAll();
    }

    public void insertComment(Comment comment) {
        commentMapper.insertComment(comment);
    }

    public List<Comment> getCommentsByDatasetId(int dataset_id) {
        return commentMapper.getCommentsByDatasetId(dataset_id);
    }
}
