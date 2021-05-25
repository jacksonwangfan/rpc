package com.rpc.client.balance;

import com.rpc.annotation.LoadBalance;
import com.rpc.common.constants.RpcConstant;
import com.rpc.common.model.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平滑加权轮询
 */
@LoadBalance(RpcConstant.BALANCE_SMOOTH_WEIGHT_ROUND)
public class SmoothWeightRoundBalance implements com.rpc.client.balance.LoadBalance<Service> {
    /**
     * key:服务value:当前权重
     */
    private static final Map<String, Integer> map = new HashMap<>();

    @Override
    public synchronized Service chooseOne(List<Service> services) {
        //将服务以及权重放入map
        services.forEach(service ->
                map.computeIfAbsent(service.toString(), key -> service.getWeight())
        );
        //加权轮训得到的结果
        Service maxWeightServer = null;
        //同一个服务所有实例权重的和
        int allWeight = services.stream().mapToInt(Service::getWeight).sum();
        //遍历所有服务实例
        for (Service service : services) {
            //拿到的服务实例的权重
            Integer currentWeight = map.get(service.toString());
            //如果当前权重比最大权重要大，返回service
            if (maxWeightServer == null || currentWeight > map.get(maxWeightServer.toString())) {
                maxWeightServer = service;
            }
        }

        assert maxWeightServer != null;

        map.put(maxWeightServer.toString(), map.get(maxWeightServer.toString()) - allWeight);

        for (Service service : services) {
            Integer currentWeight = map.get(service.toString());
            map.put(service.toString(), currentWeight + service.getWeight());
        }
        return maxWeightServer;
    }

    public static void main(String[] args) {
        List<Integer> arr = new ArrayList<>();
        arr.add(1);
        arr.add(1);
        arr.add(1);
        System.out.println(arr.toString());


        List<Service> services = new ArrayList<>(3);
        Service service = new Service();
        service.setAddress("196.128.6.1");
        service.setWeight(1);
        services.add(service);

        Service service2 = new Service();
        service2.setAddress("196.128.6.2");
        service2.setWeight(3);
        services.add(service2);

        Service service3 = new Service();
        service3.setAddress("196.128.6.3");
        service3.setWeight(5);
        services.add(service3);

        com.rpc.client.balance.LoadBalance loadBalance = new SmoothWeightRoundBalance();
        System.out.println("20次请求负载均衡结果为:");
        for(int i=1;i<=20;i++){
            System.out.println("第"+i+"次请求服务ip为："+loadBalance.chooseOne(services).getAddress());
        }
    }
}
