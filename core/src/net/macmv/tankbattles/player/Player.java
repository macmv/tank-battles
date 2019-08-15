package net.macmv.tankbattles.player;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.collision.CollisionManager;
import net.macmv.tankbattles.collision.Hitbox;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.Point2;
import net.macmv.tankbattles.lib.proto.Point3;
import net.macmv.tankbattles.render.Skin;

public class Player {

  private int id; // rand(0, MAX_INT)
  private Vector3 pos;
  private Tank tank;
  private Vector2 turretDirection; // degrees, -1 to 1
  private Vector2 turretTarget; // degrees, -1 to 1
  private final Hitbox hitbox;

  public Player(CollisionManager collisionManager, int id, net.macmv.tankbattles.lib.proto.Tank tank) {
    this.id = id;
    this.tank = Tank.fromProto(tank, true);
    pos = new Vector3();
    turretDirection = new Vector2();
    turretTarget = new Vector2();
    hitbox = new Hitbox(this.pos, new Vector3(1, 0.5f, 1));
  }

  public Player(CollisionManager collisionManager, int id, net.macmv.tankbattles.lib.proto.Tank tank, boolean loadTexture) {
    this.id = id;
    System.out.println(loadTexture);
    this.tank = Tank.fromProto(tank, loadTexture);
    pos = new Vector3();
    turretDirection = new Vector2();
    turretTarget = new Vector2();
    hitbox = new Hitbox(this.pos, new Vector3(1, 0.5f, 1));
  }

  public Player(CollisionManager collisionManager) {
    id = (int) (Math.random() * Integer.MAX_VALUE);
    tank = new Tank(Skin.getDefault());
    pos = new Vector3();
    turretDirection = new Vector2();
    turretTarget = new Vector2();
    hitbox = new Hitbox(this.pos, new Vector3(1, 0.5f, 1));
  }

  public float getRot() {
    return hitbox.getRot().z;
  }

  public void updateAnimations() {
    if (tank.useTexture && tank.getModel() != null) {
      tank.getModel().transform.setToRotation(Vector3.Y, getRot());
      tank.getModel().transform.setTranslation(pos);
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
      turretDirection.x = (turretDirection.x + 360) % 360; // fix to 0 - 360
    }
    tank.setTurretRotation(((getRot() + turretDirection.x + 180) * -1 + 360) % 360);
  }

  public static Player fromProto(CollisionManager collisionManager, net.macmv.tankbattles.lib.proto.Player p) {
    Player newPlayer = new Player(collisionManager);
    newPlayer.id = p.getId();
    newPlayer.setPos(p.getPos());
    newPlayer.tank = Tank.fromProto(p.getTank());
    newPlayer.setRotation(p.getDirection());
    newPlayer.updateAnimations();
    newPlayer.turretDirection = new Vector2(p.getTurretDirection().getX(), p.getTurretDirection().getY());
    return newPlayer;
  }

  private void setRotation(int direction) {
    hitbox.getRot().z = direction;
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
    newProto.setDirection((int) getRot());
    newProto.setTurretDirection(Point2.newBuilder().setX(turretDirection.x).setY(turretDirection.y).build());
    return newProto.build();
  }

  public void fire(Game game) {
    Vector2 vel = new Vector2(10, 0).setAngle(turretDirection.x - 90);
    System.out.println("Player firing with turretDirection: " + turretDirection + ", turretTarget: " + turretTarget);
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
    turretDirection.x += deltaDir * deltaTime * 180;
    float x = (float) Math.sin((getRot()) / 180.0 * Math.PI);
    float y = (float) Math.cos((getRot()) / 180.0 * Math.PI);
    Vector3 posDelta = new Vector3(x, 0, y).scl(deltaVel * deltaTime * 1.75f); // 1.75 is speed
    pos.add(posDelta);
    updateAnimations();
//    body.setAngularVelocity(new Vector3(0, -deltaDir * 140 * deltaTime, 0));
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

  public void setTurretTarget(float angle, float y) {
    turretTarget = new Vector2(angle % 360, y);
  }

  public void setTurretDirection(Point2 turretDirection) {
    this.turretDirection.set(turretDirection.getX(), turretDirection.getY());
    turretTarget.set(this.turretDirection);
  }

  private void setPos(Point3 pos) {
    this.pos.set(pos.getX(), pos.getY(), pos.getZ());
  }

  public void setPos(Vector3 pos, int direction) {
    this.pos.set(pos);
    setRotation(direction);
  }

  public void setPos(Point3 pos, int direction) {
    setPos(pos);
    setRotation(direction);
  }

  public Hitbox getHitbox() {
    return hitbox;
  }
}
