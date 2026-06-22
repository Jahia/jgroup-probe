package org.jahia.community.sam.jgroup.probes;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.management.ManagementFactory;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jahia.modules.sam.ProbeStatus;
import org.junit.Test;
import org.mockito.MockedStatic;

public class JGroupNackak2ProbeStatusTest {

    @Test
    public void getStatusWithNoMBeanReturnsGreenStandaloneMessage() {
        MBeanServer mbs = mock(MBeanServer.class);
        when(mbs.queryNames(any(ObjectName.class), any())).thenReturn(new LinkedHashSet<>());

        try (MockedStatic<ManagementFactory> mf = mockStatic(ManagementFactory.class)) {
            mf.when(ManagementFactory::getPlatformMBeanServer).thenReturn(mbs);

            ProbeStatus status = new JGroupNackak2Probe().getStatus();

            assertEquals(ProbeStatus.Health.GREEN, status.getHealth());
            assertEquals("JGroup NACKACK2 protocol not active (standalone or single-node instance)",
                    status.getMessage());
        }
    }

    @Test
    public void getStatusAggregatesToWorstHealthSoRedIsNotMaskedByLaterGreen() throws Exception {
        ObjectName healthy = new ObjectName("JGroupsReplication:type=protocol,cluster=clusterA,protocol=NAKACK2");
        ObjectName unhealthy = new ObjectName("JGroupsReplication:type=protocol,cluster=clusterB,protocol=NAKACK2");

        // LinkedHashSet preserves order: the RED bean is iterated BEFORE the GREEN one,
        // proving the later GREEN does not overwrite/mask the earlier RED.
        Set<ObjectName> names = new LinkedHashSet<>();
        names.add(unhealthy);
        names.add(healthy);

        MBeanServer mbs = mock(MBeanServer.class);
        when(mbs.queryNames(any(ObjectName.class), any())).thenReturn(names);

        // unhealthy bean: undelivered above the RED threshold (default 1_000_000)
        stubBean(mbs, unhealthy, 2_000_000, 0L);
        // healthy bean: everything at zero
        stubBean(mbs, healthy, 0, 0L);

        try (MockedStatic<ManagementFactory> mf = mockStatic(ManagementFactory.class)) {
            mf.when(ManagementFactory::getPlatformMBeanServer).thenReturn(mbs);

            ProbeStatus status = new JGroupNackak2Probe().getStatus();

            assertEquals(ProbeStatus.Health.RED, status.getHealth());
        }
    }

    private static void stubBean(MBeanServer mbs, ObjectName name, int undelivered, long memUsage) throws Exception {
        when(mbs.getAttribute(name, Nakack2Mbean.ATTR_UNDELIVERED_MSGS)).thenReturn(undelivered);
        when(mbs.getAttribute(name, Nakack2Mbean.ATTR_SIZE_OF_ALL_MESSAGES)).thenReturn(memUsage);
        when(mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_REQS_RECEIVED)).thenReturn(new AtomicLong(0));
        when(mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_REQS_SENT)).thenReturn(new AtomicLong(0));
        when(mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_RSPS_RECEIVED)).thenReturn(new AtomicLong(0));
        when(mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_RSPS_SENT)).thenReturn(new AtomicLong(0));
    }
}
