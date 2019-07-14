package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.Point3;
import net.macmv.tankbattles.lib.proto.TerrainMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    tileSkin = new TileSkin();
    Vector3 dimensions = loadMap(filename);
    width = (int) dimensions.x;
    height = (int) dimensions.y;
    length = (int) dimensions.z;
  }

  public Terrain(Game game, TerrainMap proto) {
    this(game, proto, true);
  }

  public Terrain(Game game, TerrainMap proto, boolean useTextures) {
    this.useTextures = useTextures;
    this.game = game;
    env = new Environment();
    env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1, 0));
    env.add(new DirectionalLight().set(1, 1, 1, -0.5f, -0.8f, -0.2f));
    tileSkin = new TileSkin();
    width = proto.getWidth();
    height = proto.getHeight();
    length = proto.getLength();
    loadTiles(proto);
  }

  private void loadTiles(TerrainMap proto) {
    proto.getPlanesMap().forEach((y, plane) -> {
      plane.getRowsMap().forEach((x, row) -> {
        row.getTilesMap().forEach((z, tile) -> {
          addTile(new Vector3(x, y, z), tile.getType());
        });
      });
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
          Vector3 pos = new Vector3(x, y, z);
          Tile tile = tiles.get(pos);
          if (tile == null) {
            throw new RuntimeException("Tile at: " + pos + " is null!");
          }
          newTile.setType(tile.getType());
          newTile.setPos(Point3.newBuilder().setX(x).setY(y).setZ(z).build());
          newRow.putTiles(z, newTile.build());
        }
        newPlane.putRows(x, newRow.build());
      }
      newMap.putPlanes(y, newPlane.build());
    }
    return newMap.build();
  }

  // only server should run this, server sends client serialized map
  // returns width, height, length of map
  private Vector3 loadMap(String filename) {
    File file = Gdx.files.internal(filename).file();
    if (!file.exists()) {
      throw new RuntimeException("File '" + file.getAbsolutePath() + "' does not exist");
    }
    TerrainMap map;
    try {
      map = TerrainMap.parseFrom(new FileInputStream(file));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not load map at " + filename);
    }
    loadTiles(map);
    return new Vector3(map.getWidth(), map.getHeight(), map.getLength());
  }

  // for map editing
  public void addTile(Vector3 pos, TerrainMap.Tile.Type type) {
    Matrix4 trans = new Matrix4();
    trans.setTranslation(pos);
    game.getCollisionManager().addObject(trans, 0, new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f)));
    tiles.put(pos.cpy(), new Tile(pos.cpy(), type));
  }

  public void save(String filename, boolean overwrite) {
    File file = Gdx.files.internal(filename).file();
    if (!file.isFile()) {
      throw new RuntimeException("Location '" + filename + "' is not a file");
    }
    if (file.exists() && !overwrite) {
      throw new RuntimeException("File '" + filename + "' already exists, and overwrite was false");
    }
    TerrainMap map = toProto();
    try {
      map.writeTo(new FileOutputStream(file));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not save map at " + filename);
    }
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
}
