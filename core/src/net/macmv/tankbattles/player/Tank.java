package net.macmv.tankbattles.player;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import net.macmv.tankbattles.render.Skin;

public class Tank {

  public final boolean useTexture;
  private Weapon primary;
  private Weapon secondary;
  private Base base;
  private Skin skin;
  private ModelInstance model;

  public Tank(Skin skin) {
    this(skin, true);
  }

  private Tank(Skin skin, boolean useTexture) {
    this.useTexture = useTexture;
    primary = new Weapon();
    secondary = new Weapon();
    base = new Base();
    this.skin = skin;
  }

  public void render(ModelBatch batch, Environment env) {
    batch.render(model, env);
  }

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

  public void requireAssets(AssetManager assetManager) {
    skin.requireAssets(assetManager);
  }

  public void loadAssets(AssetManager assetManager) {
    skin.loadAssets(assetManager);
    if (useTexture) {
      model = new ModelInstance(skin.getModel());
    }
  }
}
