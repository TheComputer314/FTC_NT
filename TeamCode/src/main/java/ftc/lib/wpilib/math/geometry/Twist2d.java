// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.geometry;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import edu.wpi.first.util.struct.StructSerializable;
import ftc.lib.wpilib.math.geometry.struct.Twist2dStruct;
import java.util.Objects;

/**
 * A change in distance along a 2D arc since the last pose update. We can use ideas from
 * differential calculus to create new Pose2d objects from a Twist2d and vice versa.
 *
 * <p>A Twist can be used to represent a difference between two poses.
 */
public class Twist2d implements StructSerializable {
  /** Linear "dx" component. */
  public double dx;

  /** Linear "dy" component. */
  public double dy;

  /** Angular "dtheta" component (radians). */
  public double dtheta;

  /** Default constructor. */
  public Twist2d() {}

  /**
   * Constructs a Twist2d with the given values.
   *
   * @param dx Change in x direction relative to robot.
   * @param dy Change in y direction relative to robot.
   * @param dtheta Change in angle relative to robot.
   */
  public Twist2d(double dx, double dy, double dtheta) {
    this.dx = dx;
    this.dy = dy;
    this.dtheta = dtheta;
  }

  @SuppressLint("DefaultLocale")
  @NonNull
  @Override
  public String toString() {
    return String.format("Twist2d(dX: %.2f, dY: %.2f, dTheta: %.2f)", dx, dy, dtheta);
  }

  /**
   * Checks equality between this Twist2d and another object.
   *
   * @param obj The other object.
   * @return Whether the two objects are equal or not.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Twist2d) {
      return Math.abs(((Twist2d) obj).dx - dx) < 1E-9
          && Math.abs(((Twist2d) obj).dy - dy) < 1E-9
          && Math.abs(((Twist2d) obj).dtheta - dtheta) < 1E-9;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(dx, dy, dtheta);
  }

  /** Twist2d struct for serialization. */
  public static final Twist2dStruct struct = new Twist2dStruct();
}
