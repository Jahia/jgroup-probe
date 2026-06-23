package org.jahia.community.sam.jgroup.probes;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class JGroupNackak2ProbeParsingTest {

    @Test
    public void parsePlainNumberReturnsBytes() {
        assertEquals(100L, JGroupNackak2Probe.parseHumanReadableSize("100"));
    }

    @Test
    public void parseKilobytes() {
        assertEquals(1024L, JGroupNackak2Probe.parseHumanReadableSize("1KB"));
    }

    @Test
    public void parseMegabytes() {
        assertEquals(512L * 1024L * 1024L, JGroupNackak2Probe.parseHumanReadableSize("512MB"));
    }

    @Test
    public void parseGigabytes() {
        assertEquals(1073741824L, JGroupNackak2Probe.parseHumanReadableSize("1GB"));
    }

    @Test
    public void parseFractionalGigabytes() {
        assertEquals((long) (1.5d * 1073741824L), JGroupNackak2Probe.parseHumanReadableSize("1.5GB"));
    }

    @Test
    public void parseIsCaseInsensitive() {
        assertEquals(1073741824L, JGroupNackak2Probe.parseHumanReadableSize("1gb"));
    }

    @Test
    public void parseNonMatchingStringReturnsMinusOne() {
        assertEquals(-1L, JGroupNackak2Probe.parseHumanReadableSize("abc"));
    }

    @Test
    public void setConfigPositiveValuesOverrideDefaults() {
        JGroupNackak2Probe probe = new JGroupNackak2Probe();
        Map<String, Object> config = new HashMap<>();
        config.put("undeliveredMessageYellowThreshold", "5");
        config.put("undeliveredMessageRedThreshold", "10");
        config.put("memoryUsageYellowThreshold", "1KB");
        config.put("memoryUsageRedThreshold", "2KB");

        probe.setConfig(config);

        assertEquals(5L, probe.getUndeliveredMessageYellowThreshold());
        assertEquals(10L, probe.getUndeliveredMessageRedThreshold());
        assertEquals(1024L, probe.getMemoryUsageYellowThreshold());
        assertEquals(2048L, probe.getMemoryUsageRedThreshold());
    }

    @Test
    public void setConfigZeroOrNegativeValuesAreIgnored() {
        JGroupNackak2Probe probe = new JGroupNackak2Probe();
        long defaultUndeliveredYellow = probe.getUndeliveredMessageYellowThreshold();
        long defaultUndeliveredRed = probe.getUndeliveredMessageRedThreshold();
        long defaultMemYellow = probe.getMemoryUsageYellowThreshold();
        long defaultMemRed = probe.getMemoryUsageRedThreshold();

        Map<String, Object> config = new HashMap<>();
        config.put("undeliveredMessageYellowThreshold", "0");
        config.put("undeliveredMessageRedThreshold", "-1");
        config.put("memoryUsageYellowThreshold", "0");
        // "abc" -> parseHumanReadableSize returns -1, which is ignored by the > 0 guard
        config.put("memoryUsageRedThreshold", "abc");

        probe.setConfig(config);

        assertEquals(defaultUndeliveredYellow, probe.getUndeliveredMessageYellowThreshold());
        assertEquals(defaultUndeliveredRed, probe.getUndeliveredMessageRedThreshold());
        assertEquals(defaultMemYellow, probe.getMemoryUsageYellowThreshold());
        assertEquals(defaultMemRed, probe.getMemoryUsageRedThreshold());
    }

    @Test
    public void setConfigMissingKeysRetainDefaults() {
        JGroupNackak2Probe probe = new JGroupNackak2Probe();
        long defaultUndeliveredYellow = probe.getUndeliveredMessageYellowThreshold();
        long defaultUndeliveredRed = probe.getUndeliveredMessageRedThreshold();
        long defaultMemYellow = probe.getMemoryUsageYellowThreshold();
        long defaultMemRed = probe.getMemoryUsageRedThreshold();

        probe.setConfig(new HashMap<>());

        assertEquals(defaultUndeliveredYellow, probe.getUndeliveredMessageYellowThreshold());
        assertEquals(defaultUndeliveredRed, probe.getUndeliveredMessageRedThreshold());
        assertEquals(defaultMemYellow, probe.getMemoryUsageYellowThreshold());
        assertEquals(defaultMemRed, probe.getMemoryUsageRedThreshold());
    }
}
