package net.macmv.tankbattles.mapeditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import net.macmv.tankbattles.collision.CollisionManager;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.TerrainMap;
import net.macmv.tankbattles.render.Render;
import net.macmv.tankbattles.terrain.TileSkin;

public class MapEditor {

  private final Render render;
  private final Game game;
  private final Vector3 viewTarget = new Vector3();
  private final Vector3 tileTarget = new Vector3();
  private ModelInstance model;
  private final ClosestRayResultCallback callback = new ClosestRayResultCallback(new Vector3(), new Vector3());
  private boolean visible = false;


  public MapEditor(Render render, Game game) {
    this.render = render;
    this.game = game;
  }

  public void updateTarget(CollisionManager collisions) {
    Vector3 camPos = render.getCamera().position;
    Vector3 unprojected = render.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    unprojected.sub(camPos).nor().scl(20).add(camPos);

    callback.setCollisionObject(null); // reset values from last call
    callback.setClosestHitFraction(1f);
    callback.setRayFromWorld(camPos);
    callback.setRayToWorld(unprojected);

    collisions.rayTest(camPos,
            unprojected,
            callback);

    if (callback.hasHit()) {
      callback.getHitPointWorld(viewTarget);
      Vector3 nor = new Vector3();
      callback.getHitNormalWorld(nor);
      if (Math.abs(nor.x) > 0.5) {
        viewTarget.y = (int) viewTarget.y;
        viewTarget.z = (int) viewTarget.z;
      } else if (Math.abs(nor.y) > 0.5) {
        viewTarget.x = (int) viewTarget.x;
        viewTarget.z = (int) viewTarget.z;
      } else if (Math.abs(nor.z) > 0.5) {
        viewTarget.x = (int) viewTarget.x;
        viewTarget.y = (int) viewTarget.y;
      }
      tileTarget.set(viewTarget);
      tileTarget.add(nor.scl(0.5f));
      tileTarget.x = Math.round(tileTarget.x);
      tileTarget.y = Math.round(tileTarget.y);
      tileTarget.z = Math.round(tileTarget.z);
      visible = true;
    } else {
      visible = false;
    }

    model.transform.setTranslation(tileTarget);
  }

  public void render(ModelBatch batch, Environment env) {
    if (visible) {
      batch.render(model, env);
    }
  }

  public void loadAssets(AssetManager assetManager, TileSkin skins) {
    Model m = skins.loadAssets(assetManager, TerrainMap.Tile.Type.SAND);
    model = new ModelInstance(m);
  }

  public void placeTile() {
    game.getTerrain().addTile(render.getAssetManager(), tileTarget, TerrainMap.Tile.Type.ROCK);
  }
}
