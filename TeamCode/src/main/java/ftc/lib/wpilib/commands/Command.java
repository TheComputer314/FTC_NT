// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.wpilib.commands;

import static ftc.lib.wpilib.util.ErrorMessages.requireNonNullParam;

import ftc.lib.trobotix.BooleanConsumer;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * A state machine representing a complete action to be performed by the robot. Commands are run by
 * the {@link CommandScheduler}, and can be composed into CommandGroups to allow users to build
 * complicated multistep actions without the need to roll the state machine logic themselves.
 *
 * <p>Commands are run synchronously from the main robot loop; no multithreading is used, unless
 * specified explicitly from the command implementation.
 *
 * <p>This class is provided by the NewCommands VendorDep
 */
public abstract class Command {
  /** Requirements set. */
  protected Set<Subsystem> m_requirements = new HashSet<>();

  /** Default constructor. */
  @SuppressWarnings("this-escape")
  protected Command() {
    String name = getClass().getName();
  }

  /** The initial subroutine of a command. Called once when the command is initially scheduled. */
  public void initialize() {}

  /** The main body of a command. Called repeatedly while the command is scheduled. */
  public void execute() {}

  /**
   * The action to take when the command ends. Called when either the command finishes normally, or
   * when it interrupted/canceled.
   *
   * <p>Do not schedule commands here that share requirements with this command. Use {@link
   * #andThen(Command...)} instead.
   *
   * @param interrupted whether the command was interrupted/canceled
   */
  public void end(boolean interrupted) {}

  /**
   * Whether the command has finished. Once a command finishes, the scheduler will call its end()
   * method and un-schedule it.
   *
   * @return whether the command has finished.
   */
  public boolean isFinished() {
    return false;
  }

  /**
   * Specifies the set of subsystems used by this command. Two commands cannot use the same
   * subsystem at the same time. If another command is scheduled that shares a requirement, {@link
   * #getInterruptionBehavior()} will be checked and followed. If no subsystems are required, return
   * an empty set.
   *
   * <p>Note: it is recommended that user implementations contain the requirements as a field, and
   * return that field here, rather than allocating a new set every time this is called.
   *
   * @return the set of subsystems that are required
   * @see InterruptionBehavior
   */
  public Set<Subsystem> getRequirements() {
    return m_requirements;
  }

  /**
   * Adds the specified subsystems to the requirements of the command. The scheduler will prevent
   * two commands that require the same subsystem from being scheduled simultaneously.
   *
   * <p>Note that the scheduler determines the requirements of a command when it is scheduled, so
   * this method should normally be called from the command's constructor.
   *
   * @param requirements the requirements to add
   */
  public final void addRequirements(Subsystem... requirements) {
    for (Subsystem requirement : requirements) {
      m_requirements.add(requireNonNullParam(requirement, "requirement", "addRequirements"));
    }
  }

  private String m_name = "";

  /**
   * Gets the name of this Command.
   *
   * <p>By default, the simple class name is used. This can be changed with {@link
   * #setName(String)}.
   *
   * @return The display name of the Command
   */
  public String getName() {
    return m_name;
  }

  /**
   * Sets the name of this Command.
   *
   * @param name The display name of the Command.
   */
  public void setName(String name) {
    m_name = name;
  }

  /**
   * Decorates this command with a timeout. If the specified timeout is exceeded before the command
   * finishes normally, the command will be interrupted and un-scheduled.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param seconds the timeout duration
   * @return the command with the timeout added
   */
  public ParallelRaceGroup withTimeout(double seconds) {
    return raceWith(new WaitCommand(seconds));
  }

  /**
   * Decorates this command with an interrupt condition. If the specified condition becomes true
   * before the command finishes normally, the command will be interrupted and un-scheduled.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param condition the interrupt condition
   * @return the command with the interrupt condition added
   * @see #onlyWhile(BooleanSupplier)
   */
  public ParallelRaceGroup until(BooleanSupplier condition) {
    return raceWith(new WaitUntilCommand(condition));
  }

  /**
   * Decorates this command with a run condition. If the specified condition becomes false before
   * the command finishes normally, the command will be interrupted and un-scheduled.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param condition the run condition
   * @return the command with the run condition added
   * @see #until(BooleanSupplier)
   */
  public ParallelRaceGroup onlyWhile(BooleanSupplier condition) {
    return until(() -> !condition.getAsBoolean());
  }

  /**
   * Decorates this command with a runnable to run before this command starts.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param toRun the Runnable to run
   * @param requirements the required subsystems
   * @return the decorated command
   */
  public SequentialCommandGroup beforeStarting(Runnable toRun, Subsystem... requirements) {
    return beforeStarting(new InstantCommand(toRun, requirements));
  }

  /**
   * Decorates this command with another command to run before this command starts.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param before the command to run before this one
   * @return the decorated command
   */
  public SequentialCommandGroup beforeStarting(Command before) {
    return new SequentialCommandGroup(before, this);
  }

  /**
   * Decorates this command with a runnable to run after the command finishes.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param toRun the Runnable to run
   * @param requirements the required subsystems
   * @return the decorated command
   */
  public SequentialCommandGroup andThen(Runnable toRun, Subsystem... requirements) {
    return andThen(new InstantCommand(toRun, requirements));
  }

  /**
   * Decorates this command with a set of commands to run after it in sequence. Often more
   * convenient/less-verbose than constructing a new {@link SequentialCommandGroup} explicitly.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param next the commands to run next
   * @return the decorated command
   */
  public SequentialCommandGroup andThen(Command... next) {
    SequentialCommandGroup group = new SequentialCommandGroup(this);
    group.addCommands(next);
    return group;
  }

  /**
   * Decorates this command with a set of commands to run parallel to it, ending when the calling
   * command ends and interrupting all the others. Often more convenient/less-verbose than
   * constructing a new {@link ParallelDeadlineGroup} explicitly.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param parallel the commands to run in parallel
   * @return the decorated command
   */
  public ParallelDeadlineGroup deadlineWith(Command... parallel) {
    return new ParallelDeadlineGroup(this, parallel);
  }

  /**
   * Decorates this command with a set of commands to run parallel to it, ending when the last
   * command ends. Often more convenient/less-verbose than constructing a new {@link
   * ParallelCommandGroup} explicitly.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param parallel the commands to run in parallel
   * @return the decorated command
   */
  public ParallelCommandGroup alongWith(Command... parallel) {
    ParallelCommandGroup group = new ParallelCommandGroup(this);
    group.addCommands(parallel);
    return group;
  }

  /**
   * Decorates this command with a set of commands to run parallel to it, ending when the first
   * command ends. Often more convenient/less-verbose than constructing a new {@link
   * ParallelRaceGroup} explicitly.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param parallel the commands to run in parallel
   * @return the decorated command
   */
  public ParallelRaceGroup raceWith(Command... parallel) {
    ParallelRaceGroup group = new ParallelRaceGroup(this);
    group.addCommands(parallel);
    return group;
  }

  /**
   * Decorates this command to run repeatedly, restarting it when it ends, until this command is
   * interrupted. The decorated command can still be canceled.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @return the decorated command
   */
  public RepeatCommand repeatedly() {
    return new RepeatCommand(this);
  }

  /**
   * Decorates this command to run "by proxy" by wrapping it in a {@link ProxyCommand}. This is
   * useful for "forking off" from command compositions when the user does not wish to extend the
   * command's requirements to the entire command composition.
   *
   * @return the decorated command
   */
  public ProxyCommand asProxy() {
    return new ProxyCommand(this);
  }

  /**
   * Decorates this command to only run if this condition is not met. If the command is already
   * running and the condition changes to true, the command will not stop running. The requirements
   * of this command will be kept for the new conditional command.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param condition the condition that will prevent the command from running
   * @return the decorated command
   * @see #onlyIf(BooleanSupplier)
   */
  public ConditionalCommand unless(BooleanSupplier condition) {
    return new ConditionalCommand(new InstantCommand(), this, condition);
  }

  /**
   * Decorates this command to only run if this condition is met. If the command is already running
   * and the condition changes to false, the command will not stop running. The requirements of this
   * command will be kept for the new conditional command.
   *
   * <p>Note: This decorator works by adding this command to a composition. The command the
   * decorator was called on cannot be scheduled independently or be added to a different
   * composition (namely, decorators), unless it is manually cleared from the list of composed
   * commands with {@link CommandScheduler#removeComposedCommand(Command)}. The command composition
   * returned from this method can be further decorated without issue.
   *
   * @param condition the condition that will allow the command to run
   * @return the decorated command
   * @see #unless(BooleanSupplier)
   */
  public ConditionalCommand onlyIf(BooleanSupplier condition) {
    return unless(() -> !condition.getAsBoolean());
  }

  /**
   * Decorates this command to have a different {@link InterruptionBehavior interruption behavior}.
   *
   * @param interruptBehavior the desired interrupt behavior
   * @return the decorated command
   */
  public WrapperCommand withInterruptBehavior(InterruptionBehavior interruptBehavior) {
    return new WrapperCommand(this) {
      @Override
      public InterruptionBehavior getInterruptionBehavior() {
        return interruptBehavior;
      }
    };
  }

  /**
   * Decorates this command with a lambda to call on interrupt or end, following the command's
   * inherent {@link #end(boolean)} method.
   *
   * @param end a lambda accepting a boolean parameter specifying whether the command was
   *     interrupted.
   * @return the decorated command
   */
  public WrapperCommand finallyDo(BooleanConsumer end) {
    requireNonNullParam(end, "end", "Command.finallyDo()");
    return new WrapperCommand(this) {
      @Override
      public void end(boolean interrupted) {
        super.end(interrupted);
        end.accept(interrupted);
      }
    };
  }

  /**
   * Decorates this command with a lambda to call on interrupt or end, following the command's
   * inherent {@link #end(boolean)} method. The provided lambda will run identically in both
   * interrupt and end cases.
   *
   * @param end a lambda to run when the command ends, whether or not it was interrupted.
   * @return the decorated command
   */
  public WrapperCommand finallyDo(Runnable end) {
    return finallyDo(interrupted -> end.run());
  }

  /**
   * Decorates this command with a lambda to call on interrupt, following the command's inherent
   * {@link #end(boolean)} method.
   *
   * @param handler a lambda to run when the command is interrupted
   * @return the decorated command
   */
  public WrapperCommand handleInterrupt(Runnable handler) {
    requireNonNullParam(handler, "handler", "Command.handleInterrupt()");
    return finallyDo(
        interrupted -> {
          if (interrupted) {
            handler.run();
          }
        });
  }

  /** Schedules this command. */
  public void schedule() {
    CommandScheduler.getInstance().schedule(this);
  }

  /**
   * Cancels this command. Will call {@link #end(boolean) end(true)}. Commands will be canceled
   * regardless of {@link InterruptionBehavior interruption behavior}.
   *
   * @see CommandScheduler#cancel(Command...)
   */
  public void cancel() {
    CommandScheduler.getInstance().cancel(this);
  }

  /**
   * Whether the command is currently scheduled. Note that this does not detect whether the command
   * is in a composition, only whether it is directly being run by the scheduler.
   *
   * @return Whether the command is scheduled.
   */
  public boolean isScheduled() {
    return CommandScheduler.getInstance().isScheduled(this);
  }

  /**
   * Whether the command requires a given subsystem.
   *
   * @param requirement the subsystem to inquire about
   * @return whether the subsystem is required
   */
  public boolean hasRequirement(Subsystem requirement) {
    return getRequirements().contains(requirement);
  }

  /**
   * How the command behaves when another command with a shared requirement is scheduled.
   *
   * @return a variant of {@link InterruptionBehavior}, defaulting to {@link
   *     InterruptionBehavior#kCancelSelf kCancelSelf}.
   */
  public InterruptionBehavior getInterruptionBehavior() {
    return InterruptionBehavior.kCancelSelf;
  }

  /**
   * Whether the given command should run when the robot is disabled. Override to return true if the
   * command should run when disabled.
   *
   * @return whether the command should run when the robot is disabled
   */
  public boolean runsWhenDisabled() {
    return false;
  }

  /**
   * Decorates this Command with a name.
   *
   * @param name name
   * @return the decorated Command
   */
  public WrapperCommand withName(String name) {
    WrapperCommand wrapper = new WrapperCommand(Command.this) {};
    wrapper.setName(name);
    return wrapper;
  }

  /**
   * An enum describing the command's behavior when another command with a shared requirement is
   * scheduled.
   */
  public enum InterruptionBehavior {
    /**
     * This command ends, {@link #end(boolean) end(true)} is called, and the incoming command is
     * scheduled normally.
     *
     * <p>This is the default behavior.
     */
    kCancelSelf,
    /** This command continues, and the incoming command is not scheduled. */
    kCancelIncoming
  }
}
