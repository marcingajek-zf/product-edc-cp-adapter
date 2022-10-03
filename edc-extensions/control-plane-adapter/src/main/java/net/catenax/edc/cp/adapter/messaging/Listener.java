package net.catenax.edc.cp.adapter.messaging;

public interface Listener<P extends Message<?>> {
  void process(P message);
}
