package com.rpc.client.balance;

import com.rpc.annotation.LoadBalance;
import com.rpc.common.constants.RpcConstant;
import com.rpc.common.model.Service;

import java.util.List;
import java.util.Random;

/**
 * 随机算法
 */
@LoadBalance(RpcConstant.BALANCE_RANDOM)
public class RandomBalance implements com.rpc.client.balance.LoadBalance<Service> {

    private static Random random = new Random();

    @Override
    public Service chooseOne(List<Service> services) {
        return services.get(random.nextInt(services.size()));
    }
}
