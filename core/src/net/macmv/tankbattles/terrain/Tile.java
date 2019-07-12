package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class Tile {
  private ModelInstance inst;
  private final Vector3 pos;
  private final Terrain.Type type;

  public Tile(Vector3 pos, Terrain.Type type) {
    this.pos = pos;
    this.type = type;
  }

  public void render(ModelBatch batch, Environment env) {
    if (inst != null) {
      batch.render(inst, env);
    }
  }

  public void loadAssets(AssetManager assetManager, TileSkin skins) {
    Model model = skins.loadAssets(assetManager, type);
    inst = new ModelInstance(model);
    inst.transform.setTranslation(pos);
  }
}
