package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.utils.UBJsonReader;
import net.macmv.tankbattles.Game;

import java.util.HashMap;

public class Terrain {
  private final Game game;
  private final Type type;
  private Model model;
  private static G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
  private HashMap<Vector2, ModelInstance> tiles = new HashMap<>();
  private final Environment env;

  public Terrain(Game game, Type type) {
    this.type = type;
    this.game = game;
    env = new Environment();
    env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1, 0));
    env.add(new DirectionalLight().set(1, 1, 1, -0.5f, -0.8f, -0.2f));
  }

  public void render(ModelBatch batch) {
    tiles.forEach((p, t) -> {
      batch.render(t, env);
    });
  }

  public Environment getEnvironment() {
    return env;
  }

  public void requireAssets(AssetManager assetManager) {
    String path = "terrain/" + type.toString().toLowerCase();
    assetManager.load(path + "/exported.g3db", Model.class);
  }

  public void loadAssets(AssetManager assetManager) {
    String path = "terrain/" + type.toString().toLowerCase();
    model = assetManager.get(path + "/exported.g3db");
    for (int y = 0; y < 10; y++) {
      for (int x = 0; x < 10; x++) {
        ModelInstance inst = new ModelInstance(model);
        Matrix4 trans = new Matrix4();
        trans.setTranslation(x, 0, y);
        game.getCollisionManager().addObject(trans,0, new btBoxShape(new Vector3(0.5f, 0.01f, 0.5f)));
        inst.transform.setTranslation(x, 0, y);
        if (Math.random() > 0.1) {
          inst.nodes.get(2).parts.get(0).enabled = false;
        }
        tiles.put(new Vector2(x, y), inst);
      }
    }
  }

  public enum Type {
    GRASS, SAND, ROCK
  }
}
