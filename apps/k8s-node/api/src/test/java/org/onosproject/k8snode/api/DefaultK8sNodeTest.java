/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.k8snode.api;

import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.ChassisId;
import org.onlab.packet.IpAddress;
import org.onosproject.k8snode.api.K8sNode.Type;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.ProviderId;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertSame;
import static org.onosproject.k8snode.api.K8sNode.Type.MINION;
import static org.onosproject.k8snode.api.K8sNodeState.COMPLETE;
import static org.onosproject.k8snode.api.K8sNodeState.DEVICE_CREATED;
import static org.onosproject.k8snode.api.K8sNodeState.INIT;
import static org.onosproject.net.Device.Type.SWITCH;

/**
 * Unit test for DefaultK8sNode.
 */
public final class DefaultK8sNodeTest {

    private static final IpAddress TEST_IP = IpAddress.valueOf("10.100.0.3");

    private static final String HOSTNAME_1 = "hostname_1";
    private static final String HOSTNAME_2 = "hostname_2";
    private static final Device DEVICE_1 = createDevice(1);
    private static final Device DEVICE_2 = createDevice(2);

    private static final IpAddress MANAGEMENT_IP = IpAddress.valueOf("10.10.10.10");
    private static final IpAddress DATA_IP = IpAddress.valueOf("20.20.20.20");

    private K8sNode refNode;

    private static final K8sNode K8S_NODE_1 = createNode(
            HOSTNAME_1,
            MINION,
            DEVICE_1,
            TEST_IP,
            INIT);
    private static final K8sNode K8S_NODE_2 = createNode(
            HOSTNAME_1,
            MINION,
            DEVICE_1,
            TEST_IP,
            INIT);
    private static final K8sNode K8S_NODE_3 = createNode(
            HOSTNAME_2,
            MINION,
            DEVICE_2,
            TEST_IP,
            INIT);

    /**
     * Initial setup for this unit test.
     */
    @Before
    public void setUp() {
        refNode = DefaultK8sNode.builder()
                .hostname(HOSTNAME_1)
                .type(MINION)
                .managementIp(MANAGEMENT_IP)
                .dataIp(DATA_IP)
                .intgBridge(DEVICE_1.id())
                .state(COMPLETE)
                .build();
    }

    /**
     * Checks equals method works as expected.
     */
    @Test
    public void testEquality() {
        new EqualsTester().addEqualityGroup(K8S_NODE_1, K8S_NODE_2)
                .addEqualityGroup(K8S_NODE_3)
                .testEquals();
    }

    /**
     * Test object construction.
     */
    @Test
    public void testConstruction() {
        checkCommonProperties(refNode);
        assertSame(refNode.state(), COMPLETE);
        assertEquals(refNode.intgBridge(), DEVICE_1.id());
    }

    /**
     * Checks the functionality of update state method.
     */
    @Test
    public void testUpdateState() {
        K8sNode updatedNode = refNode.updateState(DEVICE_CREATED);

        checkCommonProperties(updatedNode);
        assertSame(updatedNode.state(), DEVICE_CREATED);
    }

    /**
     * Checks the functionality of from method.
     */
    @Test
    public void testFrom() {
        K8sNode updatedNode = DefaultK8sNode.from(refNode).build();

        assertEquals(updatedNode, refNode);
    }

    /**
     * Checks building a node without hostname fails with proper exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithoutHostname() {
        DefaultK8sNode.builder()
                .type(MINION)
                .intgBridge(DEVICE_1.id())
                .managementIp(TEST_IP)
                .dataIp(TEST_IP)
                .state(INIT)
                .build();
    }

    /**
     * Checks building a node without type fails with proper exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithoutType() {
        DefaultK8sNode.builder()
                .hostname(HOSTNAME_1)
                .intgBridge(DEVICE_1.id())
                .managementIp(TEST_IP)
                .dataIp(TEST_IP)
                .state(INIT)
                .build();
    }

    /**
     * Checks building a node without management IP address fails with
     * proper exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithoutManagementIp() {
        DefaultK8sNode.builder()
                .hostname(HOSTNAME_1)
                .type(MINION)
                .intgBridge(DEVICE_1.id())
                .dataIp(TEST_IP)
                .state(INIT)
                .build();
    }

    private void checkCommonProperties(K8sNode node) {
        assertEquals(node.hostname(), HOSTNAME_1);
        assertEquals(node.type(), MINION);
        assertEquals(node.managementIp(), MANAGEMENT_IP);
        assertEquals(node.dataIp(), DATA_IP);
    }

    private static Device createDevice(long devIdNum) {
        return new DefaultDevice(new ProviderId("of", "foo"),
                DeviceId.deviceId(String.format("of:%016d", devIdNum)),
                SWITCH,
                "manufacturer",
                "hwVersion",
                "swVersion",
                "serialNumber",
                new ChassisId(1));
    }

    private static K8sNode createNode(String hostname, Type type,
                                      Device intgBridge, IpAddress ipAddr,
                                      K8sNodeState state) {
        return DefaultK8sNode.builder()
                .hostname(hostname)
                .type(type)
                .intgBridge(intgBridge.id())
                .managementIp(ipAddr)
                .dataIp(ipAddr)
                .state(state)
                .build();
    }
}
