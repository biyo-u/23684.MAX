package org.firstinspires.ftc.teamcode.EagleMatrix.PIDF.TEST;


import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Config
@TeleOp
public class PidArmTest extends OpMode {
    private PIDController controller;

    public static double p = 0.09 , i = 0.001 , d = 0.001;
    public static double f = 0;
    public static int target = 700;

    public final double ticksInDegree = 700 / 180.0;
    DcMotor shoulder;


    public void init(){

        controller = new PIDController(p,i,d);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        shoulder = hardwareMap.get(DcMotor.class,"shoulder");
        shoulder.setDirection(DcMotorSimple.Direction.REVERSE);
  }

    public void loop(){

        controller.setPID(p,i,d);

        int armPosition = shoulder.getCurrentPosition();

        double pid = controller.calculate(armPosition,target);
        double ff = Math.cos(Math.toRadians(target / ticksInDegree)) * f;

        double power = pid + ff;

        shoulder.setPower(power);

        telemetry.addData("pos ",armPosition);
        telemetry.addData("target ",target);
        telemetry.update();
    }
}
