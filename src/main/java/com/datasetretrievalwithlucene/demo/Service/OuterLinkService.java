package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.OuterLink;
import com.datasetretrievalwithlucene.demo.Mapper.OuterLinkMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OuterLinkService {
    private final OuterLinkMapper outerLinkMapper;

    public OuterLinkService(OuterLinkMapper outerLinkMapper) {
        this.outerLinkMapper = outerLinkMapper;
    }

    public List<OuterLink> getAll() {
        return outerLinkMapper.getAll();
    }
}
