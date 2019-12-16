- # TISCM 特性列表

	------

	[English](https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/docs/Features.md)

	**注意**：此处的功能列表并不完整，可能有原版carpet或者正在开发中的功能没有增加

	------

	# 功能

	> `/carpet <CommandName> <Value>`

	## commandPing

	启用`/ping`指令来获取你的ping值

	默认值： `true`

	选项：`false`, `true`

	分类：creative

	## dragonCrashFix

	修复了末影龙AI中导致崩服的无限循环

	默认值： `false`

	选项：`false`, `true`

	分类：bugfix

	## optimizeVoxelCode

	优化了voxel部分的代码

	默认值： `false`

	选项：`false`, `true`

	分类：optimization

	## chunkCache

	由PhiPro写的区块缓存代码

	默认值： `false`

	选项：`false`, `true`

	分类：optimization

	## entityMomentumLoss

	Disable/Enable the entity momentum cancellation if its above 10 blocks per gametick when reading the data out of disk

	关闭/启用加载区块时速度过10m/gt实体的速度丢失

	默认值： `true`

	选项：`false`, `true`

	分类：experimental

	## stainedGlassNoMapRendering

	关闭地图上渲染染色玻璃

	默认值： `false`

	选项：`false`, `true`

	分类：experimental

	## cacheExplosions

	爆炸缓存，对珍珠炮等情况优化巨大

	默认值： `false`

	选项：`false`, `true`

	分类：optimization

	## missingLightFix

	将光照变化过的子区块视作非空子区块，以修复浮空建筑下的黑影等bug

	默认值： `false`

	选项：`false`, `true`

	分类：experimental, BUGFIX

	## newLight

	由PhiPro写的更好的光照代码，即NewLight  mod

	由Salandora移植到1.13！

	默认值： `false`

	选项：`false`, `true`

	分类：experimental, optimization

	## portalSuperCache

	目前最高效的地狱门优化

	**对fillUpdate=false时创建的地狱门方块无效**

	默认值： `false`

	选项：`false`, `true`

	分类：experimental, optimization

	## microtick

	启用`/log microtick`的功能

	使用羊毛块来输出红石元件的动作与方块更新

	使用`/log microtick`来启动记录

	末地烛会检测方块更新，红石元件会输出它们的动作

	- 侦测器、活塞、末地烛：指向羊毛块

	- 中继器、比较器、铁轨、按钮等：放置在羊毛块上

	默认值： `false`

	选项：`false`, `true`

	分类：command, creative

	## structureBlockLimit

	覆写结构方块的大小限制
	
	当相对位置的值大于32时客户端里结构的位置可能会错误地显示

	默认值： `32`

	选项：`32`, `64`, `96`, `128`

	分类：creative
	
	## optimizedInventories
    
    优化漏斗与投掷器跟箱子的互动
    
    感谢: skyrising (Quickcarpet)
    
    默认值: `false`
    
    选项: `false`, `true`
    
    分类: experimental, optimization

	## YEET

	**Warn**: all yeet options will change vanilla behaviour, they WILL NOT behave like vanilla

	**警告**：所有的yeet选项都会改变原版的特性，它们的行为不会表现得跟原版一致！

	### yeetFishAI

	去掉实体鱼造成巨大卡顿的followGroupLeaderAI

	默认值： `false`

	选项：`false`, `true`

	分类：yeet

	### yeetGolemSpawn

	跳过铁傀儡生成使小墨的14k刷铁塔堆叠更快

	默认值： `false`

	选项：`false`, `true`

	分类：yeet

	### yeetVillagerAi

	去掉部分村民ai使14k刷铁塔堆叠更快

	默认值： `false`

	选项：`false`, `true`

	分类：yeet

	------

	# 记录器

	- 保存记录器状态信息，以便在重启服务器后自动加载未关闭的记录器

	> `/log <LoggerName> [<Option>]`

	## projectiles

	增加撞击点显示

	选项：`brief`, `full`, `visualize`

	### visualize

	投掷物可视化记录器

	## chunkdebug

	区块加载/卸载记录器

	选项：无

	##  villagecount

	于tab列表中显示村庄数量

	选项：无

	## tileticklist

	TileTick(NTE)列表记录器，有助于制作TileTick EMP

	选项：无

	## tileentitylist

	方块实体列表记录器，记录方块实体从`World`的`loadedTileEntityList`中添加或删除

	选项：无

	## microtick

	使用羊毛方块来记录红石元件的动作以及方块更新。输入`/carpet microTick`以获得更多信息

	选项：`all`, `unique`

	### all

	输出所有信息，默认状态

	### unique

	仅输出不同的信息，如果你不想被红石粉刷屏的话可以开

	------

	# 修复

	对原版CarpetMod的修复

	- 修复了使用`/player`指令时没有限制名字长度的问题（过长的名字会使所有人都不能进入服务器）
	- 移除仙人掌扳手修改拉杆的功能