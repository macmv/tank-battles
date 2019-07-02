package net.macmv.tankbattles.player;

import net.macmv.tankbattles.lib.proto.Tank;

public class Base {
  private int id;
  private int health;
  private int maxHealth;
  private int speed;

  public static Base fromProto(Tank.Base proto) {
    Base newBase = new Base();
    newBase.id = proto.getId();
    newBase.health = proto.getHealth();
    newBase.maxHealth = 100; // TODO: grab max health from database
    newBase.speed = 1; // TODO: grab max health from database
    return newBase;
  }

  public Tank.Base toProto() {
    Tank.Base.Builder newProto = Tank.Base.newBuilder();
    newProto.setId(id);
    newProto.setHealth(health);
    return newProto.build();
  }
}
