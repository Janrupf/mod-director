# ModDirector
###### Automatically download mods with ease!

## About
ModDirector is a small program and mod at the same time, which can be used to download files
from the internet in a managed and controlled way. Sometimes it is not possible to ship files
with a binary distribution due to copyright issues, but it is often possible to download these
files and add them at runtime or first start. This is exactly what ModDirector does!

## Overview
Installing and configuring ModDirector is simple, just drag the jar into the mods folder and
start minecraft. Currently, we only support Forge officially, but more are set to follow. 
Technically, every mod which just relies on launchwrapper is currently supported, since
ModDirector itself is independent of the target platform.

### Configuration
This is just meant to give you a quick overview, a more detailed guide can be found in the
[wiki](https://github.com/Janrupf/mod-director/wiki).

ModDirector reads configurations from a config directory, which folder exactly depends on the
platform. For Forge, it is `config/mod-director`. Configuration files should go into that
directory, each of them ending with one of the supported suffixes.

## Using ModDirector in mod packs
You don't have to ask for permission, but it would be nice to credit us, but also the authors
of the mods you are downloading.

In case you are creating a pack for curse forge, the mod can be found
[here](https://www.curseforge.com/minecraft/mc-mods/moddirector)