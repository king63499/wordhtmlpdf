
<b>增加字体</b>

1.添加字体需要打开DemoApplication.java的以下被注释代码：

String[] parameters = {
				"-d",
				"-ttcname",
				"calibri",
				"calibri.ttf", "calibri.xml", };
TTFReader.main(parameters);

2.运行上面代码之前把maven中的fop包挪到最上面。否则因为fop和spring boot都引入了同一个包但版本不同而发生错误。

3.生成xml文件（上例中的calibri.xml）以后挪到conf文件夹里,修改fop.xconf中的相关部分。

4.粗体字要有单独的粗体字字体，仿宋粗体用的是网上下载的“华文仿宋粗体”，对应配置文件里的“simfangbd”，不是免费字体需要注意版权问题。

5.运行完之后需要恢复pom文件包的引入顺序。

<b>其他</b>

6.链接只有/hello6可用，其他都是尝试各种方案的半成品。itext需要熟悉大量专有API，html转pdf效果不那么理想。费劲做出HTML以后能否原样转成pdf是个问题。
相对来说FOP只需要了解简单的xsl-fo语法就可以，并且表格，字体，页眉页脚，脚注等功能都能实现。 

7.打包启动的时候要加上-Dfile.encoding=utf-8 选项，暂时还不知道如何程序上解决这个问题。

<b>参考链接</b>

https://xmlgraphics.apache.org/

http://www.w3school.com.cn/xslfo/xslfo_pages.asp 

这是fo参考教程，尤其要注意页面布局这一块，这个跟word的布局不太一样。所以fo文件的margin值跟word也不完全一样。

