package org.jahia.community.sam.jgroup.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Command(scope = "jgroup", name = "compact", description = "Compact NACKACK2 protocol data")
@Service()
public class Compact implements Action {
    @Override
    public Object execute() throws Exception {
        Set<String> results = new HashSet<String>();
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = mbs.queryNames(new ObjectName("JGroupsReplication:type=protocol,cluster=*,protocol=NAKACK2"), null);
            StringBuilder sb = new StringBuilder();
            Object[] params = new Object[] {};
            String[] signature = new String[] {};
            for (ObjectName name : objectNames) {
                int undelivered = (Integer) mbs.getAttribute(name, "xmit_table_undelivered_msgs");
                long memUsage = (Long) mbs.getAttribute(name, "size_of_all_messages");
                long reqsReceived = ((AtomicLong) mbs.getAttribute(name, "xmit_reqs_received")).get();
                long reqsSent = ((AtomicLong) mbs.getAttribute(name, "xmit_reqs_sent")).get();
                long rspsReceived = ((AtomicLong) mbs.getAttribute(name, "xmit_rsps_received")).get();
                long rspsSent = ((AtomicLong) mbs.getAttribute(name, "xmit_rsps_sent")).get();
                sb.append("Before compacting NACKACK2 protocol data on ").append(name.getKeyProperty("cluster")).append(" : undelivered_msgs=");
                sb.append(undelivered).append(", mem_usage=");
                sb.append(memUsage).append(", reqs_received=");
                sb.append(reqsReceived).append(", reqs_sent=");
                sb.append(reqsSent).append(", rsps_received=");
                sb.append(rspsReceived).append(", rsps_sent=");
                sb.append(rspsSent).append(" => ");
                mbs.invoke(name, "compact", params, signature);
                undelivered = (Integer) mbs.getAttribute(name, "xmit_table_undelivered_msgs");
                memUsage = (Long) mbs.getAttribute(name, "size_of_all_messages");
                reqsReceived = ((AtomicLong) mbs.getAttribute(name, "xmit_reqs_received")).get();
                reqsSent = ((AtomicLong) mbs.getAttribute(name, "xmit_reqs_sent")).get();
                rspsReceived = ((AtomicLong) mbs.getAttribute(name, "xmit_rsps_received")).get();
                rspsSent = ((AtomicLong) mbs.getAttribute(name, "xmit_rsps_sent")).get();
                sb.append("After compacting NACKACK2 protocol data: ");
                sb.append("undelivered_msgs=");
                sb.append(undelivered).append(", mem_usage=");
                sb.append(memUsage).append(", reqs_received=");
                sb.append(reqsReceived).append(", reqs_sent=");
                sb.append(reqsSent).append(", rsps_received=");
                sb.append(rspsReceived).append(", rsps_sent=");
                sb.append(rspsSent).append("\n");
                results.add(sb.toString());
                sb.setLength(0);
            }
        } catch (Exception e) {
            results.add(e.getMessage());
        }
        return results;
    }
}
