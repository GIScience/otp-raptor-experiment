package org.opentripplanner.routing.graphfinder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.opentripplanner.model.StopLocation;
import org.opentripplanner.model.TripPattern;
import org.opentripplanner.model.TripTimeOnDate;
import org.opentripplanner.model.base.ToStringBuilder;
import org.opentripplanner.routing.RoutingService;
import org.opentripplanner.routing.stoptimes.ArrivalDeparture;
import org.opentripplanner.transit.model.basic.FeedScopedId;

/**
 * A reference to a pattern at a specific stop.
 * <p>
 * TODO Is this the right package for this?
 */
public class PatternAtStop {

  public String id;
  public StopLocation stop;
  public TripPattern pattern;

  public PatternAtStop(StopLocation stop, TripPattern pattern) {
    this.id = toId(stop, pattern);
    this.stop = stop;
    this.pattern = pattern;
  }

  /**
   * Convert an id generated by the toId method to an instance of PatternAtStop. Uses the supplied
   * routingService to fetch the TripPattern and Stop instances.
   *
   * @see PatternAtStop#toId(StopLocation, TripPattern)
   */
  public static PatternAtStop fromId(RoutingService routingService, String id) {
    String[] parts = id.split(";", 2);
    Base64.Decoder decoder = Base64.getDecoder();
    FeedScopedId stopId = FeedScopedId.parseId(
      new String(decoder.decode(parts[0]), StandardCharsets.UTF_8)
    );
    FeedScopedId patternId = FeedScopedId.parseId(
      new String(decoder.decode(parts[1]), StandardCharsets.UTF_8)
    );
    return new PatternAtStop(
      routingService.getStopForId(stopId),
      routingService.getTripPatternForId(patternId)
    );
  }

  /**
   * Returns a list of stop times for the specific pattern at the stop.
   *
   * @param routingService     An instance of the RoutingService to be used for the timetable
   *                           search
   * @param startTime          Start time for the search. Seconds from UNIX epoch
   * @param timeRange          Searches forward for timeRange seconds from startTime
   * @param numberOfDepartures Number of departures to fetch
   * @param arrivalDeparture   Filter by arrivals, departures, or both
   * @return A list of stop times
   */
  public List<TripTimeOnDate> getStoptimes(
    RoutingService routingService,
    long startTime,
    int timeRange,
    int numberOfDepartures,
    ArrivalDeparture arrivalDeparture
  ) {
    return routingService.stopTimesForPatternAtStop(
      stop,
      pattern,
      startTime,
      timeRange,
      numberOfDepartures,
      arrivalDeparture
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, stop, pattern);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PatternAtStop that = (PatternAtStop) o;
    return (
      Objects.equals(id, that.id) &&
      Objects.equals(stop, that.stop) &&
      Objects.equals(pattern, that.pattern)
    );
  }

  @Override
  public String toString() {
    return ToStringBuilder
      .of(getClass())
      .addStr("id", id)
      .addObj("stop", stop)
      .addObj("pattern", pattern)
      .toString();
  }

  /**
   * Converts the ids of the pattern and stop to an opaque id, which can be supplied to the users to
   * be used for refetching the combination.
   */
  private static String toId(StopLocation stop, TripPattern pattern) {
    Base64.Encoder encoder = Base64.getEncoder();
    return (
      encoder.encodeToString(stop.getId().toString().getBytes(StandardCharsets.UTF_8)) +
      ";" +
      encoder.encodeToString(pattern.getId().toString().getBytes(StandardCharsets.UTF_8))
    );
  }
}
