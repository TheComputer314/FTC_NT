// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.trobotix.path;

import ftc.lib.wpilib.math.geometry.Pose2d;
import ftc.lib.wpilib.math.geometry.Translation2d;
import ftc.lib.wpilib.math.kinematics.ChassisSpeeds;

public interface Trajectory {
  Sample sample(double time);

  double getTotalTime();

  record Constraints(
      double maxLinearSpeedMetersPerSec,
      double maxLinearAccelMetersPerSecSquared,
      double maxAngularSpeedRadPerSec,
      double maxAngularAccelRadPerSecSquared) {}

  record Sample(Pose2d pose, ChassisSpeeds speeds) {}

  static LinearTrajectory linear(Pose2d start, Pose2d end, Constraints constraints) {
    return new LinearTrajectory(start, end, constraints);
  }

  static BezierTrajectory bezier(
      Pose2d p0, Translation2d p1, Translation2d p2, Pose2d p3, Constraints constraints) {
    return new BezierTrajectory(p0, p1, p2, p3, constraints);
  }
}
