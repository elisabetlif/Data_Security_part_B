# Print Server Application

This project is a Java-based Print Server application. It utilizes RMI (Remote Method Invocation) for remote operations and employs token-based authentication for secure access.

# Access control
This project focuses on a list permissions system, where we check the access rights of users: Alice, George, and GeorgeT.

- Alice has full access to all actions while logged in. However, once she logs out, she is no longer authorized to perform any actions.
- George has access to the print function but is not allowed to stop the printer.
- GeorgeT has access to setConfig and readConfig. There are intentional delays in the client’s execution to demonstrate the functionality and handling of session tokens.

We have two different log files: one for the actions performed by the PrintServer and another for log-in attempts and session token management.
The log files now capture a complete system run, including detailed server logs for all operations.


## Prerequisites

1. **Java Development Kit (JDK)**: Version 11 or higher.
2. **Apache Maven**: Make sure Maven is installed and available in your system's PATH.
3. **Configuration File**:
   - Ensure the `PublicFile.json` file is properly set up in the correct directory (`src/main/java/resource/`).

---

## Build the Project
1. Initialize Maven and Install Dependencies  
   Run the following command to initialize Maven and install all required dependencies:
   ```bash
   mvn i
   ```
2. If user has Maven in the system, the following command will setup and run the project:
   Compiles all implementation classes
   ```sh
   mvn compile
   ```
3. Start the Print Server
   ```sh
   mvn exec:java
   ```
4. Start the Client
   Open a new terminal and run
   ```sh
   mvn exec:java@second-cli
   ```

