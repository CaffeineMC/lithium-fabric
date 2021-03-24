![Project icon](https://git-assets.jellysquid.me/hotlink-ok/lithium/icon-rounded-128px.png)

# Lithium (for Fabric)
![GitHub license](https://img.shields.io/github/license/jellysquid3/lithium-fabric.svg)
![GitHub issues](https://img.shields.io/github/issues/jellysquid3/lithium-fabric.svg)
![GitHub tag](https://img.shields.io/github/tag/jellysquid3/lithium-fabric.svg)

Lithium is a free and open-source Minecraft mod which works to optimize many areas of the game in order to provide
better overall performance. It works on both the **client and server**, and **doesn't require the mod to be installed
on both sides**.

### What makes Lithium different?

One of the most important design goals in Lithium is *correctness*. Unlike other mods which apply optimizations to the
game, Lithium does not sacrifice vanilla functionality or behavior in the name of raw speed. It's a no compromises'
solution for those wanting to speed up their game, and as such, installing Lithium should be completely transparent
to the player.

If you do encounter an issue where Lithium deviates from the norm, please don't hesitate to
[open an issue.](https://github.com/jellysquid3/lithium-fabric/issues) Each patch is carefully checked to ensure
vanilla parity, but after all, bugs are unavoidable. Before opening a new issue, please check using the search tool that your issue has not already been created, and that if
there is a suitable template for the issue you are opening, that it is filled out entirely. Issues which are duplicates
or do not contain the necessary information to triage and debug may be closed. 

### Community

[![Discord chat](https://img.shields.io/badge/chat%20on-discord-7289DA)](https://jellysquid.me/discord)

We have an [official Discord community](https://jellysquid.me/discord) for all of our projects. By joining, you can:
- Get installation help and technical support with all of our mods 
- Be notified of the latest developments as they happen
- Get involved and collaborate with the rest of our team
- ... and just hang out with the rest of our community.

### Support the developers

Lithium is made possible by the following core contributors [and others](https://github.com/jellysquid3/lithium-fabric/graphs/contributors).
You can help support members of the core team by making a pledge to our Patreon pages below.

|    | Author   | Role   | Links   |
|----|:---------|:-------|:--------|
| ![jellysquid3's Avatar](https://avatars3.githubusercontent.com/u/1363084?s=32) | jellysquid3 | Project Lead | [Patreon](https://patreon.com/jellysquid) / [Contributions](https://github.com/jellysquid3/lithium-fabric/commits?author=jellysquid3) |
| ![2No2Name's Avatar](https://avatars3.githubusercontent.com/u/50278648?s=32) | 2No2Name | Maintainer | [Patreon](https://patreon.com/2No2Name) / [Contributions](https://github.com/jellysquid3/lithium-fabric/commits?author=2No2Name) |

---

## Installation

### Stable releases

#### Manual Installation (recommended)

The latest releases of Lithium are published to our [official Modrinth page](https://modrinth.com/mod/lithium) and [GitHub releases page](https://github.com/jellysquid3/lithium-fabric/releases). Usually, builds will be
made available on GitHub slightly sooner than other locations.

You will need Fabric Loader 0.10.x or newer installed in your game in order to load Sodium. If you haven't installed Fabric
mods before, you can find a variety of community guides for doing so [here](https://fabricmc.net/wiki/install).

#### CurseForge

If you are using the new CurseForge client, you can continue to find downloads through our
[official CurseForge page](https://www.curseforge.com/minecraft/mc-mods/lithium). Please note
that the CurseForge launcher does not natively support Fabric modding, so you will also need to install
[Jumploader](https://www.curseforge.com/minecraft/mc-mods/jumploader) in order to create a Fabric environment. As such,
we generally do not recommend this option, and are looking to phase out support for it in the near future. 

### Bleeding-edge builds

If you are a player who is looking to get your hands on the latest **bleeding-edge builds for testing**, consider
taking a look at the builds produced through our [GitHub Actions workflow](https://github.com/CaffeineMC/lithium-fabric/actions/workflows/gradle.yml). This
workflow automatically runs every time a change is pushed to the repository, and as such, they will reflect the latest
state of development.

Bleeding edge builds will often include unfinished code that hasn't been extensively tested. That code may introduce
incomplete features, bugs, crashes, and all other kinds of weird issues. You **should not use these bleeding edge builds**
unless you know what you are doing and are comfortable with software debugging. If you report issues using these builds,
we will expect that this is the case. Caveat emptor.

### Configuration

Out of the box, no additional configuration is necessary once the mod has been installed. Lithium makes use of a
configuration override system which allows you to either forcefully disable problematic patches or enable incubating
patches which are otherwise disabled by default. As such, an empty config file simply means you'd like to use the
default configuration, which includes all stable optimizations by default. 

See [the Wiki page](https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File) on the configuration file
format and all available options.

### Building from sources

#### Requirements

- JRE 8 or newer (for running Gradle)
- JDK 8 (optional)
  - If you neither have JDK 8 available on your shell's path or installed through a supported package manager (such as
[SDKMAN](https://sdkman.io)), Gradle will automatically download a suitable toolchain from the [AdoptOpenJDK project](https://adoptopenjdk.net/)
and use it to compile the project. For more information on what package managers are supported and how you can
customize this behavior on a system-wide level, please see [Gradle's Toolchain user guide](https://docs.gradle.org/current/userguide/toolchains.html).
- Gradle 6.7 or newer (optional)
  - The [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:using_wrapper) is provided in
    this repository can be used instead of installing a suitable version of Gradle yourself. However, if you are building
    many projects, you may prefer to install it yourself through a suitable package manager as to save disk space and to
    avoid many different Gradle daemons sitting around in memory.

#### Building with Gradle

Lithium uses a typical Gradle project structure and can be built by simply running the default `build` task.

**Tip:** If this is a one-off build, and you would prefer the Gradle daemon does not stick around in memory afterwards 
(often consuming upwards of 1 GiB), then you can use the [`--no-daemon` argument](https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:disabling_the_daemon)
to ensure that the daemon is torn down after the build is complete. However, subsequent Gradle builds will
[start more slowly](https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:why_the_daemon) if the Gradle
daemon is not sitting warm and loaded in memory.

After Gradle finishes building the project, the resulting build artifacts (your usual mod binaries, and
their sources) can be found in `build/libs`.

Build artifacts classified with `api` are only for developers trying to compile against Lithium's API.

Please note that support is not provided for setting up build environments or compiling the mod. We ask that
users who are looking to get their hands dirty with the code have a basic understanding of compiling Java/Gradle
projects.

---

### License

Lithium is licensed under GNU LGPLv3, a free and open-source license. For more information, please see the
[license file](https://github.com/jellysquid3/lithium-fabric/blob/1.16.x/fabric/LICENSE.txt).
