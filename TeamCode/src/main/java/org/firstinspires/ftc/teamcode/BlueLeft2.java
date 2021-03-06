/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

/**
 * This file illustrates the concept of driving a path based on encoder counts.
 * It uses the common Pushbot hardware class to define the drive on the robot.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: PushbotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forwards, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backwards for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This methods assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name="Pushbot: Blue Left 2", group="Pushbot")
//@Disabled
public class BlueLeft2 extends LinearOpMode {

    /* Declare OpMode members. */
    MyHardwarePushbot         robot   = new MyHardwarePushbot();   // Use a Pushbot's hardware
    private ElapsedTime     runtime = new ElapsedTime();

    static final double     COUNTS_PER_MOTOR_REV    = 835;    // eg: Neverest 40
    static final double     DRIVE_GEAR_REDUCTION    = 2.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                                                      (WHEEL_DIAMETER_INCHES * 3.1415);
    int             target = 0;
    int             maxlift = 7100;                     // maxiumum lift height
    double          clawOffset; // starts claw closed on block
    static final double     DRIVE_SPEED             = 0.6;
    static final double     TURN_SPEED              = 0.25;
    double          ballArmUp = .7; // makes ball Arm a variable
    double          ballArmDown = 0.05;

    NormalizedColorSensor colorSensor; //This line creates a NormalizedColorSensor variable called colorSensor
    VuforiaLocalizer vuforia;

    @Override
    public void runOpMode() {

        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         * set up Vuforis
         */
        robot.init(hardwareMap);
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = "AQnPJDP/////AAAAmUS73qR6gk0zhRdrQV8z5sQcsIZOLNFEeBu51S0z1IDJ2mjzjQakuSkKjPPN5P0OU1xLzIllx8BNZ1+sUF0TMno1opOTWRq7LHOx1FwfiYL9OWCB+nti7esVQTjWHAat6af1DAX0wpzvL+YSjcTptliDr1r4mQ5jZvuypJDv2F/o0F2sGVAM0GZNczjmp3mconmIdNFt/ZkuhtEmqSYNEiRO5vp049huTdMQ+wF63ZO9naWQgNf/sD4u/6YydHzKNGg9BneTYjWrdXnfLMrUlPCFzVZ+WOWh6HnbV8OxA+AEMGSJCe9dZwkVouBgfAMXmTLkWxerr195jyo/pVPAZ2euUnqVOCbZwKS0S/jdZapO";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);
        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary
        relicTrackables.activate();


        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Resetting Encoders");
        telemetry.update();

        // setup DC motors
        robot.leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Send telemetry message to indicate successful Encoder reset
        telemetry.addData("Path0",  "Starting at %7d :%7d",
                          robot.leftDrive.getCurrentPosition(),
                          robot.rightDrive.getCurrentPosition());
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color"); // Uses the phone to find the color sensor named sensor_color

        robot.ballArm.setPosition(ballArmDown);// lowers ballArm all the way down

        // grab and slightly lift glyph
        clawOffset =robot.CLOSE_offset;
        clawOffset = Range.clip(clawOffset, -0.5, 0.5);
        robot.leftClaw.setPosition(robot.MID_SERVO + clawOffset + robot.leftclawcorrection);
        robot.leftClawup.setPosition(robot.MID_SERVO - clawOffset + robot.leftclawupcorrection);
        robot.rightClaw.setPosition(robot.MID_SERVO - clawOffset + robot.rightclawcorrection);
        robot.rightClawup.setPosition(robot.MID_SERVO + clawOffset + robot.rightclawupcorrection);
        runtime.reset();
        while (runtime.seconds() < .4) {    //wait for claw to finsh open or close
        }
        target = robot.lift.getCurrentPosition() + 650;
        robot.lift.setTargetPosition(target);
        robot.lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.lift.setPower(0.6);
        while (robot.lift.isBusy() && (robot.lift.getCurrentPosition() < maxlift)) {}   //wait for lift to stop
        robot.lift.setPower(0.0);


        NormalizedRGBA colors = colorSensor.getNormalizedColors(); // reads color sensor and puts it in the variable colors

        if (colors.blue < colors.red){ //It checks if the ball is blue

            encoderDrive(TURN_SPEED, 2, -2,5 ); // turns right to knock blue ball
            robot.ballArm.setPosition(ballArmUp);
            encoderDrive(TURN_SPEED, -2, 2,5 );

        }
        else  { //The ball facing the color sensor is red
            encoderDrive(TURN_SPEED, -2, 2, 5 ); //turns left knocking the blue ball and turning to face glyph box
            robot.ballArm.setPosition(ballArmUp);
            encoderDrive(TURN_SPEED,  2, -2,.5 );
        }

        //turn toward and view pictograph
        encoderDrive(TURN_SPEED, 2.25, -2.25,5 );
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
        runtime.reset();
        do {
            vuMark = RelicRecoveryVuMark.from(relicTemplate);
            telemetry.addData("VuMark", "%s visible", vuMark);
            telemetry.update();
        }while (runtime.seconds() < 1 );   //   vuMark ==  RelicRecoveryVuMark.UNKNOWN
        telemetry.addData("VuMark", "%s visible", vuMark);
        telemetry.update();
        encoderDrive(TURN_SPEED, -2.25, 2.25,5 );

        // drive to cryptobox
        encoderDrive(DRIVE_SPEED, -32.5, -32.5, 5); // Drive off balance board

        // turn toward right, center,or left column depending on pictograph
        if (vuMark == RelicRecoveryVuMark.RIGHT || vuMark == RelicRecoveryVuMark.UNKNOWN) {
            telemetry.addData("VuMark", "%s visible", vuMark);
            telemetry.update();
            encoderDrive(TURN_SPEED, 13, -13, 5);
        }
        else if (vuMark == RelicRecoveryVuMark.CENTER) {
            telemetry.addData("VuMark", "%s visible", vuMark);
            telemetry.update();
            encoderDrive(TURN_SPEED, 14, -14, 5);
        }
        else if (vuMark == RelicRecoveryVuMark.LEFT) {
            telemetry.addData("VuMark", "%s visible", vuMark);
            telemetry.update();
            encoderDrive(TURN_SPEED, 15, -15, 5);
        }

        encoderDrive(DRIVE_SPEED, -40, -40, 5);

        //drop glyph and lower lift
        clawOffset = robot.OPEN_offset;
        robot.leftClaw.setPosition(robot.MID_SERVO + clawOffset + robot.leftclawcorrection);
        robot.leftClawup.setPosition(robot.MID_SERVO - clawOffset + robot.leftclawupcorrection);
        robot.rightClaw.setPosition(robot.MID_SERVO - clawOffset + robot.rightclawcorrection);
        robot.rightClawup.setPosition(robot.MID_SERVO + clawOffset + robot.rightclawupcorrection);
        runtime.reset();
        while (runtime.seconds() < .4) {    //wait for claw to finsh open or close
        }
        robot.lift.setTargetPosition(50);
        robot.lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.lift.setPower(0.3);
        while (robot.lift.isBusy() && (robot.lift.getCurrentPosition() < maxlift)) {}   //wait for lift to stop
        robot.lift.setPower(0.0);


        encoderDrive(DRIVE_SPEED, 2.5, 2.5, 5);    // back up
        encoderDrive(TURN_SPEED, -3, 3, 5 );   // turn to push glyph into cryptobox
        encoderDrive(DRIVE_SPEED, 3.5,3.5,5);   // backup away from glyph


        telemetry.addData("Path", "Complete");
        telemetry.update();
    }

    /*==================================================================================
     *  Method to perfmorm a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */
    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS) {
        int newLeftTarget;
        int newRightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newLeftTarget = robot.leftDrive.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
            newRightTarget = robot.rightDrive.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
            robot.leftDrive.setTargetPosition(newLeftTarget);
            robot.rightDrive.setTargetPosition(newRightTarget);

            // Turn On RUN_TO_POSITION
            robot.leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            robot.leftDrive.setPower(Math.abs(speed));
            robot.rightDrive.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                   (runtime.seconds() < timeoutS) &&
                   (robot.leftDrive.isBusy() && robot.rightDrive.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1",  "Running to %7d :%7d", newLeftTarget,  newRightTarget);
                telemetry.addData("Path2",  "Running at %7d :%7d",
                                            robot.leftDrive.getCurrentPosition(),
                                            robot.rightDrive.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            robot.leftDrive.setPower(0);
            robot.rightDrive.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move
        }
    }
}
