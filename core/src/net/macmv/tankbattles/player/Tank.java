package net.macmv.tankbattles.player;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.render.Skin;

public class Tank {


  public final boolean useTexture;
  private Weapon primary;
  private Weapon secondary;
  private Base base;
  private Skin skin;
  private ModelInstance model;
  private int rotation = 0;
  private int rotationTarget = 0;
  private Vector3 tmp = new Vector3();

  public Tank(Skin skin) {
    this(skin, true);
  }

  private Tank(Skin skin, boolean useTexture) {
    this.useTexture = useTexture;
    primary = new Weapon();
    secondary = new Weapon();
    base = new Base();
    this.skin = skin;
    if (useTexture) {
      model = new ModelInstance(skin.getModel());
    }
  }

  public void rotate(int degrees) {
    rotationTarget = degrees % 360;
  }

  public void render(ModelBatch batch, float delta) {
    if (rotation != rotationTarget) {
      float direction;
      int d = rotation - rotationTarget;
      System.out.println("Rotating to " + rotation + ", target: " + rotationTarget + ", diff: " + d);
      if (d > 0) {
        direction = -1;
      } else {
        direction = 1;
      }
      if (rotation > 360 || rotation < -360) {
        rotation = rotation % 360;
      }
      rotation += direction * delta * 180; // rotate 180 per second
      model.transform.getTranslation(tmp);
      model.transform.setToRotation(Vector3.Y, rotation);
      model.transform.setTranslation(tmp);
      model.calculateTransforms();
    }
    batch.render(model);
  }

//  public ModelInstance getBaseModel() {
//    return baseModel;
//  }

  public ModelInstance getModel() {
    return model;
  }

  public static Tank fromProto(net.macmv.tankbattles.lib.proto.Tank p) {
    return fromProto(p, true);
  }

  public static Tank fromProto(net.macmv.tankbattles.lib.proto.Tank p, boolean useTexture) {
    Tank newTank;
    if (useTexture) {
      newTank = new Tank(Skin.getDefault(), useTexture); // TODO: replace w/ Skin.fromProto(p.getSkin()));
    } else {
      newTank = new Tank(null, useTexture);
    }
    newTank.primary = Weapon.fromProto(p.getPrimary());
    newTank.secondary = Weapon.fromProto(p.getSecondary());
    newTank.base = Base.fromProto(p.getBase());
    return newTank;
  }

  public net.macmv.tankbattles.lib.proto.Tank toProto() {
    net.macmv.tankbattles.lib.proto.Tank.Builder newProto = net.macmv.tankbattles.lib.proto.Tank.newBuilder();
    newProto.setPrimary(primary.toProto());
    newProto.setSecondary(secondary.toProto());
    newProto.setBase(base.toProto());
    return newProto.build();
  }
}
