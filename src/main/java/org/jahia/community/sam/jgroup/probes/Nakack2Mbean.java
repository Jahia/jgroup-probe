package org.jahia.community.sam.jgroup.probes;

/**
 * Shared constants for the JGroups NAKACK2 protocol MBean, used by both the probe and the
 * Karaf {@code jgroup:compact} command so the JMX query string and attribute names stay in sync.
 */
public final class Nakack2Mbean {

    /** ObjectName query matching the NAKACK2 protocol MBean for every cluster. */
    public static final String OBJECT_NAME_QUERY = "JGroupsReplication:type=protocol,cluster=*,protocol=NAKACK2";

    public static final String ATTR_UNDELIVERED_MSGS = "xmit_table_undelivered_msgs";
    public static final String ATTR_SIZE_OF_ALL_MESSAGES = "size_of_all_messages";
    public static final String ATTR_XMIT_REQS_RECEIVED = "xmit_reqs_received";
    public static final String ATTR_XMIT_REQS_SENT = "xmit_reqs_sent";
    public static final String ATTR_XMIT_RSPS_RECEIVED = "xmit_rsps_received";
    public static final String ATTR_XMIT_RSPS_SENT = "xmit_rsps_sent";

    private Nakack2Mbean() {
        // constants holder - not instantiable
    }
}
