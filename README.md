# G-Scores Back-end Java Service

This is the instruction for web developer to run the back-end service locally.
For a quick review, visit hosting [link](https://gscores-be.onrender.com). 

# Requirements

1. Maven tool + JDK or Docker

2. An IDE or cmd/shell/git bash

3. PostgreSQL

# Run the Service

### Step 1: Prepare Database

- Start your PostgreSQL on your machine. Then use the url with username and password to create an environment file following the template of '.env.example' file. Or you can use a hosting postgres with credentials.
- Move a copy of data seed file to '/resource/data' directory in the main package.

### Step 2: Run

- Add .env to the root base of folder structure.
- If you use IntelliJ IDEA (recommendation), load the project into IDE then add environment configuration then click Play button.
- If you use commandline tool, run these command in order:
```
export $(grep -v '^#' .env | xargs)
```
```
mvn clean package -DskipTests
```
```
mvn spring-boot:run
```



- If you use Docker, run this command to create an image:
```
docker build -t gscores .
```

After the image is created, run this command:
```
docker run --env-file .env -p 8080:8080 gscores
```


**GOOD LUCK!!!**

# Contributors

- Quoc Hoang
