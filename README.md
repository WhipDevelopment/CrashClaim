![](https://i.imgur.com/g5rKXNp.png)
[Discord](https://discord.gg/6FU9eCjcrA) | SpigotMC | [Website](https://whips.dev)
***
CrashClaim is a claiming plugin oriented towards improving the claiming experience for surival servers wanting to fofer a clean and comprehensive system for their players. 

The latest downlaods can be found on our SpigotMC page here.

Check out our features [here](https://github.com/Chasewhip8/WhipClaim/wiki/Features).

## Setup
Downalod or build the latest compatible version for your server.

Make sure to download and install [CrashPayment](https://discord.gg/6FU9eCjcrA), our compatability layer for Vault based payment systems while providing better compatability and a nicer api.

## Building
All artifacts will be moved into `../Server116/plugins` automaticly.

**Requirements**
- Java 8 JDK or newer
- Maven
- Git

**Compiling Dependencies**
```
git clone https://github.com/CrashCraftNetwork/CrashUtils.git
cd CrashUtils/
mvn clean install
```
```
git clone https://github.com/WhipDevelopment/CrashPayment.git
cd CrashPayment/
mvn clean install
```

**Compiling From Source**
```
git clone https://github.com/WhipDevelopment/CrashClaim.git
cd CrashClaim/
mvn clean install
```

## Contirbute
If you need a feature that you want to upstream, file a pull request and we will take a look. 

Try anf folow the code style currently pressent in the class your editing and make clean and consise code changes with comments where needed. 
