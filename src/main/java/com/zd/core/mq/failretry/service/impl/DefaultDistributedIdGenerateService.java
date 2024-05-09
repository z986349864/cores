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
     * @author 陈宇霖
     * @date 2019年01月02日20:43:24
     */
    @Override
    public String generate() {
        //TODO 可以使用雪花算法生成ID
        return Math.abs(new Random().nextInt(100000000)) + "";
    }
}
