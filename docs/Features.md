# TISCM Features

------

[中文](https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/docs/Features_cn.md)

**Notice**: Features here maybe incomplete, some features in original carpet or developing features may not appear here

------

# Features

> `/carpet <CommandName> <Value>`

## commandPing

Enables `/ping` command to see your ping

Default: `true`

Options: `false`, `true`

Categories: creative

## dragonCrashFix

fix infinite loop in dragon AI causing server crash

Default: `false`

Options: `false`, `true`

Categories: bugfix

## optimizeVoxelCode

optimizes the voxel code which is used by e.g. the entity movement

Default: `false`

Options: `false`, `true`

Categories: optimization

## chunkCache

Improved chunk caching by PhiPro

Default: `false`

Options: `false`, `true`

Categories: optimization

## entityMomentumLoss

Disable/Enable the entity momentum cancellation if its above 10 blocks per gametick when reading the data out of disk

Default: `true`

Options: `false`, `true`

Categories: experimental

## stainedGlassNoMapRendering

Disable the rendering of stained glass on maps

Default: `false`

Options: `false`, `true`

Categories: experimental

## cacheExplosions

Caching explosions, useful for situations eg pearl cannon

Default: `false`

Options: `false`, `true`

Categories: optimization

## missingLightFix

Treat any subchunk with light changes as a not-empty subchunk to solve the missing sky / block light in empty subchunk after reloading the chunk

no more ghost shadows below giant floating buildings

Default: `false`

Options: `false`, `true`

Categories: experimental, BUGFIX

## newLight

Uses alternative lighting engine by PhiPros. AKA NewLight mod

Now Ported to 1.13 by Salandora!

Default: `false`

Options: `false`, `true`

Categories: experimental, optimization

## portalSuperCache

Greatly improve the efficiency of nether portal by LucunJi

Most powerful portal optimization ever, 10000 times faster!

**DOES NOT WORK with portals created/destroyed when fillUpdate is false**

Default: `false`

Options: `false`, `true`

Categories: experimental, optimization

## microtick

Enable the function of `/log micirotick`

Display actions of redstone components and blockupdates with wool block

Use `/log microtick` to start logging

endrods will detect block updates and redstone components will show their actions

- observer, piston, endrod: pointing towards wool

- repeater, comparator, rail, button, etc.: placed on wool

Default: `false`

Options: `false`, `true`

Categories: command, creative

## structureBlockLimit

Overwrite the size limit of structure block

Relative position might display wrongly on client side if it's larger than 32

Default: `32`

Options: `32`, `64`, `96`, `128`

Categories: creative

## optimizedInventories

Optimizes hoppers and droppers interacting with chests

Credits: skyrising (Quickcarpet)

Default: `false`

Options: `false`, `true`

Categories: experimental, optimization

## YEET

**Warn**: all yeet options will change vanilla behaviour, they WILL NOT behave like vanilla

### yeetFishAI

yeet fish followGroupLeaderAI for less lag

Default: `false`

Options: `false`, `true`

Categories: yeet

### yeetGolemSpawn

yeet Golems spawning at village for faster stacking at 14k iron farm test

Default: `false`

Options: `false`, `true`

Categories: yeet

### yeetVillagerAi

yeet villager ai for faster stacking at 14k iron farm test

Default: `false`

Options: `false`, `true`

Categories: yeet

------

# Loggers

- Store logger status to automatically load unclosed loggers after reboot

> `/log <LoggerName> [<Option>]`

## projectiles

added hit point

Options: `brief`, `full`, `visualize`

### visualize

visual logger for projectiles

## chunkdebug

Log chunk loading / unloading

Options: none

##  villagecount

log village count on tab list

Options: none

## tileticklist

log tile tick list for making tiletick EMP

Options: none

## tileentitylist

log when a tile entity is added to or removed from `World`'s `loadedTileEntityList`

Options: none

## microtick

Display actions of redstone components and blockupdates with wool block. Type `/carpet microTick` for more detail

Options: `all`, `unique`

### all
Output all messages, default stats

### unique

Only output unique messages. Turn it on if you don't want to be spammed by redstone dusts

------

# Fixes

Fixes for original CarpetMod

- fix no username length limit with `/player` command (long name will make everyone cannot enter the server) 
- remove lever modification ability from flippinCactus
