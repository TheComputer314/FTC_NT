// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.trobotix.path;

public class TrapezoidProfileUtils {
  private TrapezoidProfileUtils() {}

  static double calculateTotalTime(double vel, double accel, double distance) {
    double timeToFullSpeed = vel / accel;

    // Time it takes to go halfway, assuming constant acceleration over the entire distance
    // d/2 = 1/2 * a * t^2
    // d = a * t^2
    // d/a = t^2
    // t = sqrt(d/a)
    double timeToHalfwayFullAccel = Math.sqrt(distance / accel);

    // If it takes more than half the distance to go to full speed, the profile is triangular not
    // trapezoidal.
    if (timeToHalfwayFullAccel < timeToFullSpeed) {
      return timeToHalfwayFullAccel * 2;
    }

    // Total distance spent accelerating/decelerating
    // 2 * 1/2 * a * t^2
    // a * t^2
    double accelDistance = accel * timeToFullSpeed * timeToFullSpeed;

    // Time spent at full speed
    double fullSpeedTime = (distance - accelDistance) / vel;

    return timeToFullSpeed + fullSpeedTime + timeToFullSpeed;
  }

  /**
   * Recalculates a max velocity, given an acceleration, a distance, and a target time.
   *
   * @param accel The acceleration constraint.
   * @param distance The distance to travel.
   * @param targetTime The desired time to target.
   * @return The velocity that would achieve the desired time to target, within some tolerance.
   */
  static double recalculateMaxVel(double accel, double distance, double targetTime) {
    var time =
        (targetTime * accel - Math.sqrt(accel * (targetTime * targetTime * accel - 4 * distance)))
            / 2;
    if (Double.isNaN(time)) {
      throw new IllegalArgumentException("The target time is too short for the given distance!");
    }
    return time;
  }
}
