# Copycat

A Minecraft plugin that converts images from URLs into pixel art using in-game blocks. HTTP Redirects and basic auth are supported.


![Example](assets/example.png)
![Example](assets/basicauth.png)

## Table of Contents
<!-- TOC -->
* [Copycat](#copycat)
  * [Table of Contents](#table-of-contents)
  * [Command Tree](#command-tree)
  * [Features](#features)
  * [Experimental](#experimental)
  * [Possibly Upcoming](#possibly-upcoming)
  * [How It Works](#how-it-works)
  * [Version History](#version-history)
<!-- TOC -->

## Command Tree
```
/cc
├── admin
│   └── undo <player>               # Admin undo for specific player
├── list                            # List all saved image shortcuts
├── add <name> <url>                # Add new image shortcut
├── remove <name>                   # Remove image shortcut (with tab completion)
├── copy <shortcut>                 # Render image from shortcut (with tab completion)
├── undo                            # Undo last render for current player
├── poll <shortcut>                 # Continuously poll and render image from URL
└── set
    ├── dithering <true|false>      # Enable/disable image dithering
    ├── dimensions <width> <height> # Set render dimensions
    └── profile <profileName>       # Set block profile 
```

## Features
- **Brigadier Command System**: The command system has been updated to support the new brigadier system and provide tab completion
- **Image-to-Block Conversion**: Downloads images from URLs and converts them into Minecraft block structures
- **Color Mapping**: Preprocesses Minecraft block textures to create color averages for accurate material matching
- **Image Processing**: Automatically resizes and dithers images to optimize them for pixel art conversion
- **Per-Pixel Analysis**: Maps each pixel to the closest matching Minecraft block based on color similarity
- **Basic Auth**: Support for HTTPS basic auth is included

## Experimental
- **Polling**: Will repeatedly hit a URL to refresh an image. Polling will stop on a server restart. There are no controls for abuse. I suggest this be used on a personal server only. Undo also does not work for poll commands.

## Possibly Upcoming
- **Permissions** Because they sound important. 
- **Persistent Undo Buffers** Right now, they are in memory only and lost after server restart 
- **Persistent Polling** to keep streaming those streams
- **Dynamic Color Mapping** to set a default color map with the server's loaded resource pack.

## How It Works

1. Enter an image URL in-game
2. The plugin downloads and resizes the image to your specified resolution
3. Each pixel is analyzed and mapped to the closest matching Minecraft block color
4. The plugin places the corresponding blocks to recreate the image as pixel art

**Note**: Results are optimized for the default Minecraft textures. Using other texture packs may produce suboptimal color matching.

## Version History

This plugin was originally created for Bukkit 1.4.7 and has been recently updated to work with modern PaperMC versions (1.21+).

For historical information about the original plugin, see: https://bukkit.org/threads/edit-copycat-v1-0-render-images-from-a-url-1-4-6-r0-4.12730/

