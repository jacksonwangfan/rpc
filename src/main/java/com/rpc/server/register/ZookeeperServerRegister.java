package com.rpc.server.register;

import com.rpc.common.model.Service;
import com.rpc.common.serializer.ZookeeperSerializer;
import com.alibaba.fastjson.JSON;
import com.rpc.common.constants.RpcConstant;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;

/**
 * zk服务注册器，提供服务注册、暴露服务的能力
 *
 */
public class ZookeeperServerRegister extends DefaultServerRegister {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServerRegister.class);

    private ZkClient zkClient;

    public ZookeeperServerRegister(String zkAddress, Integer port, String protocol, Integer weight) {
        zkClient = new ZkClient(zkAddress);
        zkClient.setZkSerializer(new ZookeeperSerializer());
        this.port = port;
        this.protocol = protocol;
        this.weight = weight;
    }

    /**
     * 服务注册
     *
     * @param so
     * @throws Exception
     */
    @Override
    public void register(ServiceObject so) throws Exception {
        super.register(so);
        Service service = new Service();
        String host = InetAddress.getLocalHost().getHostAddress();
        String address = host + ":" + port;
        service.setAddress(address);
        service.setName(so.getClazz().getName());
        service.setProtocol(protocol);
        service.setWeight(this.weight);
        this.exportService(service);
    }

    /**
     * 服务暴露(其实就是把服务信息保存到Zookeeper上)
     *
     * @param serviceResource
     */
    private void exportService(Service serviceResource) {
        String serviceName = serviceResource.getName();
        String uri = JSON.toJSONString(serviceResource);
        try {
            uri = URLEncoder.encode(uri, RpcConstant.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.PATH_DELIMITER + serviceName + "/service";
        if (!zkClient.exists(servicePath)) {
            // 没有该节点就创建
            zkClient.createPersistent(servicePath, true);
            logger.info("节点已创建：{}",servicePath);
        }

        String uriPath = servicePath + RpcConstant.PATH_DELIMITER + uri;
        if (zkClient.exists(uriPath)) {
            // 删除之前的节点
            logger.info("之前的节点已经清除:{}",uriPath);
            zkClient.delete(uriPath);
        }
        // 创建一个临时节点，会话失效即被清理
        zkClient.createEphemeral(uriPath);
        logger.info("创建一个临时节点：{}",uriPath);
    }
}
