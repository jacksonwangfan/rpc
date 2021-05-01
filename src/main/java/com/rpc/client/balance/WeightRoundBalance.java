package com.rpc.client.balance;

import com.rpc.annotation.LoadBalance;
import com.rpc.common.constants.RpcConstant;
import com.rpc.common.model.Service;

import java.util.List;

/**
 * 加权轮询
 */
@LoadBalance(RpcConstant.BALANCE_WEIGHT_ROUND)
public class WeightRoundBalance implements com.rpc.client.balance.LoadBalance {

    private volatile static int index;

    @Override
    public synchronized Service chooseOne(List<Service> services) {
        //获取服务的总权重
        int allWeight = services.stream().mapToInt(Service::getWeight).sum();
        int number = (index++) % allWeight;
        for(Service service : services){
            if (service.getWeight() > number){
                return service;
            }
            number -= service.getWeight();
        }
        return null;
    }
}
