package com.dinesh;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import java.io.File;

public class Camera extends JFrame {

    private VideoCapture capture;
    private JButton buttonCapture;
    private Mat frame;
    private JLabel cameraScreen;
    private Timer timer;
    private CascadeClassifier faceDetector;

    public Camera() {
        setWindow();
        initializeFaceDetector();
        initializeCamera();

        // Initialize JLabel
        cameraScreen = new JLabel();
        add(cameraScreen, BorderLayout.CENTER);

        // Initialize JButton
        buttonCapture = new JButton("Click Picture");
        buttonCapture.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captureImage();
            }
        });

        // Add JButton to the frame
        JPanel panel = new JPanel();
        panel.add(buttonCapture);
        add(panel, BorderLayout.SOUTH);
    }

    public void setWindow() {
        setTitle("Smart Attendance");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void initializeFaceDetector() {
        faceDetector = new CascadeClassifier("haarcascade_frontalface_default.xml");
        if (faceDetector.empty()) {
            System.out.println("Could not load classifier");
        } else {
            System.out.println("Classifier loaded successfully");
        }
    }

    public void initializeCamera() {
        capture = new VideoCapture(0); // for default camera
        if (!capture.isOpened()) {
            capture.open(0);
        }
        if (capture.isOpened()) {
            startCamera();
        } else {
            System.out.println("Unable to open camera");
        }
    }

    public void captureImage() {
        if (capture.isOpened()) {
            frame = new Mat();
            if (capture.read(frame)) {
                // Define the folder where images will be saved
                String folderPath = "captured_images";
                File folder = new File(folderPath);

                // Create the directory if it doesn't exist
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                // Generate a unique filename based on the current date and time
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String filename = folderPath + File.separator + "captured_image_" + timestamp + ".jpg";

                // Save the image
                Imgcodecs.imwrite(filename, frame);
                System.out.println("Image saved as " + filename);
            } else {
                System.out.println("Unable to capture image");
            }
        } else {
            System.out.println("Camera is not open");
        }
    }

    public void startCamera() {
        frame = new Mat();
        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (capture.read(frame)) {
                    // Convert the frame to grayscale
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

                    // Detect faces
                    MatOfRect faces = new MatOfRect();
                    faceDetector.detectMultiScale(grayFrame, faces);

                    // Draw rectangles around detected faces
                    for (Rect face : faces.toArray()) {
                        Imgproc.rectangle(frame, new org.opencv.core.Point(face.x, face.y),
                                new org.opencv.core.Point(face.x + face.width, face.y + face.height),
                                new Scalar(0, 255, 0), 2);
                    }

                    // Convert the Mat to ImageIcon and display it
                    ImageIcon image = new ImageIcon(HighGui.toBufferedImage(frame));
                    cameraScreen.setIcon(image);
                } else {
                    System.out.println("Unable to read frame");
                }
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new Camera();
    }
}
