package ftc.lib.wpilib.math.geometry.struct;

import java.nio.ByteBuffer;

import edu.wpi.first.util.struct.Struct;
import ftc.lib.wpilib.math.geometry.Twist2d;

public class Twist2dStruct implements Struct<Twist2d> {
  @Override
  public Class<Twist2d> getTypeClass() {
    return Twist2d.class;
  }

  @Override
  public String getTypeName() {
    return "Twist2d";
  }

  @Override
  public int getSize() {
    return kSizeDouble * 3;
  }

  @Override
  public String getSchema() {
    return "double dx;double dy;double dtheta";
  }

  @Override
  public Twist2d unpack(ByteBuffer bb) {
    double dx = bb.getDouble();
    double dy = bb.getDouble();
    double dtheta = bb.getDouble();
    return new Twist2d(dx, dy, dtheta);
  }

  @Override
  public void pack(ByteBuffer bb, Twist2d value) {
    bb.putDouble(value.dx);
    bb.putDouble(value.dy);
    bb.putDouble(value.dtheta);
  }
}
