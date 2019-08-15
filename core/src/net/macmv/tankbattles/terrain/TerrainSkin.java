package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import java.util.HashMap;

public class TerrainSkin {

  private HashMap<TerrainSkin.Type, Material> materials = new HashMap<>();

  public Material getMaterial(Type type) {
    System.out.println(materials);
    return materials.get(type);
  }

  public void loadAssets(AssetManager assetManager) {
//    if (model == null) {
//      model = assetManager.get("terrain/exported.g3db");
//    }
    materials.put(Type.GRASS, new Material(ColorAttribute.createDiffuse(Type.GRASS.getColor()), ColorAttribute.createSpecular(1, 1, 1, 1)));
    materials.put(Type.SAND, new Material(ColorAttribute.createDiffuse(Type.SAND.getColor())));
    materials.put(Type.ROCK, new Material(ColorAttribute.createDiffuse(Type.ROCK.getColor())));
  }

  public void requireAssets(AssetManager assetManager) {
//    assetManager.load("terrain/exported.g3db", Model.class);
  }

  public enum Type {
    GRASS, SAND, ROCK;

    public Color getColor() {
      Color col = new Color();
      switch (this) {
        case GRASS: col.set(0.1f, 0.8f, 0, 1); break;
        case SAND: col.set(1, 1, 0, 1); break;
        case ROCK: col.set(.5f, .5f, .5f, 1); break;
      }
      return col;
    }
  }
}
