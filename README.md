<div align='center'>

![Awesome](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)
</div>

## Zhang Chessboard Generator

A pure Kotlin utility designed to generate high-resolution, print-ready A4 chessboard patterns (7x9 squares / 6x8 inner corners) specifically optimized for camera calibration using Zhang's Method (e.g., OpenCV, MATLAB).

### Features

- **Camera Calibration Ready:** Produces an asymmetrical grid (6x8 inner corners / 7x9 squares) preventing 180-degree rotation ambiguity in OpenCV's findChessboardCorners.

- **Print-Optimized (300 DPI):** Formatted for A4 Portrait paper (210 x 297 mm) to maximize square size while preserving quiet-zone safety margins.

- **Pure Kotlin PDF Exporter:** Generates standalone PDF 1.4 documents natively without relying on operating system print drivers, CUPS, or third-party dependencies.

- **Sharp Geometry:** Disables anti-aliasing during rendering to maintain pixel-perfect, mathematically sharp corner transitions for subpixel detection.

- **Solid Bounding Box:** Configured with 4 dark outer corners to ensure the pattern is cleanly bounded against the white margin.

- **Built-in Preview GUI:** Includes a Swing-based desktop visualizer window for immediate verification inside IntelliJ IDEA.

### Printing Guidelines for Accurate Calibration

To ensure the physical target yields precise camera intrinsic and distortion parameters:

- **Print Scale:** Always select "Actual Size" or 100% Scale in your PDF reader. Do NOT use "Fit to Printable Area", as scaling distorts the true physical millimeter dimensions of the squares.

- **Mounting:** Mount the printed page on a flat, rigid board (such as MDF, acrylic, or thick cardboard) to prevent paper warping, which introduces reprojection errors.

- **Square Size Measurement:** Measure the printed square side with a caliper and pass the exact value (in millimeters or meters) to your calibration pipeline (squareSize).
