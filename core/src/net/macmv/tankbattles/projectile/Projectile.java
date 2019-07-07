package net.macmv.tankbattles.projectile;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.lib.proto.Point3;

public class Projectile {
  public final int id;
  private Vector3 pos = new Vector3();
  private Vector3 vel = new Vector3();
  private ModelInstance modelInst;
  private static Model model;

  public Projectile() {
    modelInst = new ModelInstance(model);
    id = (int) (Math.random() * Integer.MAX_VALUE);
  }

  public Projectile(Vector3 pos, Vector3 vel, int id) {
    this(pos, vel, id, true);
  }

  public Projectile(Vector3 pos, Vector3 vel, int id, boolean useTextures) {
    if (useTextures) {
      modelInst = new ModelInstance(model);
    }
    this.pos.set(pos);
    this.vel.set(vel);
    this.id = id;
  }

  public static Projectile fromProto(net.macmv.tankbattles.lib.proto.Projectile proj) {
    Projectile newProj = new Projectile();
    newProj.pos.set(proj.getPos().getX(), proj.getPos().getY(), proj.getPos().getZ());
    newProj.vel.set(proj.getVel().getX(), proj.getVel().getY(), proj.getVel().getZ());
    return newProj;
  }

  public net.macmv.tankbattles.lib.proto.Projectile toProto() {
    net.macmv.tankbattles.lib.proto.Projectile.Builder newProj = net.macmv.tankbattles.lib.proto.Projectile.newBuilder();
    newProj.setPos(Point3.newBuilder().setX(pos.x).setY(pos.y).setZ(pos.z).build());
    newProj.setVel(Point3.newBuilder().setX(vel.x).setY(vel.y).setZ(vel.z).build());
    return newProj.build();
  }

  public void update(float delta) {
    Vector3 oldPos = pos.cpy();
    pos.add(vel.x * delta, vel.y * delta, vel.z * delta);
    modelInst.transform.setToRotation(Vector3.Y, new Vector2(oldPos.x, oldPos.z).sub(new Vector2(pos.x, pos.z)).angle() * -1);
    modelInst.transform.setTranslation(pos);
  }

  public void render(ModelBatch batch, Environment env) {
    batch.render(modelInst, env);
  }

  public static void requireAssets(AssetManager assetManager) {
    assetManager.load("projectiles/projectiles.g3db", Model.class);
  }

  public static void loadAssets(AssetManager assetManager) {
    model = assetManager.get("projectiles/projectiles.g3db");
  }

  public void updatePos(Point3 pos, Point3 vel) {
    this.pos.set(pos.getX(), pos.getY(), pos.getZ());
    this.vel.set(vel.getX(), vel.getY(), vel.getZ());
  }

  public Vector3 getVel() {
    return vel;
  }

  public Vector3 getPos() {
    return pos;
  }
}
