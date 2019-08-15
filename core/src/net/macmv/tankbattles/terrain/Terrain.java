package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.TerrainMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class Terrain {
  private final Game game;
  private final boolean useTextures;
  private final HashMap<Vector2, Float> meshPoints = new HashMap<>();
  private final int width;
  private final int length;
  private final Environment env;
  private final TerrainSkin terrainSkin;
  private ModelInstance terrainModel;

  public Terrain(Game game, String filename, boolean useTextures) {
    this(game, getMap(filename), useTextures);
  }

  public Terrain(Game game, TerrainMap proto) {
    this(game, proto, true);
  }

  private Terrain(Game game, TerrainMap proto, boolean useTextures) {
    this.useTextures = useTextures;
    this.game = game;
    width = proto.getWidth();
    length = proto.getLength();
    loadMesh(proto);
    if (useTextures) {
      terrainSkin = new TerrainSkin();
      env = new Environment();
      env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1, 0));
      env.add(new DirectionalLight().set(1, 1, 1, -0.5f, -0.8f, -0.2f));
      env.add(new PointLight().set(1, 1, 1, 0, 5, 0, 20));
    } else {
      env = null;
      terrainSkin = null;
    }
  }

  public Terrain(Game game, String filename) {
    this(game, filename, true);
  }

  // only server should run this, server sends client serialized map
  // returns map protobuf
  private static TerrainMap getMap(String filename) {
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
    return map;
  }

  private void loadMesh(TerrainMap proto) {
    proto.getRowsMap().forEach((x, row) -> {
      row.getPointsMap().forEach((z, point) -> {
        meshPoints.put(new Vector2(x, z), point.getPos().getY());
      });
    });
  }

  public TerrainMap toProto() {
//    TerrainMap.Builder newMap = TerrainMap.newBuilder();
//    newMap.setWidth(width);
//    newMap.setHeight(height);
//    newMap.setLength(length);
//    for (int y = 0; y < height; y++) {
//      TerrainMap.Plane.Builder newPlane = TerrainMap.Plane.newBuilder();
//      for (int x = 0; x < width; x++) {
//        TerrainMap.Plane.Row.Builder newRow = TerrainMap.Plane.Row.newBuilder();
//        for (int z = 0; z < length; z++) {
//          TerrainMap.Tile.Builder newTile = TerrainMap.Tile.newBuilder();
//          Vector3 pos = new Vector3(x, y, z);
//          Tile tile = tiles.get(pos);
//          if (tile == null) {
//            throw new RuntimeException("Tile at: " + pos + " is null!");
//          }
//          newTile.setType(tile.getType());
//          newTile.setPos(Point3.newBuilder().setX(x).setY(y).setZ(z).build());
//          newRow.putTiles(z, newTile.build());
//        }
//        newPlane.putRows(x, newRow.build());
//      }
//      newMap.putPlanes(y, newPlane.build());
//    }
//    return newMap.build();
    return null;
  }

  public void save(String filename, boolean overwrite) {
    File file = Gdx.files.internal(filename).file();
    if (!file.isFile()) {
      throw new RuntimeException("Location '" + file.getAbsolutePath() + "' is not a file");
    }
    if (file.exists() && !overwrite) {
      throw new RuntimeException("'" + file.getAbsolutePath() + "' already exists, and overwrite is false");
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
    batch.render(terrainModel, env);
  }

  public Environment getEnvironment() {
    return env;
  }

  public void requireAssets(AssetManager assetManager) {
    terrainSkin.requireAssets(assetManager);
  }

  public void loadAssets(AssetManager assetManager) {
    terrainSkin.loadAssets(assetManager);
    updateMesh();
  }

  public TerrainSkin getTerrainSkin() {
    return terrainSkin;
  }

  public void setHeight(Vector2 pos, float height) {
    if (meshPoints.containsKey(pos)) {
      meshPoints.put(pos, height);
      updateMesh();
    }
  }

  private void updateMesh() {
    System.out.println("Updating mesh");
    Color col = ((ColorAttribute) terrainSkin.getMaterial(TerrainSkin.Type.GRASS).get(ColorAttribute.Diffuse)).color;
    float[] verts = new float[(width - 1) * (length - 1) * 9 * 4];
    short[] indicies = new short[(width - 1) * (length - 1) * 6];
    short vertexIndex = 0;
    short indexIndex = 0;
    for (int z = 0; z < length - 1; z++) { // the -1 makes sure it doesn't create quads on the bottom or right side of the map
      for (int x = 0; x < width - 1; x++) {
        Vector3 pos = new Vector3(x, meshPoints.get(new Vector2(x, z)), z);
        Vector3 pos_horz_offset = new Vector3(x + 1, meshPoints.get(new Vector2(x + 1, z)), z);
        Vector3 pos_vert_offset = new Vector3(x, meshPoints.get(new Vector2(x, z + 1)), z + 1);
        Vector3 norm = pos_horz_offset.sub(pos).crs(pos_vert_offset.sub(pos));
        for (int i = 0; i < 4; i++) {
          int horz_offset = i % 2;
          int vert_offset = i / 2;
          // position data
          verts[vertexIndex * 9 + 0] = x + horz_offset;
          verts[vertexIndex * 9 + 1] = meshPoints.get(new Vector2(x + horz_offset, z + vert_offset));
          verts[vertexIndex * 9 + 2] = z + vert_offset;
          // color data
          verts[vertexIndex * 9 + 3] = col.r;
          verts[vertexIndex * 9 + 4] = col.g;
          verts[vertexIndex * 9 + 5] = col.b;
          // normal data
          verts[vertexIndex * 9 + 6] = -norm.x;
          verts[vertexIndex * 9 + 7] = -norm.y;
          verts[vertexIndex * 9 + 8] = -norm.z;
          vertexIndex++;
        }
        // 0 1
        // 2 3
        // -4 -3
        // -2 -1
        indicies[indexIndex * 6 + 0] = (short) (vertexIndex - 2);
        indicies[indexIndex * 6 + 1] = (short) (vertexIndex - 3);
        indicies[indexIndex * 6 + 2] = (short) (vertexIndex - 4);
        indicies[indexIndex * 6 + 3] = (short) (vertexIndex - 2);
        indicies[indexIndex * 6 + 4] = (short) (vertexIndex - 1);
        indicies[indexIndex * 6 + 5] = (short) (vertexIndex - 3);
        indexIndex++;
      }
    }
    Mesh mesh = new Mesh(true, verts.length, indicies.length,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 3, ShaderProgram.COLOR_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE));
    mesh.setVertices(verts);
    mesh.setIndices(indicies);
    ModelBuilder mb = new ModelBuilder();
    mb.begin();
    mb.part("terrain", mesh, GL20.GL_TRIANGLES, new Material(new ColorAttribute(ColorAttribute.Specular, 1, 1, 1, 1)));
    terrainModel = new ModelInstance(mb.end());
    System.out.println("Generated mesh: " + terrainModel);
  }
}
