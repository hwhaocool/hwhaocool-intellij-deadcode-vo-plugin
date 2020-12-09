intellij-deadcode-vo-plugin
---

## 作用
can find unused VO in recursion mode


使用递归模式尽可能的设置一些`VO`类状态为『可达』， 和原生插件『unused declaration』的不同是：  
原生插件会尽可能多的提醒  
本插件是： `宁放过，勿杀错。`  

扫描出来的东西绝对是 `dead code`， 可以放心的 `Safe Delete` 

## Usages
Preferences | Editor | Inspections | Java | Probable Bugs

使用： Analyze -- Run Inspection By Name -- Yellow Tail

注意： 本插件，只适合用来扫描 `VO` 类型的 class， 像 `spring service`等class 不太适合

如果有这方面的需求，还是使用自带的插件 `unused declaration` 比较好