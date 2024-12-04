// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.trobotix.path;

import static ftc.lib.trobotix.path.TrapezoidProfileUtils.calculateTotalTime;
import static ftc.lib.trobotix.path.TrapezoidProfileUtils.recalculateMaxVel;

import ftc.lib.wpilib.math.geometry.Pose2d;
import ftc.lib.wpilib.math.geometry.Rotation2d;
import ftc.lib.wpilib.math.geometry.Translation2d;
import ftc.lib.wpilib.math.interpolation.InterpolatingDoubleTreeMap;
import ftc.lib.wpilib.math.kinematics.ChassisSpeeds;
import ftc.lib.wpilib.math.trajectory.TrapezoidProfile;

public class BezierTrajectory implements Trajectory {
  // Cubic coefficients for position
  private final double x_a, x_b, x_c, x_d;
  private final double y_a, y_b, y_c, y_d;

  // The relationship between a bezier curve's t value and the distance traveled on the curve isn't
  // linear, so a lerp table is used to map values
  private final InterpolatingDoubleTreeMap tLookupTable = new InterpolatingDoubleTreeMap();

  private final TrapezoidProfile driveProfile;
  private final TrapezoidProfile.State startDriveState;
  private final TrapezoidProfile.State endDriveState;
  private final TrapezoidProfile yawProfile;
  private final TrapezoidProfile.State startYawState;
  private final TrapezoidProfile.State endYawState;

  private final double totalTime;

  BezierTrajectory(
      Pose2d p0, Translation2d p1, Translation2d p2, Pose2d p3, Constraints constraints) {
    // From Freya Holmér's "Continuity of Splines"
    // https://youtu.be/jvPPXbo87ds
    // p_t = a + bt + ct^2 + dt^3
    this.x_a = p0.getX();
    this.x_b = -3 * p0.getX() + 3 * p1.getX();
    this.x_c = 3 * p0.getX() - 6 * p1.getX() + 3 * p2.getX();
    this.x_d = -p0.getX() + 3 * p1.getX() - 3 * p2.getX() + p3.getX();

    this.y_a = p0.getY();
    this.y_b = -3 * p0.getY() + 3 * p1.getY();
    this.y_c = 3 * p0.getY() - 6 * p1.getY() + 3 * p2.getY();
    this.y_d = -p0.getY() + 3 * p1.getY() - 3 * p2.getY() + p3.getY();

    // Splits the curve into several segments by sampling the curve at various points and sums the
    // length of each segment. It is a very simple and easy way of approximating the curve's arc
    // length that converges very quickly, stabilizing within 1 cm within 20-25 iterations for even
    // the wackiest of paths.
    // The iteration count is higher than 25, as the sampled points are also placed in a lookup
    // table to map arc length values to 0-1 t values for the purposes of following the path, and we
    // want that to be fairly accurate.
    tLookupTable.put(0.0, 0.0);
    double arcLengthMeters = 0;
    int iterationCount = 50;
    var segmentStartPoint = p0.getTranslation();
    for (int i = 1; i <= iterationCount; i++) {
      double t = (double) i / iterationCount;
      var segmentEndPoint = sampleCurve(t);
      arcLengthMeters += segmentEndPoint.getDistance(segmentStartPoint);
      tLookupTable.put(arcLengthMeters, t);
      segmentStartPoint = segmentEndPoint;
    }

    double totalRotation = p3.getRotation().getRadians() - p0.getRotation().getRadians();
    var driveTime =
        calculateTotalTime(
            constraints.maxLinearSpeedMetersPerSec(),
            constraints.maxLinearAccelMetersPerSecSquared(),
            arcLengthMeters);
    var yawTime =
        calculateTotalTime(
            constraints.maxAngularSpeedRadPerSec(),
            constraints.maxAngularAccelRadPerSecSquared(),
            Math.abs(totalRotation));

    double driveVel;
    double yawVel;
    if (driveTime > yawTime) {
      driveVel = constraints.maxLinearSpeedMetersPerSec();
      yawVel =
          recalculateMaxVel(
              constraints.maxAngularAccelRadPerSecSquared(), Math.abs(totalRotation), driveTime);
      totalTime = driveTime;
    } else {
      yawVel = constraints.maxAngularSpeedRadPerSec();
      driveVel =
          recalculateMaxVel(
              constraints.maxLinearAccelMetersPerSecSquared(), arcLengthMeters, yawTime);
      totalTime = yawTime;
    }

    driveProfile =
        new TrapezoidProfile(
            new TrapezoidProfile.Constraints(
                driveVel, constraints.maxLinearAccelMetersPerSecSquared()));
    startDriveState = new TrapezoidProfile.State();
    endDriveState = new TrapezoidProfile.State(arcLengthMeters, 0);

    yawProfile =
        new TrapezoidProfile(
            new TrapezoidProfile.Constraints(
                yawVel, constraints.maxAngularAccelRadPerSecSquared()));
    startYawState = new TrapezoidProfile.State(p0.getRotation().getRadians(), 0);
    endYawState = new TrapezoidProfile.State(p3.getRotation().getRadians(), 0);
  }

  @Override
  public Sample sample(double time) {
    var driveState = driveProfile.calculate(time, startDriveState, endDriveState);
    var yawState = yawProfile.calculate(time, startYawState, endYawState);

    var t = tLookupTable.get(driveState.position);
    var translation = sampleCurve(t);
    var tangent = sampleTangentVector(t);

    return new Sample(
        new Pose2d(translation, new Rotation2d(yawState.position)),
        new ChassisSpeeds(
            tangent.getX() * driveState.velocity,
            tangent.getY() * driveState.velocity,
            yawState.velocity));
  }

  @Override
  public double getTotalTime() {
    return totalTime;
  }

  private Translation2d sampleCurve(double t) {
    if (t < 0 || t > 1) {
      throw new IllegalArgumentException("t was outside [0, 1]! Got " + t + "instead!");
    }
    // p_t = a + bt + ct^2 + dt^3
    return new Translation2d(
        x_a + (x_b * t) + (x_c * t * t) + (x_d * t * t * t),
        y_a + (y_b * t) + (y_c * t * t) + (y_d * t * t * t));
  }

  private Translation2d sampleTangentVector(double t) {
    if (t < 0 || t > 1) {
      throw new IllegalArgumentException("t was outside [0, 1]! Got " + t + "instead!");
    }
    // Derivative of above
    // v_t = b + 2ct + 3dt^2
    var velocityVector =
        new Translation2d(
            x_b + (2 * x_c * t) + (3 * x_d * t * t), y_b + (2 * y_c * t) + (3 * y_d * t * t));
    // Normalize
    return velocityVector.div(velocityVector.getNorm());
  }
}
