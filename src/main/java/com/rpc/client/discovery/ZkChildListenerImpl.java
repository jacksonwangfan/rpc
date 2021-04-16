package com.rpc.client.discovery;

import com.rpc.client.cache.ServerDiscoveryCache;
import org.I0Itec.zkclient.IZkChildListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 子节点事件监听处理类，及时剔除已经宕机的服务，当子节点数据发生变化时，清空本地缓存，重新从服务端拉取最新的服务信息
 */
public class ZkChildListenerImpl implements IZkChildListener {

    private static Logger logger = LoggerFactory.getLogger(ZkChildListenerImpl.class);

    /**
     * 监听子节点的删除和新增事件
     * @param parentPath /rpc/serviceName/service
     * @param childList
     * @throws Exception
     */
    @Override
    public void handleChildChange(String parentPath, List<String> childList) throws Exception {
        logger.debug("Child change parentPath:[{}] -- childList:[{}]", parentPath, childList);
        // 只要子节点有改动就清空缓存
        String[] arr = parentPath.split("/");
        ServerDiscoveryCache.removeAll(arr[2]);
    }
}
