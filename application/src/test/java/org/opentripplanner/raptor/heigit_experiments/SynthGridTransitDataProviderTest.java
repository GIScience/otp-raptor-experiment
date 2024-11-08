package org.opentripplanner.raptor.heigit_experiments;

import static org.junit.jupiter.api.Assertions.*;
import static org.opentripplanner.raptor.heigit_experiments.CollectionBasedIntIterator.toSet;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor.spi.IntIterator;

class SynthGridTransitDataProviderTest {

  SynthGridTransitDataProvider dataProvider = new SynthGridTransitDataProvider();


  @Test
  void routeIndexIterator() {

    CollectionBasedIntIterator stops = new CollectionBasedIntIterator(List.of(0, 10, 46, 77, 95));
    IntIterator iterator = this.dataProvider.routeIndexIterator(stops);

    assertEquals(Set.of(0, 1, 4, 7, 9, 10, 15, 16, 17), toSet(iterator));

  }


  @Test
  void numberOfStops() {

    int stops = this.dataProvider.numberOfStops();
    assertEquals(100, stops);
  }


}