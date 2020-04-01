
![Project icon](https://github.com/jellysquid3/lithium-fabric/raw/1.16.x/dev/doc/logo.png)

# Lithium
![GitHub license](https://img.shields.io/github/license/jellysquid3/lithium-fabric.svg)
![GitHub issues](https://img.shields.io/github/issues/jellysquid3/lithium-fabric.svg)
![GitHub tag](https://img.shields.io/github/tag/jellysquid3/lithium-fabric.svg)
[![CurseForge downloads](http://cf.way2muchnoise.eu/full_360438_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/lithium)

Lithium is a free and open-source Minecraft mod (under GNU LGPLv3) which works to optimize many areas of the game
in order to provide better overall performance. It works on both the **client and server**, and **doesn't require the mod to be installed on both sides**.

:warning: **Lithium is still in an early stage of development.** This mod contains a large number of patches which may cause issues in your game. Please be prepared
to edit your configuration file to narrow down issues. If you do run into an issue, please let me know through opening an issue!

### Support development

You can help buy me food and support development while getting early access builds of my mods by [making a monthly pledge to my Patreon!](https://patreon.com/jellysquid) You'll also gain some special perks, such as prioritized support on [my Discord server](https://discord.gg/kcb57Cm).

<a href="https://www.patreon.com/bePatron?u=824442"><img src="https://github.com/jellysquid3/Phosphor/raw/1.16.x/dev/doc/patreon.png" width="200"></a>

### Join the Discord

You can join the official Discord for my mods by [clicking here](https://discord.gg/UEa6r3d).

<a href="https://discord.gg/ApPrpT"><img src="https://i.vgy.me/YrTrsE.png"></a>

### Compiling the mod

You will need the latest version of Gradle 4.9 present on your system along with the Java 11 JDK. For Windows users, you can use [Chocolatey](https://chocolatey.org) or [SDKMAN](https://sdkman.io/)
to manage these installations. These should be standard packages on most other operating systems. Alternatively, you can use the included Gradle wrapper with `gradlew`.

Once the prerequisites have been met, start a build with:

```
gradle build
```

The resulting build artifacts will be present in `build/libs`.

### License

Lithium is licensed under GNU LGPLv3, a free and open-source license. For more information, please see the [license file](https://github.com/jellysquid3/lithium/blob/master/LICENSE.txt).
