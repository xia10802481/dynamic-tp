package com.dtp.adapter.hystrix.handler;

import cn.hutool.core.collection.CollUtil;
import com.dtp.adapter.common.AbstractDtpHandler;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.SimpleTpProperties;
import com.dtp.common.util.StreamUtil;
import com.google.common.collect.Maps;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * HystrixDtpHandler related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public class HystrixDtpHandler extends AbstractDtpHandler {

    private static final String NAME = "hystrixTp";

    private static final Map<String, ThreadPoolExecutor> HYSTRIX_EXECUTORS = Maps.newHashMap();

    @Override
    public void updateTp(DtpProperties dtpProperties) {
        val hystrixTpList = dtpProperties.getHystrixTp();
        val executors = getExecutors();
        if (CollUtil.isEmpty(hystrixTpList) || CollUtil.isEmpty(executors)) {
            return;
        }

        val tmpMap = StreamUtil.toMap(hystrixTpList, SimpleTpProperties::getThreadPoolName);
        executors.forEach((k ,v) -> updateBase(NAME, tmpMap.get(k), (ThreadPoolExecutor) v));
    }

    @Override
    public Map<String, ? extends Executor> getExecutors() {
        if (CollUtil.isNotEmpty(HYSTRIX_EXECUTORS)) {
            return HYSTRIX_EXECUTORS;
        }

        Collection<HystrixThreadPoolMetrics> threadPoolMetrics = HystrixThreadPoolMetrics.getInstances();
        if (CollUtil.isEmpty(threadPoolMetrics)) {
            return HYSTRIX_EXECUTORS;
        }
        threadPoolMetrics.forEach(x -> {
            val threadPoolKey = x.getThreadPoolKey();
            HYSTRIX_EXECUTORS.put(threadPoolKey.name(), x.getThreadPool());
        });

        log.info("DynamicTp adapter, hystrix executors init end, executors: {}", HYSTRIX_EXECUTORS);
        return HYSTRIX_EXECUTORS;
    }
}
