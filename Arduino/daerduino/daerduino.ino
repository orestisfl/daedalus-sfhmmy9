#include <SoftwareSerial.h>
SoftwareSerial BTSerial(4, 2); // RX, TX

typedef unsigned long Millis;

// the pin that the pushbutton is attached to.
#define BUTTON_PIN 7
// button being pressed for 1 second => emergency mode.
#define EMERGENCY_DIFF_MILLIS 1000
// the pin that gives voltage to our buzzer.
#define BUZZER_PIN 6

int emergencyPressesCount = 0;
unsigned long startTime = 0;

void setup() {
    // Open serial communications:
    Serial.begin(9600);
    Serial.println("Type AT commands!");
    pinMode(BUTTON_PIN, INPUT);
    pinMode(BUZZER_PIN, OUTPUT);

    // The HC-06 defaults to 9600 according to the datasheet.
    BTSerial.begin(9600);
}

// TODO: make it non-blocking.
bool emergencyButtonPressed() {
    Millis startTime = millis();
    while (digitalRead(BUTTON_PIN) == HIGH) {
        // Delay a little bit to avoid bouncing
        delay(50);
        if (millis() - startTime > EMERGENCY_DIFF_MILLIS) return true;
    }
    return false;
}

bool checkBT() {
    if (BTSerial.available()) {
        return true;
    } else {
        delay(10);  // wait for small timeout
        return BTSerial.available();
    }
}

String readBT() {
    String response = "";
    while (checkBT()) { // While there is more to be read, keep reading.
        response += (char)BTSerial.read();
        // small delay to ensure we receive whole command. Maybe it's better to use short commands
        // to avoid this.
    }
    return response;
}

bool isConnected() {
    BTSerial.write("AT");
    delay(600);  // it needs a huge delay.
    String response = readBT();
    Serial.println("isConnected:");
    Serial.println(response);
    return response != "OK";
}

void loop() {
    // Read received data if available.
    String command;
    if ((command = readBT()) != "") {
        if (command == "1") BTSerial.println("LED: ON");
        else if (command == "0") BTSerial.println("LED: off");
        else if (command == "RING") sing();

        Serial.println("Received command:");
        Serial.println(command);
    }

    // Read user input if available.
    while (Serial.available()) {
        delay(10); // The delay is necessary to get this working!
        BTSerial.write(Serial.read());
    }

    if (emergencyButtonPressed()) {
        // Send emergency here.
        Serial.println("pressed!");
        BTSerial.write("RING");
    }
}
