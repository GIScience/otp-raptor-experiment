package org.opentripplanner.raptor.heigit_experiments;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.opentripplanner.raptor.spi.IntIterator;


class CollectionBasedIntIterator implements IntIterator {

  public CollectionBasedIntIterator(Collection<Integer> data) {
    this.data = data.iterator();
  }

  Iterator<Integer> data;


  @Override
  public int next() {
    return this.data.next();
  }

  @Override
  public boolean hasNext() {
    return this.data.hasNext();
  }

  static Set<Integer> toSet(IntIterator iterator) {
    Set<Integer> result = new LinkedHashSet<>();
    while(iterator.hasNext())
      result.add(iterator.next());

    return result;
  }
}
