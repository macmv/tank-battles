package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.TerrainMap;

import java.util.HashMap;

public class Terrain {
  private final Game game;
  private final boolean useTextures;
  private final HashMap<Vector3, Tile> tiles = new HashMap<>();
  private final int width;
  private final int height;
  private final int length;
  private final Environment env;
  private TileSkin tileSkin;

  public Terrain(Game game, String filename, boolean useTextures) {
    this.useTextures = useTextures;
    this.game = game;
    env = new Environment();
    env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1, 0));
    env.add(new DirectionalLight().set(1, 1, 1, -0.5f, -0.8f, -0.2f));
    Vector3 dimensions = loadMap(filename);
    width = (int) dimensions.x;
    height = (int) dimensions.y;
    length = (int) dimensions.z;
    tileSkin = new TileSkin();
  }

  public Terrain(Game game, String filename) {
    this(game, filename, true);
  }

  private Vector3 loadMap(String filename) {
    // TODO: load map from file
    for (int y = 0; y < 10; y++) {
      for (int x = 0; x < 10; x++) {
        Matrix4 trans = new Matrix4();
        trans.setTranslation(x, 0, y);
        game.getCollisionManager().addObject(trans, 0, new btBoxShape(new Vector3(0.5f, 0.01f, 0.5f)));
        tiles.put(new Vector3(x, 0, y), new Tile(new Vector3(x, 0, y), TerrainMap.Tile.Type.GRASS));
      }
    }
    return new Vector3(10, 1, 10);
  }

  public void render(ModelBatch batch) {
    tiles.forEach((p, t) -> {
      t.render(batch, env);
    });
  }

  public Environment getEnvironment() {
    return env;
  }

  public void requireAssets(AssetManager assetManager) {
    tileSkin.requireAssets(assetManager);
  }

  public void loadAssets(AssetManager assetManager) {
    tiles.forEach((p, t) -> {
      t.loadAssets(assetManager, tileSkin);
    });
  }

  public TerrainMap toProto() {
    TerrainMap.Builder newMap = TerrainMap.newBuilder();
    newMap.setWidth(width);
    newMap.setHeight(height);
    newMap.setLength(length);
    for (int y = 0; y < height; y++) {
      TerrainMap.Plane.Builder newPlane = TerrainMap.Plane.newBuilder();
      for (int x = 0; x < width; x++) {
        TerrainMap.Plane.Row.Builder newRow = TerrainMap.Plane.Row.newBuilder();
        for (int z = 0; z < length; z++) {
          TerrainMap.Tile.Builder newTile = TerrainMap.Tile.newBuilder();
          // TODO: set tile stof
          newRow.putTiles(z, newTile.build());
        }
        newPlane.putRows(x, newRow.build());
      }
      newMap.putPlanes(y, newPlane.build());
    }
    return newMap.build();
  }
}
