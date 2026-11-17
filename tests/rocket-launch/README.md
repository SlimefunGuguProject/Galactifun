# Rocket launch integration regression tests

These are **manual integration tests**, not tests executed by `gradlew build`.
The shipping JAR excludes this directory. Never load `RocketTest.jar` on a live
server: it creates synthetic blocks/items, injects faults, replaces its own
journal directory temporarily and disables Galactifun in the final case.

## Environment and commands

1. Build the addon with JDK 17 using `./gradlew --no-daemon clean build shadowJar`.
   This keeps the project's Java 17 target and Paper 1.18.2 API. JDK 25 does not
   run the existing Gradle 8.3 wrapper.
2. Create an isolated Paper 26.3 server with a fresh world and empty Slimefun
   storage, and only Slimefun, GuizhanLibPlugin, this addon and the test kit.
   Bind `server-ip=127.0.0.1`, `server-port=25595`, `rcon.port=25596`,
   `online-mode=false`, `enable-rcon=true`, `pause-when-empty-seconds=-1`,
   `white-list=false`, `level-name=rocket-test`. Use a local random RCON secret.
   In Galactifun, set `worlds.earth-name: rocket-test`; disable other planet
   worlds to keep the fixture small. Disable all auto-updaters and external
   database connections. Do not copy player data or production databases.
3. Create a UTF-8 classpath file containing the **shadow** addon JAR, the exact
   Slimefun test JAR, Paper 26.3 API and its runtime library JARs, separated by
   the operating system's classpath separator. The fixture refers to the
   addon's relocated InfinityLib classes, as used by its packaged JAR.
4. With JDK 25 in `JAVA_HOME`, compile the fixture:

   ```sh
   python3 tests/rocket-launch/build-kit.py \
     --classpath-file "$TEST_CLASSPATH_FILE" \
     --output "$TEST_SERVER/plugins/RocketTest.jar"
   ```

5. Start only that server with JDK 25 and JVM option
   `-Dgalactifun.rocket-launch-tests=true`. Wait for `Done`, successful addon
   enablement and the Slimefun protection-manager delayed initialization.
   The kit refuses any other bind address or game port.
6. Run the suite sequentially. It raises the isolated server to 200 ticks/s,
   so the actual 1,200-tick timeout and 200-tick countdown finish promptly:

   ```sh
   python3 tests/rocket-launch/run-suite.py \
     --server "$TEST_SERVER" --output "$TEST_RESULTS/green.json"
   ```

   `disable` must be last. It calls the real plugin manager's disable method;
   restart the isolated process before another run. To run a subset, repeat
   `--case normal --case targetpartial`. Retain the console log and
   `plugins/RocketTest/results.yml` beside the JSON report.

For RED, run the original JAR first in a separate fresh instance and use
`--baseline`. Normal launch, input cancellation and the countdown lock are
controls; permission/border/overflow/quit/kick/timeout, stale callbacks, full
or disappearing chests and transfer-fault cases should expose the original
failure. The original selection callback is invoked reflectively by its
signature; the fixed implementation uses `beginLaunch`. Fuel IDs come from
`StackUtils.getIdOrType(SlimefunItems.FUEL_BUCKET)`, never the Java constant's
name. The fixture's `unknownfuel` case enters the real `openGUI` method.

## What the suite observes

- Real Paper blocks, Slimefun block storage and live chest inventories.
- A synthetic `Player` interface object, rather than a network client. It
  invokes the selection callback and real Bukkit event handlers/scheduler.
  `asynccancel` dispatches a genuinely asynchronous chat event from the Bukkit
  async executor. UI navigation and a physical client's behavior need a
  separate two-player smoke test.
- INPUT cancellation, command cancellation, quit, uncancelled kick, a cancelled
  kick, timeout, stale queued input, duplicate launches and real plugin disable.
- Countdown keeps rocket/pad locked; quitting during it cancels safely.
- Normal delivery returns exactly one rocket, 12 actual fuel items, seven
  diamonds and three preexisting emeralds. A synthetic destination supplies a
  controlled distance through the real fuel-cost calculation: consuming all
  12 returns zero fuel; consuming five returns exactly seven. Two pilots choosing the same source
  and different destinations can produce only one returned rocket/cargo set.
- Final validation rejects a missing/full chest and source preflight exception
  before removing source assets. Capacity stays local to the selected single
  chest half, matching the original behavior.
- Source-removal exceptions before and after changing the source to air, and a
  partially written destination, require a durable recovery journal. The
  target is never retried automatically. A separate `restore` case loads the
  journal from disk into a real Skull and Slimefun block record and verifies
  fuel, cargo and an independent long-array PDC sentinel.
- A journal path that is a file (not a writable directory) aborts safely before
  any source removal. The fixture restores its own directory afterwards.
- Cancelling a real Pig's teleport leaves all delivered items in the chest and
  reports its location to the pilot. This observes the async teleport result.

For a client smoke test, have two players open the same rocket selector before
selecting a destination, then submit different coordinates. Check that the
second selection is rejected, the countdown cannot be bypassed by breaking
rocket/pad, and only the first landing chest receives the manifest. Verify the
permission denial with the actual region plugin used by that test server.

## Manual recovery boundary

`plugins/Galactifun/rocket-recovery/<session>.yml` is private operational data,
not a replay queue. It stores source coordinates, Slimefun ID, original fuel
and fuel type, block data and a head item containing the original profile/PDC,
plus destination coordinates, before/after inventories and the delivery list.
Phases are `prepared`, `source-removed`, `delivered` and `recovery-required`.
A successful `delivered` record also remains as evidence.

After `recovery-required`, preserve the journal and inspect the exact source
and destination before restoring anything. The destination can be unchanged,
partially written or fully written. Restore only the missing assets from the
source head/PDC and manifest, comparing both inventories; never blindly replay
`delivery`. If the original rocket remains, its launch lock is intentionally
retained until that comparison and repair are complete. For a missing source,
its head/block data, Slimefun ID and fuel fields are sufficient to rebuild the
source, provided any delivered copies are first reconciled.

A hard JVM exit or machine failure may leave INPUT/COUNTDOWN locks or an
ambiguous transfer. There is deliberately no automatic crash-time unlock or
replay: Slimefun keys and world data are not a shared transaction. Normal
quit/kick/timeout/plugin-disable cleanup is covered; hard-exit recovery requires
source/destination/journal inspection by an administrator.
