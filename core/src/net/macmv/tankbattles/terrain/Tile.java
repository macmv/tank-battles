package net.macmv.tankbattles.terrain;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class Tile {
  private final ModelInstance inst;
  private final Vector3 pos;

  public Tile(Vector3 pos, ModelInstance inst) {
    this.inst = inst;
    if (Math.random() > 0.1) {
      inst.nodes.get(2).parts.get(0).enabled = false;
    }
    this.pos = pos;
  }

  public Tile(Vector3 pos) {
    inst = null;
    this.pos = pos;
  }

  public void render(ModelBatch batch, Environment env) {
    if (inst != null) {
      batch.render(inst, env);
    }
  }
}
