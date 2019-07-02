package net.macmv.tankbattles.player;

import com.badlogic.gdx.math.Vector2;
import net.macmv.tankbattles.lib.proto.Point;
import net.macmv.tankbattles.render.Skin;

public class Player {

  private int id;
  private Vector2 pos;
  private Tank tank;

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
    if (tank.useTexture) {
      tank.getModel().transform.setTranslation(pos.x, 0, pos.y);
//      tank.getTurretModel().transform.setTranslation(pos.x, 0, pos.y);
    }
  }

  public static Player fromProto(net.macmv.tankbattles.lib.proto.Player p) {
    Player newPlayer = new Player();
    newPlayer.id = p.getId();
    newPlayer.pos = new Vector2(p.getPos().getX(), p.getPos().getY());
    newPlayer.tank = Tank.fromProto(p.getTank());
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
    return newProto.build();
  }

  public void move(Vector2 d, float deltaTime) {
    tank.rotate((int) d.angle());
    updatePos(d.scl(deltaTime).add(pos));
  }

  public Vector2 getPos() {
    return pos;
  }
}
