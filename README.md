## Coffee Payment Rotation Project

### Contact

- josh.t.sachs@gmail.com
- Joshua Sachs

[3/10/25]

## Running the Application

### Prerequisites

- Java 11 or higher
- Gradle (or use the included Gradle wrapper)

### Quick Start

The easiest way to run the application is using the JAR file:

```bash
# Build the application
./gradlew clean build

# Run the JAR file
java -jar app/build/libs/app.jar
```

### Step-by-step Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/coffee-rotation-hpe.git
   cd coffee-rotation-hpe
   ```

2. **Build the application**:
   ```bash
   ./gradlew clean build
   ```
   On Windows, use `gradlew.bat clean build` instead.

3. **Run the application**:
   ```bash
   java -jar app/build/libs/app.jar
   ```

### Alternative Running Methods

You can also run the application directly with Gradle, but this will show progress indicators:
```bash
./gradlew run
```

For a cleaner output, use the quiet flag:
```bash
./gradlew run -q
```

### Using the Application

Once running, the application presents a menu-driven interface:
1. Add a Coworker - Register team members and their coffee preferences
2. List Coworkers - View all registered coworkers
3. Create Coffee Order - Record a new coffee order with participants and payer
4. Who Should Pay Next? - Get a recommendation based on fairness
5. View Payment Summary - See statistics and balances
6. Remove a Coworker - Remove someone from the rotation
0. Exit - Close the application

### Data Persistence

The application stores all data in two JSON files in the current directory:
- `coworkers.json` - Stores coworker information and preferences
- `orders.json` - Records order history and payment details

These files are created automatically and allow the application to maintain state between runs.


### Assumptions

1. I made it so that the user can create new coworkers and enter their preferred coffee beverages. I figured sometimes people change. 
2. I added a remove coworker option, in case someone quits the team. Don't need to keep them in the rotation. 
3. The app allows for custom orders where coworkers can order something different from their usual preference. 
4. The fairness algorithm prioritizes: Anyone who hasn't paid yet and whoever is most "behind" in payments relative to fair share.