package net.macmv.tankbattles.collision;

import com.badlogic.gdx.math.Vector3;

public class Hitbox {
  private final Vector3 pos;
  private final Vector3 size;
  private final Vector3 rot;
  private final Vector3 vel;

  public Hitbox(Vector3 pos, Vector3 size) {
    this.pos = pos;
    this.size = size;
    rot = new Vector3();
    vel = new Vector3();
  }

  // uses pos and rot to calculate corner
  // example of corner: top corner -> vec3(0, 1, 0)
  public Vector3 getCorner(Vector3 corner) {
    float x = pos.x + size.x * corner.x;
    float y = pos.y + size.y * corner.y;
    float z = pos.z + size.z * corner.z;
    float rotatedX = 0;
    float rotatedY = 0;
    float rotatedZ = 0;
    float cosX = (float) Math.cos(rot.x);
    float cosY = (float) Math.cos(rot.y);
    float cosZ = (float) Math.cos(rot.z);
    float sinX = (float) Math.sin(rot.x);
    float sinY = (float) Math.sin(rot.y);
    float sinZ = (float) Math.sin(rot.z);
    rotatedY += y * cosX - z * sinX; // just google this
    rotatedZ += z * cosX + y * sinX;
    rotatedX += x * cosY - z * sinY;
    rotatedZ += z * cosY + x * sinY;
    rotatedX += x * cosZ - y * sinZ;
    rotatedY += y * cosZ + x * sinZ;
    return new Vector3(rotatedX, rotatedY, rotatedZ);
  }

  // updates pos and rot so that the corner is moved to given pos
  public void setCorner(Vector3 corner, Vector3 newPos) {
  }

  public Vector3 getRot() {
    return rot;
  }

  public void setVelocity(Vector3 vel) {
    this.vel.set(vel);
  }
}
