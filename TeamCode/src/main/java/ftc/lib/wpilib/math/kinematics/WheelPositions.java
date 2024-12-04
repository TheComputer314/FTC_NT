// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.math.kinematics;

import ftc.lib.wpilib.math.interpolation.Interpolatable;

/**
 * Interface for wheel positions.
 *
 * @param <T> Wheel positions type.
 */
public interface WheelPositions<T extends WheelPositions<T>> extends Interpolatable<T> {
  /**
   * Returns a copy of this instance.
   *
   * @return A copy.
   */
  T copy();
}
