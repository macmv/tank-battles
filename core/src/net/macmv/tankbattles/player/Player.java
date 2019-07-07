package net.macmv.tankbattles.player;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.Game;
import net.macmv.tankbattles.lib.proto.Point2;
import net.macmv.tankbattles.lib.proto.Point3;
import net.macmv.tankbattles.render.Skin;

public class Player {

  private int id; // rand(0, MAX_INT)
  private Vector3 pos;
  private Tank tank;
  private int direction; // in degrees
  private Vector2 turretDirection; // degrees, -1 to 1
  private Vector2 turretTarget; // degrees, -1 to 1

  public Player(int id, net.macmv.tankbattles.lib.proto.Tank tank) {
    this.id = id;
    this.tank = Tank.fromProto(tank, true);
    pos = new Vector3();
    turretDirection = new Vector2();
    turretTarget = new Vector2();
  }

  public Player(int id, net.macmv.tankbattles.lib.proto.Tank tank, boolean loadTexture) {
    this.id = id;
    System.out.println(loadTexture);
    this.tank = Tank.fromProto(tank, loadTexture);
    pos = new Vector3();
    turretDirection = new Vector2();
    turretTarget = new Vector2();
  }

  public Player() {
    id = (int) (Math.random() * Integer.MAX_VALUE);
    tank = new Tank(Skin.getDefault());
    pos = new Vector3();
    turretDirection = new Vector2();
    turretTarget = new Vector2();
  }

  public void updateAnimations() {
    updateAnimations(pos, direction);
  }

  public void updateAnimations(Point3 pos, int direction) {
    this.pos.set(pos.getX(), pos.getY(), pos.getZ());
    updateAnimations(this.pos, direction);
  }

  public void updateAnimations(Vector3 pos, int direction) {
    this.pos.set(pos);
    this.direction = direction;
    if (tank.useTexture && tank.getModel() != null) {
      tank.getModel().transform.setToRotation(Vector3.Y, -direction + 180);
      tank.getModel().transform.setTranslation(pos.x, pos.y, pos.z);
    }
    float right = 0; // TODO: animations for treads
    float left = 0;
    tank.updateAnimations(right, left);
    if (turretDirection.x > turretTarget.x - 2 && turretDirection.x < turretTarget.x + 2) {
      turretDirection.x = turretTarget.x;
    } else {
      if (turretDirection.x - turretTarget.x > 180 || turretDirection.x - turretTarget.x < -180) {
        if (turretDirection.x - turretTarget.x > 0) {
          turretDirection.x += 1;
        } else {
          turretDirection.x -= 1;
        }
      } else {
        if (turretDirection.x - turretTarget.x > 0) {
          turretDirection.x -= 1;
        } else {
          turretDirection.x += 1;
        }
      }
      turretDirection.x = (turretDirection.x + 360) % 360;
    }
    tank.setTurretRotation(direction - turretDirection.x);
  }

  public static Player fromProto(net.macmv.tankbattles.lib.proto.Player p) {
    Player newPlayer = new Player();
    newPlayer.id = p.getId();
    newPlayer.pos = new Vector3(p.getPos().getX(), p.getPos().getY(), p.getPos().getZ());
    newPlayer.tank = Tank.fromProto(p.getTank());
    newPlayer.direction = p.getDirection();
    newPlayer.updateAnimations();
    newPlayer.turretDirection = new Vector2(p.getTurretDirection().getX(), p.getTurretDirection().getY());
    return newPlayer;
  }

  public int getId() {
    return id;
  }

  public Tank getTank() {
    return tank;
  }

  public net.macmv.tankbattles.lib.proto.Player toProto() {
    net.macmv.tankbattles.lib.proto.Player.Builder newProto = net.macmv.tankbattles.lib.proto.Player.newBuilder();
    newProto.setId(id);
    newProto.setPos(Point3.newBuilder().setX(pos.x).setY(pos.y).setZ(pos.z).build());
    newProto.setTank(tank.toProto());
    newProto.setDirection(direction);
    newProto.setTurretDirection(Point2.newBuilder().setX(turretDirection.x).setY(turretDirection.y).build());
    return newProto.build();
  }

  public void fire(Game game) {
    Vector2 vel = new Vector2(10, 0).setAngle(turretDirection.x - 90);
    game.sendProjectile(new Vector3(pos.x, pos.y + 1.5f, pos.z), new Vector3(vel.x, turretTarget.y * 10 + 2, vel.y));
  }

  public void move(int right, int left, float deltaTime) {
    // right is speed of right tread, left speed of left tread
    float deltaVel = (right + left) / 2f;
    int deltaDir = (left - right) % 2;
    if (deltaDir != 0) {
      if (deltaVel < 0) {
        deltaVel -= 0.1;
      } else {
        deltaVel += 0.1;
      }
    }
    if (left == -right && right != 0 && left != 0) {
      deltaDir = left;
    }
    float turretDirChange = deltaDir * deltaTime * ((left != 0 && right != 0) ? 150 : 80);
    direction += turretDirChange;
    turretDirection.x += turretDirChange;
    float x = (float) Math.cos((direction - 90) / 180.0 * Math.PI);
    float y = (float) Math.sin((direction - 90) / 180.0 * Math.PI);
    Vector3 posDelta = new Vector3(x, 0, y).scl(deltaVel * deltaTime * 1.75f); // 1.75 is speed
    pos.add(posDelta);
    // TODO: check collisions with stuff here
    if (false/* collision */) {
      pos.sub(posDelta);
    }
    updateAnimations();
  }

  public Vector3 getPos() {
    return pos;
  }

  public void requireAssets(AssetManager assetManager) {
    tank.requireAssets(assetManager);
  }

  public void loadAssets(AssetManager assetManager) {
    tank.loadAssets(assetManager);
  }

  public int getDirection() {
    return direction;
  }

  public void setTurretTarget(float angle, float y) {
    turretTarget = new Vector2(angle % 360, y);
  }

  public void setTurretDirection(Point2 turretDirection) {
    this.turretDirection.set(turretDirection.getX(), turretDirection.getY());
    turretTarget.set(this.turretDirection);
  }

  public void setPos(Point3 pos) {
    this.pos.set(pos.getX(), pos.getY(), pos.getZ());
  }

  public void updatePos(Vector3 newPos, int direction) {
    this.pos.set(newPos);
    this.direction = direction;
  }
}
