package org.whispersystems.bithub.storage;

import org.whispersystems.bithub.entities.Payment;

public class CurrentPayment {

  private final byte[]  badge;
  private final byte[]  smallBadge;
  private final Payment entity;

  protected CurrentPayment(byte[] badge, byte[] smallBadge, Payment entity) {
    this.badge      = badge;
    this.smallBadge = smallBadge;
    this.entity     = entity;
  }

  public byte[] getBadge() {
    return badge;
  }

  public byte[] getSmallBadge() {
    return smallBadge;
  }

  public Payment getEntity() {
    return entity;
  }

}
