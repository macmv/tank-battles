package net.macmv.tankbattles.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.utils.UBJsonReader;
import net.macmv.tankbattles.lib.proto.Tank;

import java.util.HashMap;

public class Skin {

  private final String name;
  private Model model;
  private static G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
  private static HashMap<String, Skin> skins = new HashMap<>();

  private Skin(String name) {
    this.name = name;
  }

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

  public void requireAssets(AssetManager assetManager) {
    assetManager.load("skins/" + name + "/meshes.g3db", Model.class);
  }

  public void loadAssets(AssetManager assetManager) {
    model = assetManager.get("skins/" + name + "/meshes.g3db");
  }
}
