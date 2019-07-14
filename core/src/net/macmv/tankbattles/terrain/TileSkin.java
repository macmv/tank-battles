package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import net.macmv.tankbattles.lib.proto.TerrainMap;

import java.util.HashMap;

public class TileSkin {

  private HashMap<TerrainMap.Tile.Type, Model> models = new HashMap<>();
  private Model model;

  public Model loadAssets(AssetManager assetManager, TerrainMap.Tile.Type type) {
    if (model == null) {
      model = assetManager.get("terrain/exported.g3db");
    }
    if (models.get(type) != null) { // will used cached model if it has already been stored
      return models.get(type);
    }
    model.nodes.forEach(m -> {
      if (m.id.equals(type.toString().toLowerCase())) { // will only populate array w/ models it needs
        Model tmpModel = new Model();
        tmpModel.nodes.add(m);
        m.translation.set(0, 0, 0); // cannot use Vector3.Zero for some reason, it messes things up
        models.put(type, tmpModel);
      }
    });
    return models.get(type); // if it was not loaded before, that means the model doesn't exist, so it will return null
  }

  public void requireAssets(AssetManager assetManager) {
    assetManager.load("terrain/exported.g3db", Model.class);
  }
}
