package org.jahia.community.sam.jgroup.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.community.sam.jgroup.probes.Nakack2Mbean;

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
        Set<String> results = new HashSet<>();
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = mbs.queryNames(new ObjectName(Nakack2Mbean.OBJECT_NAME_QUERY), null);
            StringBuilder sb = new StringBuilder();
            Object[] params = new Object[0];
            String[] signature = new String[0];
            for (ObjectName name : objectNames) {
                int undelivered = (Integer) mbs.getAttribute(name, Nakack2Mbean.ATTR_UNDELIVERED_MSGS);
                long memUsage = (Long) mbs.getAttribute(name, Nakack2Mbean.ATTR_SIZE_OF_ALL_MESSAGES);
                long reqsReceived = ((AtomicLong) mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_REQS_RECEIVED)).get();
                long reqsSent = ((AtomicLong) mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_REQS_SENT)).get();
                long rspsReceived = ((AtomicLong) mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_RSPS_RECEIVED)).get();
                long rspsSent = ((AtomicLong) mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_RSPS_SENT)).get();
                sb.append("Before compacting NACKACK2 protocol data on ").append(name.getKeyProperty("cluster")).append(" : undelivered_msgs=");
                sb.append(undelivered).append(", mem_usage=");
                sb.append(memUsage).append(", reqs_received=");
                sb.append(reqsReceived).append(", reqs_sent=");
                sb.append(reqsSent).append(", rsps_received=");
                sb.append(rspsReceived).append(", rsps_sent=");
                sb.append(rspsSent).append(" => ");
                mbs.invoke(name, "compact", params, signature);
                undelivered = (Integer) mbs.getAttribute(name, Nakack2Mbean.ATTR_UNDELIVERED_MSGS);
                memUsage = (Long) mbs.getAttribute(name, Nakack2Mbean.ATTR_SIZE_OF_ALL_MESSAGES);
                reqsReceived = ((AtomicLong) mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_REQS_RECEIVED)).get();
                reqsSent = ((AtomicLong) mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_REQS_SENT)).get();
                rspsReceived = ((AtomicLong) mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_RSPS_RECEIVED)).get();
                rspsSent = ((AtomicLong) mbs.getAttribute(name, Nakack2Mbean.ATTR_XMIT_RSPS_SENT)).get();
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
