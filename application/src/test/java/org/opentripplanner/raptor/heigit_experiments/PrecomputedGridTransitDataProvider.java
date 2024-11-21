package org.opentripplanner.raptor.heigit_experiments;

public class PrecomputedGridTransitDataProvider extends SynthGridTransitDataProvider {

  public PrecomputedGridTransitDataProvider(int size) {
    super(size);
  }

  public PrecomputedGridTransitDataProvider() {
    super(10);
  }


  /**
   * This method is called once, right after the constructor, before the routing start.
   * <p>
   * Strictly not needed, logic can be moved to constructor, but is separated out to be able to
   * measure performance as part of the route method.
   */
  @Override
  public void setup() {
    super.setup();
  }
}
