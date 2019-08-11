package net.macmv.tankbattles.collision;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.projectile.Projectile;

public class CollisionManager {
  private final btDynamicsWorld world;
  private final btDefaultCollisionConfiguration collisionConfig;
  private final btCollisionDispatcher dispatcher;
  private final btDbvtBroadphase broadphase;
  private final btSequentialImpulseConstraintSolver constraintSolver;
  private final DebugDrawer debugDrawer;

  public CollisionManager() {
    this(true);
  }

  public CollisionManager(boolean loadTextures) {
    Bullet.init();
    collisionConfig = new btDefaultCollisionConfiguration();
    dispatcher = new btCollisionDispatcher(collisionConfig);
    broadphase = new btDbvtBroadphase();
    constraintSolver = new btSequentialImpulseConstraintSolver();
    world = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
    world.setGravity(new Vector3(0, -10f, 0));

    if (loadTextures) {
      debugDrawer = new DebugDrawer();
      debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
      world.setDebugDrawer(debugDrawer);
    } else {
      debugDrawer = null;
    }
  }

  public void update(float deltaTime) {
    world.stepSimulation(deltaTime);
//    System.out.println("Number of objects: " + world.getCollisionObjectArray().size());
  }

  public btRigidBody addObject(Matrix4 transform, float mass, btCollisionShape shape) {
    System.out.println("ADDING OBJECT");
    Vector3 intertia = new Vector3();
    shape.calculateLocalInertia(mass, intertia);
    btRigidBody.btRigidBodyConstructionInfo constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, intertia);
    btRigidBody body = new btRigidBody(constructionInfo);
    world.addRigidBody(body);
    MotionState ms = new MotionState();
    ms.transform = transform;
    body.setMotionState(ms);
    constructionInfo.dispose();
    return body;
  }

  public void debugDrawWorld() {
    if (debugDrawer == null) {
      throw new RuntimeException("Cannot draw debug world without loadTextures set!");
    }
    world.debugDrawWorld();
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, ClosestRayResultCallback callback) {
    world.rayTest(rayFrom, rayTo, callback);
  }

  static public class MotionState extends btMotionState {
    public Matrix4 transform;

    @Override
    public void getWorldTransform(Matrix4 worldTrans) {
      worldTrans.set(transform);
    }

    @Override
    public void setWorldTransform(Matrix4 worldTrans) {
      transform.set(worldTrans);
    }
  }

  public DebugDrawer getDebugDrawer() {
    return debugDrawer;
  }

  public static class OnContact extends ContactListener {
    @Override
    public void onContactStarted(btCollisionObject a, btCollisionObject b) {
      if (a != null && b != null) {
        System.out.println("COLLISION! a: " + a.userData + ", b: " + b.userData);
        Projectile proj;
        if (a.userData instanceof Projectile) {
          proj = (Projectile) a.userData;
        } else if (b.userData instanceof Projectile) {
          proj = (Projectile) b.userData;
        } else {
          return;
        }
        if (a.userData instanceof Player) {
          proj.doDamage((Player) a.userData);
        } else if (b.userData instanceof Player) {
          proj.doDamage((Player) b.userData);
        }
        proj.getGame().destroyProjectile(proj);
      }
    }
  }
}
