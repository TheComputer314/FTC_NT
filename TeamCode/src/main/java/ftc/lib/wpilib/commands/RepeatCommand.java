// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.commands;

import static ftc.lib.wpilib.util.ErrorMessages.requireNonNullParam;

/**
 * A command that runs another command repeatedly, restarting it when it ends, until this command is
 * interrupted. Command instances that are passed to it cannot be added to any other groups, or
 * scheduled individually.
 *
 * <p>The rules for command compositions apply: command instances that are passed to it cannot be
 * added to any other composition or scheduled individually,and the composition requires all
 * subsystems its components require.
 *
 * <p>This class is provided by the NewCommands VendorDep
 */
public class RepeatCommand extends Command {
  private final Command m_command;
  private boolean m_ended;

  /**
   * Creates a new RepeatCommand. Will run another command repeatedly, restarting it whenever it
   * ends, until this command is interrupted.
   *
   * @param command the command to run repeatedly
   */
  @SuppressWarnings("this-escape")
  public RepeatCommand(Command command) {
    m_command = requireNonNullParam(command, "command", "RepeatCommand");
    CommandScheduler.getInstance().registerComposedCommands(command);
    m_requirements.addAll(command.getRequirements());
    setName("Repeat(" + command.getName() + ")");
  }

  @Override
  public void initialize() {
    m_ended = false;
    m_command.initialize();
  }

  @Override
  public void execute() {
    if (m_ended) {
      m_ended = false;
      m_command.initialize();
    }
    m_command.execute();
    if (m_command.isFinished()) {
      // restart command
      m_command.end(false);
      m_ended = true;
    }
  }

  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public void end(boolean interrupted) {
    // Make sure we didn't already call end() (which would happen if the command finished in the
    // last call to our execute())
    if (!m_ended) {
      m_command.end(interrupted);
      m_ended = true;
    }
  }

  @Override
  public boolean runsWhenDisabled() {
    return m_command.runsWhenDisabled();
  }

  @Override
  public InterruptionBehavior getInterruptionBehavior() {
    return m_command.getInterruptionBehavior();
  }
}
