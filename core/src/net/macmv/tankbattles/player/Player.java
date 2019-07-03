package net.macmv.tankbattles.player;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.lib.proto.Point;
import net.macmv.tankbattles.render.Skin;

public class Player {

  private int id;
  private Vector2 pos;
  private Tank tank;
  private int direction; // in degrees

  public Player(int id, net.macmv.tankbattles.lib.proto.Tank tank) {
    this.id = id;
    this.tank = Tank.fromProto(tank, true);
    pos = new Vector2();
  }


  public Player(int id, net.macmv.tankbattles.lib.proto.Tank tank, boolean loadTexture) {
    this.id = id;
    System.out.println(loadTexture);
    this.tank = Tank.fromProto(tank, loadTexture);
    pos = new Vector2();
  }

  public Player() {
    id = (int) (Math.random() * Integer.MAX_VALUE);
    tank = new Tank(Skin.getDefault());
    pos = new Vector2();
  }

  public void updatePos(Point pos) {
    updatePos(new Vector2(pos.getX(), pos.getY()));
  }

  public void updatePos(Vector2 pos) {
    this.pos.set(pos);
    if (tank.useTexture && tank.getModel() != null) {
      tank.getModel().transform.setToRotation(Vector3.Y, -direction + 180);
      tank.getModel().transform.setTranslation(pos.x, 0, pos.y);
    }
  }

  public static Player fromProto(net.macmv.tankbattles.lib.proto.Player p) {
    Player newPlayer = new Player();
    newPlayer.id = p.getId();
    newPlayer.pos = new Vector2(p.getPos().getX(), p.getPos().getY());
    newPlayer.tank = Tank.fromProto(p.getTank());
    newPlayer.direction = p.getDirection();
    newPlayer.updatePos(newPlayer.pos);
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
    newProto.setPos(Point.newBuilder().setX(pos.x).setY(pos.y).build());
    newProto.setTank(tank.toProto());
    newProto.setDirection(direction);
    return newProto.build();
  }

  public void move(Vector2 d, float deltaTime) {
    // d.y is forward/back, d.x is turn right/left
    direction += d.x * deltaTime * 180; // 180 per second, should feel snappy
    float x = (float) Math.cos((direction + 90) / 180.0 * Math.PI);
    float y = (float) Math.sin((direction + 90) / 180.0 * Math.PI);
    pos.add(new Vector2(x, y).scl(d.y * deltaTime)); // 1 is speed
    updatePos(pos);
  }

  public Vector2 getPos() {
    return pos;
  }

  public void loadAssets(AssetManager assetManager) {
    tank.loadAssets(assetManager);
  }

  public void finishLoading(AssetManager assetManager) {
    tank.finishLoading(assetManager);
  }
}
