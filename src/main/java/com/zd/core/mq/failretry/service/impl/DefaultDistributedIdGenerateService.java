package com.zd.core.mq.failretry.service.impl;

import com.zd.core.mq.failretry.service.IDistributedIdGenerateService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DefaultDistributedIdGenerateService implements IDistributedIdGenerateService {

    /**
     * 生成主键
     *
     * @return
     */
    @Override
    public String generate() {
        //TODO 可以使用雪花算法生成ID
        return Math.abs(new Random().nextInt(100000000)) + "";
    }
}
