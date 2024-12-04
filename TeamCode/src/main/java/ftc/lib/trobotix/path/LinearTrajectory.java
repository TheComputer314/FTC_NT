// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.trobotix.path;

import static ftc.lib.trobotix.path.TrapezoidProfileUtils.calculateTotalTime;
import static ftc.lib.trobotix.path.TrapezoidProfileUtils.recalculateMaxVel;

import ftc.lib.wpilib.math.geometry.Pose2d;
import ftc.lib.wpilib.math.geometry.Rotation2d;
import ftc.lib.wpilib.math.kinematics.ChassisSpeeds;
import ftc.lib.wpilib.math.trajectory.TrapezoidProfile;

public class LinearTrajectory implements Trajectory {
  private final Pose2d startPose;
  private final Pose2d endPose;

  private final TrapezoidProfile driveProfile;
  private final TrapezoidProfile.State startDriveState;
  private final TrapezoidProfile.State endDriveState;
  private final double totalDistance;
  private final TrapezoidProfile yawProfile;
  private final TrapezoidProfile.State startYawState;
  private final TrapezoidProfile.State endYawState;

  private final double totalTime;

  private final double sin;
  private final double cos;

  LinearTrajectory(Pose2d startPose, Pose2d endPose, Constraints constraints) {
    this.startPose = startPose;
    this.endPose = endPose;

    totalDistance = endPose.getTranslation().getDistance(startPose.getTranslation());
    double totalRotation =
        endPose.getRotation().getRadians() - startPose.getRotation().getRadians();
    var driveTime =
        calculateTotalTime(
            constraints.maxLinearSpeedMetersPerSec(),
            constraints.maxLinearAccelMetersPerSecSquared(),
            totalDistance);
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
              constraints.maxLinearAccelMetersPerSecSquared(), totalDistance, yawTime);
      totalTime = yawTime;
    }

    driveProfile =
        new TrapezoidProfile(
            new TrapezoidProfile.Constraints(
                driveVel, constraints.maxLinearAccelMetersPerSecSquared()));
    startDriveState = new TrapezoidProfile.State();
    endDriveState = new TrapezoidProfile.State(totalDistance, 0);

    yawProfile =
        new TrapezoidProfile(
            new TrapezoidProfile.Constraints(
                yawVel, constraints.maxAngularAccelRadPerSecSquared()));
    startYawState = new TrapezoidProfile.State(startPose.getRotation().getRadians(), 0);
    endYawState = new TrapezoidProfile.State(endPose.getRotation().getRadians(), 0);

    var deltaPosAngle = endPose.getTranslation().minus(startPose.getTranslation()).getAngle();
    sin = deltaPosAngle.getSin();
    cos = deltaPosAngle.getCos();
  }

  @Override
  public Sample sample(double time) {
    var translationalState = driveProfile.calculate(time, startDriveState, endDriveState);
    var yawState = yawProfile.calculate(time, startYawState, endYawState);

    var translationalPosition =
        startPose
            .getTranslation()
            .interpolate(endPose.getTranslation(), translationalState.position / totalDistance);

    return new Sample(
        new Pose2d(translationalPosition, new Rotation2d(yawState.position)),
        new ChassisSpeeds(
            translationalState.velocity * cos,
            translationalState.velocity * sin,
            yawState.velocity));
  }

  @Override
  public double getTotalTime() {
    return totalTime;
  }
}
