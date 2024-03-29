package net.macmv.tankbattles.projectile;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.collision.Hitbox;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.Point3;
import net.macmv.tankbattles.player.Player;

public class Projectile {
  public final int id;
  private final boolean useTextures;
  private final Hitbox hitbox;
  private Vector3 pos = new Vector3();
  private Vector3 vel = new Vector3();
  private ModelInstance modelInst;
  private static Model model;
  private final Game game;
  private Vector3 prevPos;

  public Projectile(Vector3 pos, Vector3 vel, int id, Game game) {
    this(pos, vel, id, game, true);
  }

  public Projectile(Vector3 pos, Vector3 vel, int id, Game game, boolean useTextures) {
    if (useTextures) {
      modelInst = new ModelInstance(model);
    }
    this.useTextures = useTextures;
    this.pos.set(pos);
    this.vel.set(vel);
    this.id = id;
    this.game = game;
    hitbox = new Hitbox(pos, new Vector3(0.2f, 0.2f, 0.2f));
    hitbox.setVelocity(vel);
    game.getCollisionManager().add(hitbox);
  }

  public Projectile(Vector3 pos, Vector3 vel, int id) {
    this.pos.set(pos);
    this.vel.set(vel);
    this.id = id;
    game = null;
    useTextures = false;
    hitbox = null;
  }

  public static Projectile fromProto(net.macmv.tankbattles.lib.proto.Projectile proj, Game game) {
    Vector3 pos = new Vector3(proj.getPos().getX(), proj.getPos().getY(), proj.getPos().getZ());
    Vector3 vel = new Vector3(proj.getVel().getX(), proj.getVel().getY(), proj.getVel().getZ());
    return new Projectile(pos, vel, proj.getId(), game);
  }

  public net.macmv.tankbattles.lib.proto.Projectile toProto() {
    net.macmv.tankbattles.lib.proto.Projectile.Builder newProj = net.macmv.tankbattles.lib.proto.Projectile.newBuilder();
    newProj.setPos(Point3.newBuilder().setX(pos.x).setY(pos.y).setZ(pos.z).build());
    newProj.setVel(Point3.newBuilder().setX(vel.x).setY(vel.y).setZ(vel.z).build());
    newProj.setId(id);
    return newProj.build();
  }

  public void update() {
    if (useTextures && modelInst != null) {
      modelInst.transform.setToRotation(Vector3.Y, new Vector2(prevPos.x, prevPos.z).sub(new Vector2(pos.x, pos.z)).angle() * -1);
      modelInst.transform.setTranslation(pos);
    }
    prevPos = pos.cpy();
  }

  public void render(ModelBatch batch, Environment env) {
    if (modelInst != null) {
      batch.render(modelInst, env);
    }
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

  public void destroy() {
    System.out.println("MURDER");
  }

  public void doDamage(Player player) {

  }

  public Hitbox getHitbox() {
    return hitbox;
  }

  public Game getGame() {
    return game;
  }
}
