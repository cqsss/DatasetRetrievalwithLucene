package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.Score;
import com.datasetretrievalwithlucene.demo.Mapper.ScoreMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lenovo
 * @date 2021/5/17
 */
@Service
public class ScoreService {
    private final ScoreMapper scoreMapper;

    public ScoreService(ScoreMapper scoreMapper) {
        this.scoreMapper = scoreMapper;
    }

    public List<Score> getAll() {
        return scoreMapper.getAll();
    }

    public Score getScore(int user_id, int dataset_id) {
        return scoreMapper.getScore(user_id, dataset_id);
    }

    public void insertScore(Score score) {
        scoreMapper.insertScore(score);
    }

    public void updateScoreById(int score_id, int score_num) {
        scoreMapper.updateScoreById(score_id, score_num);
    }

    public List<Integer> getScoreListByDatasetId(int dataset_id) {
        return scoreMapper.getScoreListByDatasetId(dataset_id);
    }

    public boolean searchScore(int user_id, int dataset_id) {
        if (getScore(user_id, dataset_id) == null) {
            return false;
        } else {
            return true;
        }
    }
}
