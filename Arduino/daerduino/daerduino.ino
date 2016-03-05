#include <SoftwareSerial.h>
SoftwareSerial BTSerial(4, 2); // RX, TX

#include <TimerOne.h>

typedef unsigned long Millis;

// the pin that the pushbutton is attached to.
#define BUTTON_PIN 7
// button being pressed for 1 second => emergency mode.
#define EMERGENCY_DIFF_MILLIS 1000
// the pin that gives voltage to our buzzer.
#define BUZZER_PIN 6
#define BT_TIMEOUT_MILLIS 50
#define INTERRUPT_FREQUENCY_MICROS 1500000

int emergencyPressesCount = 0;
unsigned long startTime = 0;

typedef enum {STATE_OFF, STATE_PROTECTED, STATE_SEMI, STATE_DANGER} State;
volatile State currentState;
volatile bool playRing = false;

void setup() {
    // Open serial communications:
    Serial.begin(9600);
    Serial.println("Type AT commands!");
    pinMode(BUTTON_PIN, INPUT);
    pinMode(BUZZER_PIN, OUTPUT);

    Timer1.initialize(INTERRUPT_FREQUENCY_MICROS);
    Timer1.attachInterrupt(interruptBluetooth);

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
    Millis currentTime = millis();
    while (true) { // While there is more to be read, keep reading.
        if (checkBT()) {
            char c = (char)BTSerial.read();
            if (c == ';' || response == "OK") return response;
            response += c;
            currentTime = millis();
        } else if (millis() - currentTime > BT_TIMEOUT_MILLIS) return "";
    }
}

String readBTNonBlocking() {
    String response = "";
    while (checkBT()) { // While there is more to be read, keep reading.
        response += (char)BTSerial.read();
    }
    return response;
}

bool isConnected() {
    BTSerial.write("AT");
    delay(600);  // it needs a huge delay.
    String response = readBTNonBlocking();
    return response != "OK";
}

void interruptBluetooth() {
    Serial.println("Interrupted:");
    // Read received data if available.
    noInterrupts();
    if (isConnected()) {
        Serial.println("CONNECTED sent to pair.");
        BTSerial.write("CONNECTED");
    } else {
        Serial.println("No connection to send to pair.");
        return;
    }
    while (checkBT()) {
        String command = readBT();
        if (command == "RING") {
            playRing = true;
        } else if (command == "SEMI") {
            currentState = STATE_SEMI;
        } else if (command == "POFF") {
            currentState = STATE_OFF;
        } else if (command == "RSTOP") {
            playRing = false;
        } else if (command == "PON") {
            currentState = STATE_PROTECTED;
        } else if (command == "DANG") {
            currentState = STATE_DANGER;
        } else {
            // dont' respond.
            continue;
        }
        BTSerial.write("OK");

        Serial.println("Received command:");
        Serial.println(command);
    }
    interrupts();
}

void loop() {
    // Read user input if available.
    if (playRing) sing();
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
