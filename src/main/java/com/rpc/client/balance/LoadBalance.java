package com.rpc.client.balance;

import com.rpc.common.model.Service;

import java.util.List;

/**
 * 负载均衡算法接口
 */
public interface LoadBalance<T> {
    /**
     *
     * @param services
     * @return
     */
    Service chooseOne(List<T> services);
}
