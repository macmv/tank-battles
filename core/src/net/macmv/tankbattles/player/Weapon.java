package net.macmv.tankbattles.player;

import net.macmv.tankbattles.lib.proto.Tank;

public class Weapon {
  private int id;
  private int clipReload;
  private int singleReload;

  public static Weapon fromProto(Tank.Weapon proto) {
    Weapon newWeapon = new Weapon();
    newWeapon.id = proto.getId();
    newWeapon.clipReload = proto.getClipReload();
    newWeapon.singleReload = proto.getSingleReload();
    return newWeapon;
  }

  public Tank.Weapon toProto() {
    Tank.Weapon.Builder newProto = Tank.Weapon.newBuilder();
    newProto.setId(id);
    newProto.setClipReload(clipReload);
    newProto.setSingleReload(singleReload);
    return newProto.build();
  }
}
