# WeightTracker

This application was developed for the mobile computing class (CSE350), taught by Professor Chuah at Lehigh University. Its aim is to provide an alternative to IoT scales. By simply using the camera on an Android device a user is able to take a photo of their existing scale and the number will be inferred using Optical Character Recognition (OCR).

# Required Technologies
Android SDK 23 (minimum 19)

Tesseract OCR API

AWS EC2 t2.micro instance running Ubuntu 14.04

Apache

MySQL

Flask framework

# Server

Built on top of a t2.micro EC2 instance, this server acts as both a database to store the users weight as they use the app. Additionally, it provides more CPU power to process images sent from the users Android device. The OCR will run here using a python wrapper for Tesseract.