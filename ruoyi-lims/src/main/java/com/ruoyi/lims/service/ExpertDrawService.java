package com.ruoyi.lims.service;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 抽取核心逻辑。
 * 只负责「从一堆候选 ID 里随机挑 N 个」，不查库也不写库，方便单独写单元测试。
 */
@Service
public class ExpertDrawService {

    private Random random = new Random();

    /**
     * 从候选里随机挑 count 个。
     * 用的是 Fisher-Yates 洗牌，但只洗前 count 个位置，
     * 挑 5 个人就只交换 5 次，不用把整个列表洗完。
     */
    public List<Long> randomPick(List<Long> candidateIds, int count) {
        if (count <= 0 || CollUtil.isEmpty(candidateIds)) {
            return new ArrayList<>();
        }
        List<Long> copy = new ArrayList<>(candidateIds);
        if (count >= copy.size()) {
            Collections.shuffle(copy, random);
            return copy;
        }
        int size = copy.size();
        for (int i = 0; i < count; i++) {
            int j = i + random.nextInt(size - i);
            Collections.swap(copy, i, j);
        }
        return new ArrayList<>(copy.subList(0, count));
    }

    /**
     * 仅供单元测试注入固定随机源，让抽取结果可以复现
     */
    public void setRandom(Random random) {
        this.random = random;
    }
}
