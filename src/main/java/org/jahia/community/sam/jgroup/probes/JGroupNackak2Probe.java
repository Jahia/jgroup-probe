package org.jahia.community.sam.jgroup.probes;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.core.util.db.ConnectionHelper;
import org.apache.jackrabbit.core.util.db.DbUtility;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.utils.FileUtils;
import org.osgi.service.component.annotations.Component;

import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.utils.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;

@Component(immediate = true, service = Probe.class)
public class JGroupNackak2Probe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(JGroupNackak2Probe.class);
    private long undeliveredMessageYellowThreshold = 100000L;
    private long undeliveredMessageRedThreshold = 1000000L;
    private long memoryUsageRedThreshold = parseHumanReadableSize("1GB"); // 1 GB
    private long memoryUsageYellowThreshold = parseHumanReadableSize("512MB"); // 512 MB
    private ProbeStatus status=new ProbeStatus("Cluster revisions are in-sync", ProbeStatus.Health.GREEN);

    @Override
    public String getName() {
        return "JGroupNackak2Probe";
    }

    @Override
    public String getDescription() {
        return "Checking that the JGroup NACKACK2 protocol is not causing cluster nodes to OOM";
    }

    @Override
    public ProbeStatus getStatus() {
        // Add a dedicated entry for monitoring NACACK2 protocol usage
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = mbs.queryNames(new ObjectName("JGroupsReplication:type=protocol,cluster=*,protocol=NAKACK2"), null);
            for (ObjectName name : objectNames) {
                int undelivered = (Integer) mbs.getAttribute(name, "xmit_table_undelivered_msgs");
                long memUsage = (Long) mbs.getAttribute(name, "size_of_all_messages");
                long reqsReceived = ((AtomicLong) mbs.getAttribute(name, "xmit_reqs_received")).get();
                long reqsSent = ((AtomicLong) mbs.getAttribute(name, "xmit_reqs_sent")).get();
                long rspsReceived = ((AtomicLong) mbs.getAttribute(name, "xmit_rsps_received")).get();
                long rspsSent = ((AtomicLong) mbs.getAttribute(name, "xmit_rsps_sent")).get();
                // Example of values:
                // "xmit_table_undelivered_msgs": "0",
                // "size_of_all_messages": "0",
                // "xmit_reqs_received": "3",
                // "xmit_reqs_sent": "6",
                // "xmit_rsps_received": "6",
                // "xmit_rsps_sent": "3",
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("JGroup NACKACK2 Protocol stats - undelivered: {}, memUsage: {}, reqsReceived: {}, reqsSent: {}, rspsReceived: {}, rspsSent: {}",
                            undelivered, memUsage, reqsReceived, reqsSent, rspsReceived, rspsSent);
                }
                if (undelivered > undeliveredMessageRedThreshold || memUsage > memoryUsageRedThreshold) {
                    status = new ProbeStatus("JGroup NACKACK2 Protocol shows high undelivered messages or memory usage: undelivered=" + undelivered + ", memUsage=" + FileUtils.humanReadableByteCount(memUsage),
                            ProbeStatus.Health.RED);
                } else if (undelivered > undeliveredMessageYellowThreshold || memUsage > memoryUsageYellowThreshold) {
                    status = new ProbeStatus("JGroup NACKACK2 Protocol shows elevated undelivered messages or memory usage: undelivered=" + undelivered + ", memUsage=" + FileUtils.humanReadableByteCount(memUsage),
                            ProbeStatus.Health.YELLOW);
                }

                else {
                    status = new ProbeStatus("JGroup NACKACK2 Protocol is healthy: undelivered=" + undelivered + ", memUsage=" + FileUtils.humanReadableByteCount(memUsage),
                            ProbeStatus.Health.GREEN);
                }
            }
        } catch (MalformedObjectNameException | MBeanException | AttributeNotFoundException |
                 InstanceNotFoundException | ReflectionException e) {
            status = new ProbeStatus("Error while checking JGroup NACKACK2 Protocol: " + e.getMessage(),
                    ProbeStatus.Health.RED);
        }
        return status;
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.CRITICAL;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if(config.containsKey("undeliveredMessageYellowThreshold")) {
            long yellowThreshold = Long.parseLong(config.get("undeliveredMessageYellowThreshold").toString());
            undeliveredMessageYellowThreshold = yellowThreshold > 0 ? yellowThreshold : undeliveredMessageYellowThreshold;
        }
        if(config.containsKey("undeliveredMessageRedThreshold")) {
            long redThreshold = Long.parseLong(config.get("undeliveredMessageRedThreshold").toString());
            undeliveredMessageRedThreshold = redThreshold > 0 ? redThreshold : undeliveredMessageRedThreshold;
        }
        if(config.containsKey("memoryUsageYellowThreshold")) {
            long yellowThreshold = parseHumanReadableSize(config.get("memoryUsageYellowThreshold").toString());
            memoryUsageYellowThreshold = yellowThreshold > 0 ? yellowThreshold : memoryUsageYellowThreshold;
        }
        if(config.containsKey("memoryUsageRedThreshold")) {
            long redThreshold = parseHumanReadableSize(config.get("memoryUsageRedThreshold").toString());
            memoryUsageRedThreshold = redThreshold > 0 ? redThreshold : memoryUsageRedThreshold;
        }
    }

    public static long parseHumanReadableSize(String fileSize) {
        long returnValue = -1;
        Pattern patt = Pattern.compile("([\\d.]+)([GMK]?B?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patt.matcher(fileSize.trim());

        Map<String, Integer> powerMap = new HashMap<>();
        powerMap.put("B", 0);
        powerMap.put("", 0); // Handle cases like "100" meaning 100 bytes
        powerMap.put("KB", 1);
        powerMap.put("K", 1);
        powerMap.put("MB", 2);
        powerMap.put("M", 2);
        powerMap.put("GB", 3);
        powerMap.put("G", 3);
        // Add more units as needed (TB, PB, etc.)

        if (matcher.find()) {
            String numberStr = matcher.group(1);
            String unitStr = matcher.group(2).toUpperCase();

            int pow = powerMap.getOrDefault(unitStr, 0); // Default to bytes if unit not found

            BigDecimal bytes = new BigDecimal(numberStr);
            bytes = bytes.multiply(BigDecimal.valueOf(1024).pow(pow));
            returnValue = bytes.longValue();
        }
        return returnValue;
    }
}
