# LampAndroid
Final Project for Connected Devices (IOT) class.

### Summary of the Class
The class assignments were built around a "Lamp" that was programmable using Raspberry PI and some LED lights. From this initial hardware (all built from scratch), I built a web UI, iOS interface with bluetooth low energy, and connected other Lamps to our own, through an EC2 instance on AWS. The web and iOS interfaces could change the hue, saturation, and brightness of the LED lights, as well as a manual UI on the lamp itself (physical screen on the lamp in the video). I definitely learned a lot about IOT, would highly recommend this class!

### About the Project
LampiAndroid is an Android application that uses bluetooth low energy and Google Assistant (voice command) to control the state of the Lampi. Basically, it is an extension to the already existing Web and iOS interfaces used to control the Lamp. I used [RxAndroidBLE](https://github.com/dariuszseweryn/RxAndroidBle) to scan for nearby bluetooth devices (The lamp was discoverable using bleno, which is a NodeJS module used to implement bluetooth low energy peripherals). With bleno, the lamp could be discoverable using LightBlue, and RxAndroidBLE was used to write/read to the lamp through the Android app.

With Google Assistant, I followed Google's quick tutorials on how to integrate built-in intent and app actions into the app. A video demo of the Android app is located below. 

When the app first runs, it will connect to the lamp and will display a UI similar to that of the lamp's UI (representing the HSV of the lamp). This functionality is done through reading from the lamp. Then, if a user were to drag the hue, saturation, or brightness bars in their app, the lamp/Web/iOS UI's will all update accordingly, as well as the LED lights. The power button will also turn the lamp on/off, and update all the other UIs accordingly. Finally, Google Assistant works. In addition, a user can say many different things that result in the same action ("Hey Google, turn my brightness to 0 percent", "Hey Google, turn my brightness down all the way", "Hey Google, turn my brightness to zero" all result in the same thing).

https://user-images.githubusercontent.com/50087436/171494068-5bac6cdf-4dca-43e8-bcb4-6d5f5a48aa09.mp4

