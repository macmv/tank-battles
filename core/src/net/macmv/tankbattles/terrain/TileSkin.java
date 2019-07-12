package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;

public class TileSkin {

  public Model loadAssets(AssetManager assetManager, Terrain.Type type) {
    return assetManager.get("terrain/" + type.toString().toLowerCase() + "/exported.g3db");
  }

  public void requireAssets(AssetManager assetManager) {
    for (Terrain.Type type : Terrain.Type.values()) {
      String path = "terrain/" + type.toString().toLowerCase();
      assetManager.load(path + "/exported.g3db", Model.class);
    }
  }
}
