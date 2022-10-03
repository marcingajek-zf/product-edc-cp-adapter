package net.catenax.edc.cp.adapter.messaging;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

public abstract class Message<T> {
  @Getter private final String traceId;
  @Getter private final T payload;
  private final AtomicInteger errorNumber = new AtomicInteger();
  private int retryLimit = 3; // TODO configure
  @Getter private Exception finalException;

  public Message(String traceId, T payload, int retryLimit) {
    this(traceId, payload);
    this.retryLimit = retryLimit;
  }

  public Message(String traceId, T payload) {
    this.payload = payload;
    this.traceId = traceId;
  }

  public Message(T payload, int retryLimit) {
    this(payload);
    this.retryLimit = retryLimit;
  }

  public Message(T payload) {
    traceId = UUID.randomUUID().toString();
    this.payload = payload;
  }

  protected long unsucceeded() {
    errorNumber.incrementAndGet();
    return getDelayTime();
  }

  protected void clearErrors() {
    errorNumber.set(0);
  }

  protected boolean canRetry() {
    return errorNumber.get() < retryLimit;
  }

  protected void setFinalException(Exception e) {
    this.finalException = e;
  }

  private int getDelayTime() {
    return errorNumber.get() < 5
        ? errorNumber.get() * 750
        : (int) Math.pow(errorNumber.get(), 2) * 150;
  }
}
