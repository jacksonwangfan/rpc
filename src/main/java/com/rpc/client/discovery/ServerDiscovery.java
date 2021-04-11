package com.rpc.client.discovery;

import com.rpc.common.model.Service;

import java.util.List;

/**
 * 服务发现抽象类
 */
public interface ServerDiscovery {

    List<Service> findServiceListByRegisterCenter(String name);
}
