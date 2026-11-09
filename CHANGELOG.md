# Changelog

All notable changes to the JGroup NAKACK2 Probe module are documented in this file.

## [Unreleased]

### Fixed
- `getStatus()` no longer reports the misleading "Cluster revisions are in-sync" message (a leftover from another probe) when no NAKACK2 MBean is present. A standalone / single-node instance now returns a clear GREEN status.
- Health is now aggregated to the **worst** severity across all matched NAKACK2 MBeans; previously the loop overwrote the result each iteration, so a later healthy cluster could mask an earlier RED one.
- Probe health is computed in a local variable instead of a shared mutable instance field, removing a race condition on the singleton OSGi component.
- README documented `undeliveredMessageRedThreshold=10000000` but the module default is `1000000`; the README now matches the code, and the `jgroup:compact` Karaf command is now documented.

### Changed
- Extracted the NAKACK2 JMX ObjectName and attribute names into a shared constants holder (removes literal duplication across the probe and the `jgroup:compact` command).
- Removed unused imports and applied the diamond operator (SonarQube cleanups).

### Added
- First unit-test suite (JUnit 4 + Mockito, 12 tests) covering human-readable size parsing, threshold configuration, the standalone empty-MBean case, and worst-severity aggregation. JaCoCo coverage wiring for SonarQube.
