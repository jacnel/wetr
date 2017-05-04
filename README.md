Note: this write up was part of a formal document, in this version the images have been removed.

# WETR: Making the Bathroom Scale Smart
By: Jacob Nelson


# 1)	Introduction

Over the last several years there has been a notable increase in the presence of Internet of Things devices. They range from cameras to thermostats and door locks to wearable technology. IoT is being adopted at an amazing rate and estimates on the high end predict that there will be over 20 billion devices by 2020 (IEEE Spectrum article). While growth is fast, there are some obstacles that keep IoT devices from being accessible to all. Chief among them is the price. For example, a regular pack of four LED lightbulbs costs around twenty dollars, but the IKEA smart lighting, which only includes two bulbs, costs eighty dollars (Amazon and IKEA). Based on this discrepancy and inspired by Genie (GENIE paper), the software thermostat project by Balaji et al., I decided to attempt to bridge the gap between the high cost of IoT devices and the conveniences they provide. In this project, I propose a mobile application, WEight TRacker (WETR), that allows users to turn any ordinary digital bathroom scale into a smart scale. It allows them to take a photo of their scale, which is in turn analyzed and the weight extracted using optical character recognition. Beyond being simple to use, it also provides a way for them to monitor their weight over time. This application gives users access to the benefits of connected devices without having to pay an exorbitant price. In the following document, I present the motivation behind the application, some background on the necessary technologies, a discussion on the implementation of WETR, an evaluation based on user feedback, and finally further development goals.

# 2)	Motivation

The motivation behind WETR is twofold. First, it is an effort to close the digital divide that IoT device prices inherently create. And second, it is aimed at making healthy living easier by giving users a simple way to monitor their weight over time. 

As previously mentioned in the introduction, there is a wide gap in pricing for most connected devices. While the price of compute resources continues to drop, there is still a large overhead incurred due to the software necessary for them to function. Thus, many think that smart devices are a luxury. When it comes to the smart scale industry, there are few that match the price of a basic bathroom scale. Even the lower end of the spectrum can cost more than fifty dollars, while a traditional digital scale can be bought for under fifteen dollars. Additionally, many people do not want to buy a brand-new scale. They already own one and have been using it for years. When working to augment the traditional physical thermostats at University California San Diego, Balaji et al. found the solution was to take existing technology and add a software layer on top to make it smarter. This is the same approach that WETR takes. It needs nothing more than an Internet connected phone with a camera to turn any conventional digital scale into a smart scale that will help users track their weight. 

There are many applications, wearable technologies, and other smart devices that help us keep an eye on our health. WETR aims to disrupt this space by changing the way data is recorded. Most applications that are not connected to a smart scale use manual input to enter data. This decreases the usability by adding more user interaction. The primary inspiration for WETR was Snapchat. It is a straightforward photo messaging application that is very popular with the younger generations. When opened, Snapchat directly loads the camera screen to take a photo, the user then chooses the recipients and send it. The streamlined interface reduces friction between the application and the user, which I believe influenced the success of Snapchat. Similarly, WETR presents a user interface designed to maximize ease of use. Users are brought directly to the camera screen which takes when the screen is tapped, after which the image is uploaded to the server for processing. Additionally, there is a button that brings the users to a graph displaying their weight data over the last week. In designing WETR it was important that interaction be as simple as possible. The ability to detect the weight directly from an image is intended to reduce interaction. Making recording the users weight as easy as taking a photo. The image is then analyzed using optical character recognition, which is an important field in computer vision and will be discussed further in the background section, to extract the weight data.

WETR aims to reduce the digital divide by combining computer vision techniques, mobile computing and simple design principles. These motivating factors helped shape this application and I believe the model of adding software layers to make existing technologies smart will have an impact on how people interact with the world.

# 3)	Background

The following section will discuss some background necessary to understand WETR’s implementation. Particularly, optical character recognition, which is the functional foundation for this application.

# 3.1 Optical Character Recognition (OCR)

Optical character recognition is a computer vision technique which takes an image containing characters (i.e. a scanned PDF document) and extracts the words represented in the document into machine encoded text. It is widely used in libraries, legal settings and for visually impaired readers. For WETR, this manifests itself in the seven-segment digital display being parsed for the digits it contains, which is then turned into the user’s weight and stored in a database.

In broad terms, the document is first scanned as an image then processed to produce a bitmap. This bitmap goes through several preprocessing steps to get it ready for recognition. Among these include de-skewing the image, creating a binary image (only black and white), layout analysis, word and line detection, and character isolation or segmentation. Aligning the image correctly and normalizing the aspect ratio is important for recognizing characters based on preexisting font data. Transforming the image into only black and white helps edge detection algorithms accurately detect the letters in the document. Layout analysis and word and line detection limits the compute space required by the recognition algorithms, thus reducing latency. Finally, character isolation and segmentation either breaks connected characters into their unique shapes or combines segments that pertain to the same character. When these steps are finished, the recognition algorithm is ready to be invoked on the image.

There are two accepted ways for recognizing a character within a document. The first uses a matrix of pixels for known characters in a known font. Comparison with the parsed character from the document results in a match confidence level, which is accepted above some threshold. Some limitations of this approach include a reliance on high target character isolation, a lack of flexibility in terms of scale and similarity to a known font, as well as a higher computational load. The other method extracts glyph characteristics of the target reducing the number of features. Characteristics can represent lines, closed loops, line directionality, and line intersections. This abstraction is then compared to a vector-like representation of characters in different fonts to determine a match using some nearest neighbor classifier. Feature detection is a general computer vision strategy and is more robust than the matrix character recognition, making it the method of choice for most modern OCR software. In WETR, I utilize an open source OCR library called Tesseract, which is maintained by Google. Tesseract adds another dimension to the recognition task by implementing a two-phase algorithm. In the first pass normal character recognition occurs, but in the second pass high confidence matches from the previous pass are also used to find better matches for characters below a threshold of confidence.

# 3.2 Base64 Encoding

Base64 is an encoding strategy to translate binary data into a string representation. Each character in the string represents exactly six bits of binary data. Because six binary digits have a total of sixty-four combinations of ones and zeros there are sixty-four different characters that represent six bits at a time. Typically the bits are encoded using characters ‘a-z’, ‘A-Z’, and ‘0-9’ along with two additional special characters that depend on the encoding implementation. When the length of binary data is not a multiple of six bits then padding is added during encoding to ensure that the encoded string is. The table to the right shows the Base64 translation table. In the context of WETR, Base64 allows the Android device to send an image as a string to the server, which then decodes it and performs OCR on the received data. 


# 4)	Implementation

Section 4 explains the implementation of different application components. It can be divided into three specific sub-groups. First, the Android development process and decisions is discussed. Then, the work implementing a workable optical character recognition algorithm is be shown. And finally, the backend development process is considered.

# 4.1	Android Development (Android, Java)

To keep the application as simple as possible, there are only two user flows that can occur. The following figure demonstrates the possible paths that a user can take after opening the app. When the user launches the application, they are immediately greeted with a screen that displays the rear facing camera. Right off the bat the application is ready to capture a picture for processing. With the first use case, the user taps the shaded area on the screen, the camera automatically focuses, takes a photo, and sends it as a Base64 encoded string to the server. The server then responds with the recognized number, passing it back for verification by the user before inserting it into a database. When accepted, the correct number is added to the database and a message is returned to the device confirming this action. Now the user can take another photo or move on to the second use case: displaying a graph. When the user taps the small white button in the upper right corner the mobile device sends a request for the user data from the last week. When returned, the values are graphed using the AndroidPlot graphing library.

Internally, a few important considerations were needed. While there are Android libraries that do OCR, it was my goal to reduce overhead on the phone. This prompted me to decide to setup an external server that analyzes the images instead. The backend of this application will be discussed in further detail in section 4.3. By offloading this work, it both saves battery life and improves overall execution time. A dedicated machine is responsible for the bulk of the computation instead of the less powerful mobile phone CPU. A trick was needed when dealing with a Base64 encoded image string. The string is too large to pass it as an extra in an Intent, so instead I save the image as a file and let the Activity responsible for sending it to the server do the encoding. Finally, I chose AndroidPlot to graph the user weight data because of its overall ease of use, stability, performance and support. It made it easy to create a rich graphic that is informative to the user. Due to the nature of camera based applications in Android, I was required to extend the SurfaceView class to create a CameraPreview class that displays the frames from the camera. CameraPreview implements the SurfaceHolder.Callback interface to take care of starting and stopping the camera preview when the surface is created or destroyed respectively. To take a photo, the user taps the shaded area of the screen and a captureImage() function is called.  This function implements a callback function associated with my Camera instance. When the Camera takes a picture, a Bitmap is created from the raw byte array, is then compressed to a JPEG format and finally encoded as Base64 string and saved to a file.  At this point, all that is left is reading that file and sending its contents to the server for analysis.

# 4.2	OCR Development (Python, OpenCV, Tesseract)

The most technical application logic exists within the OCR processing that takes place on the server. This portion of the application relies heavily on OpenCV for preprocessing and Tesseract for ORC. When the image is received from the Android device, it first undergoes a decoding phase to translate the Base64 encoded message to an OpenCV-usable type. Next, preprocessing extracts the display, converts it to black and white, and cleans up the edges to prepare it for recognition. Finally, the preprocessed image is piped into Tesseract for recognition. To recognize the seven-segment display, a specific font was necessary called “letsgodigital”, which is included with the Tesseract “langdata” repository. During the evaluation section I will touch on the accuracy of my recognizer based on this borrowed training data and suggest methods for improving its accuracy. 

# 4.2.1 Image Preprocessing

Some important steps in the preprocessing are listed below. Most of the preprocessing is dedicated to extracting the area in which the numbers are located. This was achieved by finding the largest contour in the image that had exactly four vertices. While this strategy limits the usability to rectangular digital displays, it also extracts only the portion of the image that contains numbers.

•	Bilateral filtering: 
o	A non-linear, edge preserving, noise reduction filter. It takes both proximity and intensity into consideration when determining the de-noising weight. A benefit is that it preserves sharp edges and produces a staircase effect in intensity, which helps later preprocessing steps. But, a drawback is that it can introduce false edges into the image.
•	Canny edge detection:
o	A multi-staged edge detection algorithm developed by John. F Canny in 1986. While it is computationally expensive, it provides some important benefits. Namely, that has a low error rate, good localization of edges, and only finds each edge once.
•	Contour extraction: 
o	An OpenCV function that detects the curves joining continuous edges found during the edge detection phase. After the contours are discovered, it is possible to filter them to find the largest rectangle which is likely to be the digital display.
•	De-skewing:
o	After the outline of the display is discovered, a perspective transform de-skews the image so that it can be processed easily by Tesseract.
•	Binarization: 
o	A threshold is carefully set for which all pixels of lower intensity become white and all pixels with greater intensity become black. 
•	Resizing:
o	This is the final step before Tesseract takes over and processes the image. After some experimenting, tesseract responds best when the height of each letter is roughly fifty pixels, so we must resize the image. See Figure 4.

# 4.2.2 Tesseract

The final step of OCR is to let Tesseract work its magic. After preprocessing the image is piped into Tesseract with the appropriate parameters. By explicitly telling Tesseract to utilize the “letsgodigital” font, I could ignore any false positives from other fonts that did not resemble the digital display. Additionally, the Tesseract call whitelisted only ‘0-9’ and ‘.’ for recognition, thus eliminating false positives due to characters outside this set being recognized.

# 4.2.3 Post-processing

After the call to Tesseract returns with a string, it is cleaned up so that only the substring from the beginning to the digit to the right of the first index is passed. This avoids potential mishaps when recognition goes awry. It guarantees that only on decimal exists in the string, ensuring it can be read as a numeric value on the mobile device.

# 4.3	Backend Development (Python, Flask Framework, MySQL, AWS EC2)

Now that the OCR implementation details have been discussed, I will move on to the topic of backend development. Written in Python, following the Flask framework, my server was deployed on an Amazon Web Services EC2 instance running Apache and the Ubuntu 14.04 operating system, hosting a MySQL database to store user’s weight entries.

# 4.3.1 Flask Server

Flask is a lightweight server framework for developing in Python. Its simplicity lent itself well to this project because it is small. However, Flask is also highly extensible and provides many helpful externally modules, such as SQLAlchemy for SQL database connectivity. Using an AWS EC2 t2.micro instance allows constant access to the server through the public hostname of the EC2 instance at port 80. An upside of using the t2.micro over a more powerful CPU is that I am able to host my service for free for 750 hours.

The server is a basic model-view-controler paradigm with three routes defined. The first accepts a POST request containing image data at ‘/process_img’. It is at this step that the OCR code is invoked and a string containing the recognized characters is sent in response. Second, the verification phase of the Android app sends another POST request to the server containing the corrected value for the user’s weight. At this stage, the server updates the MySQL database with the user’s name, the current time, and the user’s weight.  Finally, when the user navigates to the graph on the mobile device, a POST request is sent to the server, containing the period of user data to retrieve from the database (i.e. “week” or “month”). The server queries the database, finding all entries in the period and returns a list of dates and weights to the caller. 

# 4.3.2 MySQL Database

To store user weights I decided to host a MySQL database. The structure was simple with only one table storing user names, the date the measurement recorded in UTC, as well as the weight itself. The name is stored as a ‘varchar’, as is the date, but the weight is a ‘Decimal’ number with a precision of two. Having such a simple storage mechanism proved to be both beneficial and harmful to development. Insertions were straight forward, but queries proved to be slightly cumbersome. Storing the date as a ‘varchar’ made finding values with a range of dates difficult, although not impossible. Due to the formatting of UTC datetimes in Python, the string representation is entirely numerical, making it possible to do a simple string comparison looking
for all matches with strings greater than the start date. An example of this logic is shown in Figure 4.  

# 5)	Evaluation

This section will discuss the application from a critical perspective, touching on some of the aspects that were well developed and areas that lacked. First I will discuss the design choices and possible alternatives that could make the application more robust. Next, I will introduce some feedback given to me by my peers and comment on their thoughts.

# 5.1 Implementation and Design

The OCR capability was surprisingly accurate, although it took significant trial and error to find what preprocessing steps to take. Ultimately, the step that resulted in better than expected recognition was reducing the size of the preprocessed image such that the height of each character was roughly fifty pixels. Any larger and Tesseract would have difficulty recognizing them; any smaller and the decimal would get lost.

When considering databases for this project I settled with MySQL because I felt comfortable with it and understand its syntax. However, as the project developed I began to wonder how a time series database would change the application. It is a transition worth considering in the future to allow more efficient querying.

This prototype does not include user management, which makes it the opposite of scalable. I made the decision to withhold user logins so I could focus my efforts on the more interesting portions of the project, like OCR. In the next development iteration, this will likely be the first additional feature. 

Finally, this application needs further testing on a range of digital scales. Mine at home is back lit, which creates high contrast in the photos processed. To verify that this implementation is robust enough to handle all digital scales, testing on a variety of displays is necessary.

# 5.2 User Feedback

After showing a few friends the application I built, they provided sound feedback to improve it. Primarily they were underwhelmed with the feature list of the app. Two of them suggested that health tips or a body mass index indicator be added. I think these are both great decisions and will keep them in mind for the future. 

When it came to the ease of use, they were pleased with how simple it was and commented that it was intuitive. The simplicity allowed them to pick them up and understand what to do without prompting. Although they claimed it is not complicated, I would like to implement some sort of walk through the first time the application is opened, to orient the user.

More feedback I received, from multiple sources, was that the graph of weight over time could be extended. It was my intention include larger periods, but complications with AndroidPlot kept me from achieving the desired effect. Further research into better tools may be needed to overcome this hurdle.

Overall, the users that tested my system were pleased with its responsiveness, ease of use and simplicity. 

# 6)	Conclusion

WETR is a mobile application that attempts to bridge the divide between “dumb” scales and the IoT explosion. WETR utilizes optical character recognition, takes advantage of an external server for image processing, and implements a database to store user data. Translating images to usable data makes buying a brand-new scale unnecessary. Now, anyone can turn their current digital bathroom scale into a high-tech smart scale, without any additional purchases. For people who cannot afford to pay for such technology, this is a great alternative. With WETR, monitoring health is easier than ever and is accessible to anyone that owns an Android smart phone with a camera. 
