package com.rpc.client.balance;

import com.rpc.annotation.LoadBalance;
import com.rpc.common.constants.RpcConstant;
import com.rpc.common.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 轮询算法
 */
@LoadBalance(RpcConstant.BALANCE_ROUND)
public class FullRoundBalance implements com.rpc.client.balance.LoadBalance {

    private static Logger logger = LoggerFactory.getLogger(FullRoundBalance.class);

    private volatile int index;

    @Override
    public synchronized Service chooseOne(List<Service> services) {
        // 加锁防止多线程情况下，index超出services.size()
        if (index == services.size()) {
            index = 0;
        }
        return services.get(index++);
    }
}
