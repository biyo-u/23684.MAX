package org.firstinspires.ftc.teamcode.OpModes.Auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.EMLite.Air_Traffic_Control;
import org.firstinspires.ftc.teamcode.Utilities.Constants;
import org.firstinspires.ftc.teamcode.EMLite.Migration.WingMove;
import org.firstinspires.ftc.teamcode.Utilities.Robot;
import org.firstinspires.ftc.teamcode.Subsystems.GoBildaPinpointDriver;
import org.firstinspires.ftc.teamcode.Utilities.Old.Position;
import org.firstinspires.ftc.teamcode.Utilities.Old.Rotation;
import org.firstinspires.ftc.teamcode.Utilities.Old.Distance;

import org.firstinspires.ftc.teamcode.EMLite.Migration.EagleGPS;

@Disabled
@Autonomous(name = "MOVE DIRECTLY TO OBSERVATION ZONE", group = Constants.GroupNames.Autonomous, preselectTeleOp = "TeleOp")
public class EmergencyLastResortAuto extends OpMode {
    private Robot robot; // imports robot hardwareMap class
    private GoBildaPinpointDriver odometry; // imports robot odometry class
    private WingMove wingmove; // imports EagleMatrix Lite movement class for drivetrain
    private EagleGPS eagleGPS; // imports EagleMatrix Lite GPS class
    public Air_Traffic_Control coordinates;
    public Position migration = new Position(new Distance(0, DistanceUnit.INCH), new Distance(20, DistanceUnit.INCH), new Rotation(180, AngleUnit.DEGREES)); // target position
    int counter; // counter to ensure AUTO program is active and running loops
    public boolean GoalMet = false; // checks to see if goal (zetaTranslation) has been reached

    // lines 31 to 36 create all the numerical variables that will be used to compare due to the Double.compare function not working with the direct doubles
    double zetaY = 0; // robot's current X position cast to a double
    double zetaY2 = 0; // robot's target X position cast to a double
    double zetaX = 0; // robot's current Y position cast to a double
    double zetaX2 = 0; // robot's target Y position cast to a double
    double zetaHeading = 0; // robot's current heading cast to a double
    double zetaHeading2 = 0; // robot's target heading cast to a double

    // lines 45-47 create all the string variables that will be used to a) create the different values for each switch line of code, and keep track of AUTO as it runs
    String actionCounter = "MOVE TO OBSERVATION ZONE"; // robot's current action, String (text) value for switch block
    String autoStage = null; // robot's current action, String (text) value for telemetry
    String autoStatus = null; // robot's current action status, String (text) value for telemetry

    @Override
    public void init() {
        // lines 48-54 initialise all information related to the odometry pods so when in use will properly function and not print out NullPointerExceptions.
        this.odometry = hardwareMap.get(GoBildaPinpointDriver.class, "odometry");
        this.odometry.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        this.odometry.setOffsets(-6.44, 6.8745);
        this.odometry.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD, GoBildaPinpointDriver.EncoderDirection.FORWARD);
        this.odometry.resetPosAndIMU();
        this.odometry.setPosition(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0));

        // lines 56-59 initialise all other hardware imports and the movement imports so they are not rendered as "null", which when it happens creates the fatal NullPointerException error.
        this.robot = new Robot(hardwareMap, telemetry);
        this.wingmove = new WingMove(robot);
        this.eagleGPS = new EagleGPS(robot, wingmove, odometry, coordinates);
        this.migration = new Position(new Distance(0, DistanceUnit.INCH), new Distance(20, DistanceUnit.INCH), new Rotation(0, AngleUnit.DEGREES));

        GoalMet = false; // resets goals boolean to false
        telemetry.addData("Hardware Status", "initialised"); // prints on driver station that all hardware is initialised

        zetaY = -odometry.getPosY(); // gets zetaPrime's current Y
        zetaY2 = migration.getY(); // sets zetaPrime's target Y
        zetaX = odometry.getPosX(); // get zetaPrime's current X
        zetaX2 = migration.getX(); // sets zetaPrime's target X
        zetaHeading = odometry.getHeading(); // gets zetaPrime's current heading
        zetaHeading2 = migration.getHeading(); // sets zetaPrime's target heading
    }

    @Override
    public void loop() {
        odometry.update();

        Pose2D zetaPosition = odometry.getPosition(); // creates Pose2D of the robot's position
        zetaY = zetaPosition.getY(DistanceUnit.INCH); // gets zetaPrime's current Y
        zetaX = zetaPosition.getX(DistanceUnit.INCH); // get zetaPrime's current X
        zetaHeading = zetaPosition.getHeading(AngleUnit.DEGREES); // gets zetaPrime's current heading

        switch (actionCounter) {

            case "MOVE TO OBSERVATION ZONE": // MOVES ROBOT TO OBSERVATION ZONE FROM SUBMERSIBLE POSITION
                eagleGPS.last_resort();
                break;

            case "EXEUNT": // ENDS THE SWITCH STATEMENT ONCE ALL CASES ARE COMPLETED
                eagleGPS.exeunt();
                break;

            default: // IF actionCounter IS NOT DEFINED OR DEFINED TO A UNDEFINED STATE, PRINT ERROR
                eagleGPS.error();
        }

        counter++; // update counter for ever loop
        odometry.update(); // updates odometry every loop

        telemetry.addLine("ZETA PRIME LOCATIONS"); // heading title
        telemetry.addData("CurrentY (Forward, Backward)", zetaY); // robot's current Y position
        telemetry.addData("CurrentX (Left, Right)", zetaX); // robot's current X position
        telemetry.addData("CurrentHeading (Rotation)", zetaHeading); // robot's current heading

        telemetry.addLine("\n" + "AUTO DIAGNOSTICS"); // heading title
        telemetry.addData("COUNT", counter); // counter
        telemetry.addData("AUTO STAGE", autoStage); // stage auto is in
        telemetry.addData("AUTO STATUS", autoStatus); // status action is in
        telemetry.addLine("EagleMatrix 0.2.9."); // library version title
        counter++; // update counter for ever loop
        odometry.update(); // updates odometry every loop
        telemetry.update(); // updates telemetry every loop
    }
}