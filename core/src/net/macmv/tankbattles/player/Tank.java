package net.macmv.tankbattles.player;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import net.macmv.tankbattles.render.Skin;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Tank {

  public final boolean useTexture;
  private Weapon primary;
  private Weapon secondary;
  private Base base;
  private Skin skin;
  private ModelInstance model;
  private ArrayList<AnimationController> rightTread = new ArrayList<>();
  private ArrayList<AnimationController> leftTread = new ArrayList<>();

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

  public void render(ModelBatch batch, Environment env, float delta) {
    rightTread.forEach(a -> a.update(delta));
    leftTread.forEach(a -> a.update(delta));
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
    newTank.base = Base.fromProto(p.getBase());
    return newTank;
  }

  public net.macmv.tankbattles.lib.proto.Tank toProto() {
    net.macmv.tankbattles.lib.proto.Tank.Builder newProto = net.macmv.tankbattles.lib.proto.Tank.newBuilder();
    newProto.setPrimary(primary.toProto());
    newProto.setBase(base.toProto());
    return newProto.build();
  }

  public void requireAssets(AssetManager assetManager) {
    skin.requireAssets(assetManager);
  }

  public void changeAnimations(float right, float left) {
    leftTread.forEach(a -> {
      a.setAnimation(a.current.animation.id, -1, right, null);
    });
    rightTread.forEach(a -> {
      a.setAnimation(a.current.animation.id, -1, left, null);
    });
  }

  public void loadAssets(AssetManager assetManager) {
    skin.loadAssets(assetManager);
    if (useTexture) {
      model = new ModelInstance(skin.getModel());

      Pattern right = Pattern.compile("Right Tread\\..{3}");
      Pattern left = Pattern.compile("Left Tread\\..{3}");

      model.nodes.forEach(n -> {
        if (right.matcher(n.id).matches()) {
          AnimationController a = new AnimationController(model);
          rightTread.add(a);
          a.setAnimation(n.id + "|Move", 0, null);
        } else if (left.matcher(n.id).matches()) {
          AnimationController a = new AnimationController(model);
          leftTread.add(a);
          a.setAnimation(n.id + "|Move", 0, null);
        }
      });
    }
  }
}
