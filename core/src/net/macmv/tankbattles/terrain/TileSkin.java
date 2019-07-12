package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;

public class TileSkin {

  private HashMap<Terrain.Type, Model> models = new HashMap<>();
  private Model model;

  public Model loadAssets(AssetManager assetManager, Terrain.Type type) {
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
        m.translation.set(Vector3.Zero);
        models.put(type, tmpModel);
      }
    });
    return models.get(type); // if it was not loaded before, that means the model doesn't exist, so it will return null
  }

  public void requireAssets(AssetManager assetManager) {
    assetManager.load("terrain/exported.g3db", Model.class);
  }
}
