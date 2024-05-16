package org.opentripplanner.ext.siri;

import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.opentripplanner.framework.time.CountdownTimer;
import org.opentripplanner.model.TimetableSnapshot;
import org.opentripplanner.model.TimetableSnapshotProvider;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.mappers.TransitLayerUpdater;
import org.opentripplanner.updater.TimetableSnapshotSourceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractTimetableSnapshotSource implements TimetableSnapshotProvider {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractTimetableSnapshotSource.class);
  private final TransitLayerUpdater transitLayerUpdater;
  /**
   * Lock to indicate that buffer is in use
   */
  protected final ReentrantLock bufferLock = new ReentrantLock(true);

  /**
   * The working copy of the timetable snapshot. Should not be visible to routing threads. Should
   * only be modified by a thread that holds a lock on {@link #bufferLock}. All public methods that
   * might modify this buffer will correctly acquire the lock.
   */
  protected final TimetableSnapshot buffer = new TimetableSnapshot();

  /**
   * The last committed snapshot that was handed off to a routing thread. This snapshot may be given
   * to more than one routing thread if the maximum snapshot frequency is exceeded.
   */
  private volatile TimetableSnapshot snapshot = null;

  /**
   * If a timetable snapshot is requested less than this number of milliseconds after the previous
   * snapshot, just return the same one. Throttles the potentially resource-consuming task of
   * duplicating a TripPattern -> Timetable map and indexing the new Timetables.
   */
  private final CountdownTimer snapshotFrequencyThrottle;

  /**
   * We inject a provider to retrieve the current service-date(now). This enables us to unit-test
   * the purgeExpiredData feature.
   */
  private final Supplier<LocalDate> localDateNow;

  private LocalDate lastPurgeDate = null;

  public AbstractTimetableSnapshotSource(
    TransitLayerUpdater transitLayerUpdater,
    TimetableSnapshotSourceParameters parameters,
    Supplier<LocalDate> localDateNow
  ) {
    this.transitLayerUpdater = transitLayerUpdater;
    this.snapshotFrequencyThrottle = new CountdownTimer(parameters.maxSnapshotFrequency());
    this.localDateNow = localDateNow;
    // Force commit so that snapshot initializes
    commitTimetableSnapshot(true);
  }

  /**
   * @return an up-to-date snapshot mapping TripPatterns to Timetables. This snapshot and the
   * timetable objects it references are guaranteed to never change, so the requesting thread is
   * provided a consistent view of all TripTimes. The routing thread need only release its reference
   * to the snapshot to release resources.
   */
  public final TimetableSnapshot getTimetableSnapshot() {
    // Try to get a lock on the buffer
    if (bufferLock.tryLock()) {
      // Make a new snapshot if necessary
      try {
        commitTimetableSnapshot(false);
        return snapshot;
      } finally {
        bufferLock.unlock();
      }
    }
    // No lock could be obtained because there is either a snapshot commit busy or updates
    // are applied at this moment, just return the current snapshot
    return snapshot;
  }

  public final void commitTimetableSnapshot(final boolean force) {
    if (force || snapshotFrequencyThrottle.timeIsUp()) {
      if (force || buffer.isDirty()) {
        LOG.debug("Committing {}", buffer);
        snapshot = buffer.commit(transitLayerUpdater, force);

        // We only reset the timer when the snapshot is updated. This will cause the first
        // update to be committed after a silent period. This should not have any effect in
        // a busy updater. It is however useful when manually testing the updater.
        snapshotFrequencyThrottle.restart();
      } else {
        LOG.debug("Buffer was unchanged, keeping old snapshot.");
      }
    } else {
      LOG.debug("Snapshot frequency exceeded. Reusing snapshot {}", snapshot);
    }
  }

  protected final boolean purgeExpiredData() {
    final LocalDate today = localDateNow.get();
    // TODO: Base this on numberOfDaysOfLongestTrip for tripPatterns
    final LocalDate previously = today.minusDays(2); // Just to be safe...

    // Purge data only if we have changed date
    if (lastPurgeDate != null && lastPurgeDate.compareTo(previously) >= 0) {
      return false;
    }

    LOG.debug("purging expired realtime data");

    lastPurgeDate = previously;

    return buffer.purgeExpiredData(previously);
  }

  protected final LocalDate localDateNow() {
    return localDateNow.get();
  }
}
