
# Server Availability Manager - JGroup NAKACK2 protocol probe

This probe check how many messages are pending acknowledgment in the JGroup NAKACK2 protocol, which can indicate network issues or slow nodes in a Jahia cluster.
Those pending messages can lead to increased latency and potential timeouts in cluster communication as well as Out Of Memory (OOM) errors.

## Installation

- In Jahia, go to "Administration --> Server settings --> System components --> Modules"
- Upload the JAR **jgroup-probe-X.X.X.jar**
- Check that the module is started

## How to use
The probe will be automatically added to the Server Availability Manager output (read [this page](https://academy.jahia.com/documentation/system-administrator/dev-ops/monitoring-your-jahia-platform/monitoring-your-servers) for more information).

## Configuration
You can configure the thresholds for warning and critical states by adding the following properties to your `org.jahia.modules.sam.healthcheck.ProbesRegistry.cfg` file:

```properties
probes.JGroupNackak2Probe.undeliveredMessageYellowThreshold=100000
probes.JGroupNackak2Probe.undeliveredMessageRedThreshold=1000000
probes.JGroupNackak2Probe.memoryUsageYellowThreshold=512MB
probes.JGroupNackak2Probe.memoryUsageRedThreshold=1GB
```

## Karaf command

The module also ships a Karaf shell command that triggers a compaction of the
NAKACK2 retransmission table and prints the before/after statistics per cluster:

```
jgroup:compact
```
