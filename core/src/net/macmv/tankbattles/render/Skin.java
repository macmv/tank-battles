package net.macmv.tankbattles.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.UBJsonReader;
import net.macmv.tankbattles.lib.proto.Tank;

import java.util.HashMap;

public class Skin {

  private static Skin defaultSkin;
  private final Model model;
  private static ModelBuilder builder = new ModelBuilder();
  private static G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
  private static HashMap<String, Skin> skins = new HashMap<>();

  private Skin(String name) {
    model = loader.loadModel(Gdx.files.internal("skins/" + name + "/meshes.g3db"));
//    turretModel = loader.loadModel(Gdx.files.internal("skins/" + name + "/turret.g3db"));
//    turretModel = builder.createBox(1, 1, 1,
//            new Material(ColorAttribute.createDiffuse(0, 1, 0, 1)),
//            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
  }

//  public Model getBaseModel() {
//    return baseModel;
//  }

  public Model getModel() {
    return model;
  }

  public static Skin fromProto(Tank.Skin proto) {
    String name = proto.getName();
    if (!skins.containsKey(name)) {
      skins.put(name, new Skin(name));
    }
    return skins.get(name);
  }

  public static Skin getDefault() {
    String name = "default";
    if (!skins.containsKey(name)) {
      skins.put(name, new Skin(name));
    }
    return skins.get(name);
  }
}
