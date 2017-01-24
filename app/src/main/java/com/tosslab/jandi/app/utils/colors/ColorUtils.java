package com.tosslab.jandi.app.utils.colors;


import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

public class ColorUtils {
    /**
     * <table class="colortable"><tbody><tr><th style="background:black">Named</th><th>Color&nbsp;name</th><th>Hex&nbsp;rgb</th></tr><tr><td class="c" style="background:#f0f8ff">&nbsp;</td><td><dfn id="aliceblue">aliceblue</dfn></td><td>#f0f8ff</td></tr><tr><td class="c" style="background:#faebd7">&nbsp;</td><td><dfn id="antiquewhite">antiquewhite</dfn></td><td>#faebd7</td></tr><tr><td class="c" style="background:#00ffff">&nbsp;</td><td><dfn id="aqua0">aqua</dfn></td><td>#00ffff</td></tr><tr><td class="c" style="background:#7fffd4">&nbsp;</td><td><dfn id="aquamarine">aquamarine</dfn></td><td>#7fffd4</td></tr><tr><td class="c" style="background:#f0ffff">&nbsp;</td><td><dfn id="azure">azure</dfn></td><td>#f0ffff</td></tr><tr><td class="c" style="background:#f5f5dc">&nbsp;</td><td><dfn id="beige">beige</dfn></td><td>#f5f5dc</td></tr><tr><td class="c" style="background:#ffe4c4">&nbsp;</td><td><dfn id="bisque">bisque</dfn></td><td>#ffe4c4</td></tr><tr><td class="c" style="background:#000000">&nbsp;</td><td><dfn id="black0">black</dfn></td><td>#000000</td></tr><tr><td class="c" style="background:#ffebcd">&nbsp;</td><td><dfn id="blanchedalmond">blanchedalmond</dfn></td><td>#ffebcd</td></tr><tr><td class="c" style="background:#0000ff">&nbsp;</td><td><dfn id="blue0">blue</dfn></td><td>#0000ff</td></tr><tr><td class="c" style="background:#8a2be2">&nbsp;</td><td><dfn id="blueviolet">blueviolet</dfn></td><td>#8a2be2</td></tr><tr><td class="c" style="background:#a52a2a">&nbsp;</td><td><dfn id="brown">brown</dfn></td><td>#a52a2a</td></tr><tr><td class="c" style="background:#deb887">&nbsp;</td><td><dfn id="burlywood">burlywood</dfn></td><td>#deb887</td></tr><tr><td class="c" style="background:#5f9ea0">&nbsp;</td><td><dfn id="cadetblue">cadetblue</dfn></td><td>#5f9ea0</td></tr><tr><td class="c" style="background:#7fff00">&nbsp;</td><td><dfn id="chartreuse">chartreuse</dfn></td><td>#7fff00</td></tr><tr><td class="c" style="background:#d2691e">&nbsp;</td><td><dfn id="chocolate">chocolate</dfn></td><td>#d2691e</td></tr><tr><td class="c" style="background:#ff7f50">&nbsp;</td><td><dfn id="coral">coral</dfn></td><td>#ff7f50</td></tr><tr><td class="c" style="background:#6495ed">&nbsp;</td><td><dfn id="cornflowerblue">cornflowerblue</dfn></td><td>#6495ed</td></tr><tr><td class="c" style="background:#fff8dc">&nbsp;</td><td><dfn id="cornsilk">cornsilk</dfn></td><td>#fff8dc</td></tr><tr><td class="c" style="background:#dc143c">&nbsp;</td><td><dfn id="crimson">crimson</dfn></td><td>#dc143c</td></tr><tr><td class="c" style="background:#00ffff">&nbsp;</td><td><dfn id="cyan">cyan</dfn></td><td>#00ffff</td></tr><tr><td class="c" style="background:#00008b">&nbsp;</td><td><dfn id="darkblue">darkblue</dfn></td><td>#00008b</td></tr><tr><td class="c" style="background:#008b8b">&nbsp;</td><td><dfn id="darkcyan">darkcyan</dfn></td><td>#008b8b</td></tr><tr><td class="c" style="background:#b8860b">&nbsp;</td><td><dfn id="darkgoldenrod">darkgoldenrod</dfn></td><td>#b8860b</td></tr><tr><td class="c" style="background:#a9a9a9">&nbsp;</td><td><dfn id="darkgray">darkgray</dfn></td><td>#a9a9a9</td></tr><tr><td class="c" style="background:#006400">&nbsp;</td><td><dfn id="darkgreen">darkgreen</dfn></td><td>#006400</td></tr><tr><td class="c" style="background:#a9a9a9">&nbsp;</td><td><dfn id="darkgrey">darkgrey</dfn></td><td>#a9a9a9</td></tr><tr><td class="c" style="background:#bdb76b">&nbsp;</td><td><dfn id="darkkhaki">darkkhaki</dfn></td><td>#bdb76b</td></tr><tr><td class="c" style="background:#8b008b">&nbsp;</td><td><dfn id="darkmagenta">darkmagenta</dfn></td><td>#8b008b</td></tr><tr><td class="c" style="background:#556b2f">&nbsp;</td><td><dfn id="darkolivegreen">darkolivegreen</dfn></td><td>#556b2f</td></tr><tr><td class="c" style="background:#ff8c00">&nbsp;</td><td><dfn id="darkorange">darkorange</dfn></td><td>#ff8c00</td></tr><tr><td class="c" style="background:#9932cc">&nbsp;</td><td><dfn id="darkorchid">darkorchid</dfn></td><td>#9932cc</td></tr><tr><td class="c" style="background:#8b0000">&nbsp;</td><td><dfn id="darkred">darkred</dfn></td><td>#8b0000</td></tr><tr><td class="c" style="background:#e9967a">&nbsp;</td><td><dfn id="darksalmon">darksalmon</dfn></td><td>#e9967a</td></tr><tr><td class="c" style="background:#8fbc8f">&nbsp;</td><td><dfn id="darkseagreen">darkseagreen</dfn></td><td>#8fbc8f</td></tr><tr><td class="c" style="background:#483d8b">&nbsp;</td><td><dfn id="darkslateblue">darkslateblue</dfn></td><td>#483d8b</td></tr><tr><td class="c" style="background:#2f4f4f">&nbsp;</td><td><dfn id="darkslategray">darkslategray</dfn></td><td>#2f4f4f</td></tr><tr><td class="c" style="background:#2f4f4f">&nbsp;</td><td><dfn id="darkslategrey">darkslategrey</dfn></td><td>#2f4f4f</td></tr><tr><td class="c" style="background:#00ced1">&nbsp;</td><td><dfn id="darkturquoise">darkturquoise</dfn></td><td>#00ced1</td></tr><tr><td class="c" style="background:#9400d3">&nbsp;</td><td><dfn id="darkviolet">darkviolet</dfn></td><td>#9400d3</td></tr><tr><td class="c" style="background:#ff1493">&nbsp;</td><td><dfn id="deeppink">deeppink</dfn></td><td>#ff1493</td></tr><tr><td class="c" style="background:#00bfff">&nbsp;</td><td><dfn id="deepskyblue">deepskyblue</dfn></td><td>#00bfff</td></tr><tr><td class="c" style="background:#696969">&nbsp;</td><td><dfn id="dimgray">dimgray</dfn></td><td>#696969</td></tr><tr><td class="c" style="background:#696969">&nbsp;</td><td><dfn id="dimgrey">dimgrey</dfn></td><td>#696969</td></tr><tr><td class="c" style="background:#1e90ff">&nbsp;</td><td><dfn id="dodgerblue">dodgerblue</dfn></td><td>#1e90ff</td></tr><tr><td class="c" style="background:#b22222">&nbsp;</td><td><dfn id="firebrick">firebrick</dfn></td><td>#b22222</td></tr><tr><td class="c" style="background:#fffaf0">&nbsp;</td><td><dfn id="floralwhite">floralwhite</dfn></td><td>#fffaf0</td></tr><tr><td class="c" style="background:#228b22">&nbsp;</td><td><dfn id="forestgreen">forestgreen</dfn></td><td>#228b22</td></tr><tr><td class="c" style="background:#ff00ff">&nbsp;</td><td><dfn id="fuchsia0">fuchsia</dfn></td><td>#ff00ff</td></tr><tr><td class="c" style="background:#dcdcdc">&nbsp;</td><td><dfn id="gainsboro">gainsboro</dfn></td><td>#dcdcdc</td></tr><tr><td class="c" style="background:#f8f8ff">&nbsp;</td><td><dfn id="ghostwhite">ghostwhite</dfn></td><td>#f8f8ff</td></tr><tr><td class="c" style="background:#ffd700">&nbsp;</td><td><dfn id="gold">gold</dfn></td><td>#ffd700</td></tr><tr><td class="c" style="background:#daa520">&nbsp;</td><td><dfn id="goldenrod">goldenrod</dfn></td><td>#daa520</td></tr><tr><td class="c" style="background:#808080">&nbsp;</td><td><dfn id="gray0">gray</dfn></td><td>#808080</td></tr><tr><td class="c" style="background:#008000">&nbsp;</td><td><dfn id="green0">green</dfn></td><td>#008000</td></tr><tr><td class="c" style="background:#adff2f">&nbsp;</td><td><dfn id="greenyellow">greenyellow</dfn></td><td>#adff2f</td></tr><tr><td class="c" style="background:#808080">&nbsp;</td><td><dfn id="grey">grey</dfn></td><td>#808080</td></tr><tr><td class="c" style="background:#f0fff0">&nbsp;</td><td><dfn id="honeydew">honeydew</dfn></td><td>#f0fff0</td></tr><tr><td class="c" style="background:#ff69b4">&nbsp;</td><td><dfn id="hotpink">hotpink</dfn></td><td>#ff69b4</td></tr><tr><td class="c" style="background:#cd5c5c">&nbsp;</td><td><dfn id="indianred">indianred</dfn></td><td>#cd5c5c</td></tr><tr><td class="c" style="background:#4b0082">&nbsp;</td><td><dfn id="indigo">indigo</dfn></td><td>#4b0082</td></tr><tr><td class="c" style="background:#fffff0">&nbsp;</td><td><dfn id="ivory">ivory</dfn></td><td>#fffff0</td></tr><tr><td class="c" style="background:#f0e68c">&nbsp;</td><td><dfn id="khaki">khaki</dfn></td><td>#f0e68c</td></tr><tr><td class="c" style="background:#e6e6fa">&nbsp;</td><td><dfn id="lavender">lavender</dfn></td><td>#e6e6fa</td></tr><tr><td class="c" style="background:#fff0f5">&nbsp;</td><td><dfn id="lavenderblush">lavenderblush</dfn></td><td>#fff0f5</td></tr><tr><td class="c" style="background:#7cfc00">&nbsp;</td><td><dfn id="lawngreen">lawngreen</dfn></td><td>#7cfc00</td></tr><tr><td class="c" style="background:#fffacd">&nbsp;</td><td><dfn id="lemonchiffon">lemonchiffon</dfn></td><td>#fffacd</td></tr><tr><td class="c" style="background:#add8e6">&nbsp;</td><td><dfn id="lightblue">lightblue</dfn></td><td>#add8e6</td></tr><tr><td class="c" style="background:#f08080">&nbsp;</td><td><dfn id="lightcoral">lightcoral</dfn></td><td>#f08080</td></tr><tr><td class="c" style="background:#e0ffff">&nbsp;</td><td><dfn id="lightcyan">lightcyan</dfn></td><td>#e0ffff</td></tr><tr><td class="c" style="background:#fafad2">&nbsp;</td><td><dfn id="lightgoldenrodyellow">lightgoldenrodyellow</dfn></td><td>#fafad2</td></tr><tr><td class="c" style="background:#d3d3d3">&nbsp;</td><td><dfn id="lightgray">lightgray</dfn></td><td>#d3d3d3</td></tr><tr><td class="c" style="background:#90ee90">&nbsp;</td><td><dfn id="lightgreen">lightgreen</dfn></td><td>#90ee90</td></tr><tr><td class="c" style="background:#d3d3d3">&nbsp;</td><td><dfn id="lightgrey">lightgrey</dfn></td><td>#d3d3d3</td></tr><tr><td class="c" style="background:#ffb6c1">&nbsp;</td><td><dfn id="lightpink">lightpink</dfn></td><td>#ffb6c1</td></tr><tr><td class="c" style="background:#ffa07a">&nbsp;</td><td><dfn id="lightsalmon">lightsalmon</dfn></td><td>#ffa07a</td></tr><tr><td class="c" style="background:#20b2aa">&nbsp;</td><td><dfn id="lightseagreen">lightseagreen</dfn></td><td>#20b2aa</td></tr><tr><td class="c" style="background:#87cefa">&nbsp;</td><td><dfn id="lightskyblue">lightskyblue</dfn></td><td>#87cefa</td></tr><tr><td class="c" style="background:#778899">&nbsp;</td><td><dfn id="lightslategray">lightslategray</dfn></td><td>#778899</td></tr><tr><td class="c" style="background:#778899">&nbsp;</td><td><dfn id="lightslategrey">lightslategrey</dfn></td><td>#778899</td></tr><tr><td class="c" style="background:#b0c4de">&nbsp;</td><td><dfn id="lightsteelblue">lightsteelblue</dfn></td><td>#b0c4de</td></tr><tr><td class="c" style="background:#ffffe0">&nbsp;</td><td><dfn id="lightyellow">lightyellow</dfn></td><td>#ffffe0</td></tr><tr><td class="c" style="background:#00ff00">&nbsp;</td><td><dfn id="lime0">lime</dfn></td><td>#00ff00</td></tr><tr><td class="c" style="background:#32cd32">&nbsp;</td><td><dfn id="limegreen">limegreen</dfn></td><td>#32cd32</td></tr><tr><td class="c" style="background:#faf0e6">&nbsp;</td><td><dfn id="linen">linen</dfn></td><td>#faf0e6</td></tr><tr><td class="c" style="background:#ff00ff">&nbsp;</td><td><dfn id="magenta">magenta</dfn></td><td>#ff00ff</td></tr><tr><td class="c" style="background:#800000">&nbsp;</td><td><dfn id="maroon0">maroon</dfn></td><td>#800000</td></tr><tr><td class="c" style="background:#66cdaa">&nbsp;</td><td><dfn id="mediumaquamarine">mediumaquamarine</dfn></td><td>#66cdaa</td></tr><tr><td class="c" style="background:#0000cd">&nbsp;</td><td><dfn id="mediumblue">mediumblue</dfn></td><td>#0000cd</td></tr><tr><td class="c" style="background:#ba55d3">&nbsp;</td><td><dfn id="mediumorchid">mediumorchid</dfn></td><td>#ba55d3</td></tr><tr><td class="c" style="background:#9370db">&nbsp;</td><td><dfn id="mediumpurple">mediumpurple</dfn></td><td>#9370db</td></tr><tr><td class="c" style="background:#3cb371">&nbsp;</td><td><dfn id="mediumseagreen">mediumseagreen</dfn></td><td>#3cb371</td></tr><tr><td class="c" style="background:#7b68ee">&nbsp;</td><td><dfn id="mediumslateblue">mediumslateblue</dfn></td><td>#7b68ee</td></tr><tr><td class="c" style="background:#00fa9a">&nbsp;</td><td><dfn id="mediumspringgreen">mediumspringgreen</dfn></td><td>#00fa9a</td></tr><tr><td class="c" style="background:#48d1cc">&nbsp;</td><td><dfn id="mediumturquoise">mediumturquoise</dfn></td><td>#48d1cc</td></tr><tr><td class="c" style="background:#c71585">&nbsp;</td><td><dfn id="mediumvioletred">mediumvioletred</dfn></td><td>#c71585</td></tr><tr><td class="c" style="background:#191970">&nbsp;</td><td><dfn id="midnightblue">midnightblue</dfn></td><td>#191970</td></tr><tr><td class="c" style="background:#f5fffa">&nbsp;</td><td><dfn id="mintcream">mintcream</dfn></td><td>#f5fffa</td></tr><tr><td class="c" style="background:#ffe4e1">&nbsp;</td><td><dfn id="mistyrose">mistyrose</dfn></td><td>#ffe4e1</td></tr><tr><td class="c" style="background:#ffe4b5">&nbsp;</td><td><dfn id="moccasin">moccasin</dfn></td><td>#ffe4b5</td></tr><tr><td class="c" style="background:#ffdead">&nbsp;</td><td><dfn id="navajowhite">navajowhite</dfn></td><td>#ffdead</td></tr><tr><td class="c" style="background:#000080">&nbsp;</td><td><dfn id="navy0">navy</dfn></td><td>#000080</td></tr><tr><td class="c" style="background:#fdf5e6">&nbsp;</td><td><dfn id="oldlace">oldlace</dfn></td><td>#fdf5e6</td></tr><tr><td class="c" style="background:#808000">&nbsp;</td><td><dfn id="olive0">olive</dfn></td><td>#808000</td></tr><tr><td class="c" style="background:#6b8e23">&nbsp;</td><td><dfn id="olivedrab">olivedrab</dfn></td><td>#6b8e23</td></tr><tr><td class="c" style="background:#ffa500">&nbsp;</td><td><dfn id="orange">orange</dfn></td><td>#ffa500</td></tr><tr><td class="c" style="background:#ff4500">&nbsp;</td><td><dfn id="orangered">orangered</dfn></td><td>#ff4500</td></tr><tr><td class="c" style="background:#da70d6">&nbsp;</td><td><dfn id="orchid">orchid</dfn></td><td>#da70d6</td></tr><tr><td class="c" style="background:#eee8aa">&nbsp;</td><td><dfn id="palegoldenrod">palegoldenrod</dfn></td><td>#eee8aa</td></tr><tr><td class="c" style="background:#98fb98">&nbsp;</td><td><dfn id="palegreen">palegreen</dfn></td><td>#98fb98</td></tr><tr><td class="c" style="background:#afeeee">&nbsp;</td><td><dfn id="paleturquoise">paleturquoise</dfn></td><td>#afeeee</td></tr><tr><td class="c" style="background:#db7093">&nbsp;</td><td><dfn id="palevioletred">palevioletred</dfn></td><td>#db7093</td></tr><tr><td class="c" style="background:#ffefd5">&nbsp;</td><td><dfn id="papayawhip">papayawhip</dfn></td><td>#ffefd5</td></tr><tr><td class="c" style="background:#ffdab9">&nbsp;</td><td><dfn id="peachpuff">peachpuff</dfn></td><td>#ffdab9</td></tr><tr><td class="c" style="background:#cd853f">&nbsp;</td><td><dfn id="peru">peru</dfn></td><td>#cd853f</td></tr><tr><td class="c" style="background:#ffc0cb">&nbsp;</td><td><dfn id="pink">pink</dfn></td><td>#ffc0cb</td></tr><tr><td class="c" style="background:#dda0dd">&nbsp;</td><td><dfn id="plum">plum</dfn></td><td>#dda0dd</td></tr><tr><td class="c" style="background:#b0e0e6">&nbsp;</td><td><dfn id="powderblue">powderblue</dfn></td><td>#b0e0e6</td></tr><tr><td class="c" style="background:#800080">&nbsp;</td><td><dfn id="purple0">purple</dfn></td><td>#800080</td></tr><tr><td class="c" style="background:#663399">&nbsp;</td><td><dfn id="rebeccapurple">rebeccapurple</dfn></td><td>#663399</td></tr><tr><td class="c" style="background:#ff0000">&nbsp;</td><td><dfn id="red0">red</dfn></td><td>#ff0000</td></tr><tr><td class="c" style="background:#bc8f8f">&nbsp;</td><td><dfn id="rosybrown">rosybrown</dfn></td><td>#bc8f8f</td></tr><tr><td class="c" style="background:#4169e1">&nbsp;</td><td><dfn id="royalblue">royalblue</dfn></td><td>#4169e1</td></tr><tr><td class="c" style="background:#8b4513">&nbsp;</td><td><dfn id="saddlebrown">saddlebrown</dfn></td><td>#8b4513</td></tr><tr><td class="c" style="background:#fa8072">&nbsp;</td><td><dfn id="salmon">salmon</dfn></td><td>#fa8072</td></tr><tr><td class="c" style="background:#f4a460">&nbsp;</td><td><dfn id="sandybrown">sandybrown</dfn></td><td>#f4a460</td></tr><tr><td class="c" style="background:#2e8b57">&nbsp;</td><td><dfn id="seagreen">seagreen</dfn></td><td>#2e8b57</td></tr><tr><td class="c" style="background:#fff5ee">&nbsp;</td><td><dfn id="seashell">seashell</dfn></td><td>#fff5ee</td></tr><tr><td class="c" style="background:#a0522d">&nbsp;</td><td><dfn id="sienna">sienna</dfn></td><td>#a0522d</td></tr><tr><td>&nbsp;<p></td><td class="c" style="background:#c0c0c0">&nbsp;</td><td><dfn id="silver0">silver</dfn></td><td>#c0c0c0</td></tr><tr><td class="c" style="background:#87ceeb">&nbsp;</td><td><dfn id="skyblue">skyblue</dfn></td><td>#87ceeb</td></tr><tr><td class="c" style="background:#6a5acd">&nbsp;</td><td><dfn id="slateblue">slateblue</dfn></td><td>#6a5acd</td></tr><tr><td class="c" style="background:#708090">&nbsp;</td><td><dfn id="slategray">slategray</dfn></td><td>#708090</td></tr><tr><td class="c" style="background:#708090">&nbsp;</td><td><dfn id="slategrey">slategrey</dfn></td><td>#708090</td></tr><tr><td class="c" style="background:#fffafa">&nbsp;</td><td><dfn id="snow">snow</dfn></td><td>#fffafa</td></tr><tr><td class="c" style="background:#00ff7f">&nbsp;</td><td><dfn id="springgreen">springgreen</dfn></td><td>#00ff7f</td></tr><tr><td class="c" style="background:#4682b4">&nbsp;</td><td><dfn id="steelblue">steelblue</dfn></td><td>#4682b4</td></tr><tr><td class="c" style="background:#d2b48c">&nbsp;</td><td><dfn id="tan">tan</dfn></td><td>#d2b48c</td></tr><tr><td class="c" style="background:#008080">&nbsp;</td><td><dfn id="teal0">teal</dfn></td><td>#008080</td></tr><tr><td class="c" style="background:#d8bfd8">&nbsp;</td><td><dfn id="thistle">thistle</dfn></td><td>#d8bfd8</td></tr><tr><td class="c" style="background:#ff6347">&nbsp;</td><td><dfn id="tomato">tomato</dfn></td><td>#ff6347</td></tr><tr><td class="c" style="background:#40e0d0">&nbsp;</td><td><dfn id="turquoise">turquoise</dfn></td><td>#40e0d0</td></tr><tr><td class="c" style="background:#ee82ee">&nbsp;</td><td><dfn id="violet">violet</dfn></td><td>#ee82ee</td></tr><tr><td class="c" style="background:#f5deb3">&nbsp;</td><td><dfn id="wheat">wheat</dfn></td><td>#f5deb3</td></tr><tr><td class="c" style="background:#ffffff">&nbsp;</td><td><dfn id="white0">white</dfn></td><td>#ffffff</td></tr><tr><td class="c" style="background:#f5f5f5">&nbsp;</td><td><dfn id="whitesmoke">whitesmoke</dfn></td><td>#f5f5f5</td></tr><tr><td class="c" style="background:#ffff00">&nbsp;</td><td><dfn id="yellow0">yellow</dfn></td><td>#ffff00</td></tr><tr><td class="c" style="background:#9acd32">&nbsp;</td><td><dfn id="yellowgreen">yellowgreen</dfn></td><td>#9acd32</td></tr></tbody></table>
     */
    private static final SimpleArrayMap<String, Integer> colorMap = new SimpleArrayMap<>();

    static {
        // https://www.w3.org/TR/css3-color/#svg-color
        colorMap.put("transparent", 0x0);
        colorMap.put("aliceblue", 0xFFF0F8FF);
        colorMap.put("antiquewhite", 0xFFFAEBD7);
        colorMap.put("aqua", 0xFF00FFFF);
        colorMap.put("aquamarine", 0xFF7FFFD4);
        colorMap.put("azure", 0xFFF0FFFF);
        colorMap.put("beige", 0xFFF5F5DC);
        colorMap.put("bisque", 0xFFFFE4C4);
        colorMap.put("black", 0xFF000000);
        colorMap.put("blanchedalmond", 0xFFFFEBCD);
        colorMap.put("blue", 0xFF0000FF);
        colorMap.put("blueviolet", 0xFF8A2BE2);
        colorMap.put("brown", 0xFFA52A2A);
        colorMap.put("burlywood", 0xFFDEB887);
        colorMap.put("cadetblue", 0xFF5F9EA0);
        colorMap.put("chartreuse", 0xFF7FFF00);
        colorMap.put("chocolate", 0xFFD2691E);
        colorMap.put("coral", 0xFFFF7F50);
        colorMap.put("cornflowerblue", 0xFF6495ED);
        colorMap.put("cornsilk", 0xFFFFF8DC);
        colorMap.put("crimson", 0xFFDC143C);
        colorMap.put("cyan", 0xFF00FFFF);
        colorMap.put("darkblue", 0xFF00008B);
        colorMap.put("darkcyan", 0xFF008B8B);
        colorMap.put("darkgoldenrod", 0xFFB8860B);
        colorMap.put("darkgray", 0xFFA9A9A9);
        colorMap.put("darkgreen", 0xFF006400);
        colorMap.put("darkgrey", 0xFFA9A9A9);
        colorMap.put("darkkhaki", 0xFFBDB76B);
        colorMap.put("darkmagenta", 0xFF8B008B);
        colorMap.put("darkolivegreen", 0xFF556B2F);
        colorMap.put("darkorange", 0xFFFF8C00);
        colorMap.put("darkorchid", 0xFF9932CC);
        colorMap.put("darkred", 0xFF8B0000);
        colorMap.put("darksalmon", 0xFFE9967A);
        colorMap.put("darkseagreen", 0xFF8FBC8F);
        colorMap.put("darkslateblue", 0xFF483D8B);
        colorMap.put("darkslategray", 0xFF2F4F4F);
        colorMap.put("darkslategrey", 0xFF2F4F4F);
        colorMap.put("darkturquoise", 0xFF00CED1);
        colorMap.put("darkviolet", 0xFF9400D3);
        colorMap.put("deeppink", 0xFFFF1493);
        colorMap.put("deepskyblue", 0xFF00BFFF);
        colorMap.put("dimgray", 0xFF696969);
        colorMap.put("dimgrey", 0xFF696969);
        colorMap.put("dodgerblue", 0xFF1E90FF);
        colorMap.put("firebrick", 0xFFB22222);
        colorMap.put("floralwhite", 0xFFFFFAF0);
        colorMap.put("forestgreen", 0xFF228B22);
        colorMap.put("fuchsia", 0xFFFF00FF);
        colorMap.put("gainsboro", 0xFFDCDCDC);
        colorMap.put("ghostwhite", 0xFFF8F8FF);
        colorMap.put("gold", 0xFFFFD700);
        colorMap.put("goldenrod", 0xFFDAA520);
        colorMap.put("gray", 0xFF808080);
        colorMap.put("green", 0xFF008000);
        colorMap.put("greenyellow", 0xFFADFF2F);
        colorMap.put("grey", 0xFF808080);
        colorMap.put("honeydew", 0xFFF0FFF0);
        colorMap.put("hotpink", 0xFFFF69B4);
        colorMap.put("indianred", 0xFFCD5C5C);
        colorMap.put("indigo", 0xFF4B0082);
        colorMap.put("ivory", 0xFFFFFFF0);
        colorMap.put("khaki", 0xFFF0E68C);
        colorMap.put("lavender", 0xFFE6E6FA);
        colorMap.put("lavenderblush", 0xFFFFF0F5);
        colorMap.put("lawngreen", 0xFF7CFC00);
        colorMap.put("lemonchiffon", 0xFFFFFACD);
        colorMap.put("lightblue", 0xFFADD8E6);
        colorMap.put("lightcoral", 0xFFF08080);
        colorMap.put("lightcyan", 0xFFE0FFFF);
        colorMap.put("lightgoldenrodyellow", 0xFFFAFAD2);
        colorMap.put("lightgray", 0xFFD3D3D3);
        colorMap.put("lightgreen", 0xFF90EE90);
        colorMap.put("lightgrey", 0xFFD3D3D3);
        colorMap.put("lightpink", 0xFFFFB6C1);
        colorMap.put("lightsalmon", 0xFFFFA07A);
        colorMap.put("lightseagreen", 0xFF20B2AA);
        colorMap.put("lightskyblue", 0xFF87CEFA);
        colorMap.put("lightslategray", 0xFF778899);
        colorMap.put("lightslategrey", 0xFF778899);
        colorMap.put("lightsteelblue", 0xFFB0C4DE);
        colorMap.put("lightyellow", 0xFFFFFFE0);
        colorMap.put("lime", 0xFF00FF00);
        colorMap.put("limegreen", 0xFF32CD32);
        colorMap.put("linen", 0xFFFAF0E6);
        colorMap.put("magenta", 0xFFFF00FF);
        colorMap.put("maroon", 0xFF800000);
        colorMap.put("mediumaquamarine", 0xFF66CDAA);
        colorMap.put("mediumblue", 0xFF0000CD);
        colorMap.put("mediumorchid", 0xFFBA55D3);
        colorMap.put("mediumpurple", 0xFF9370DB);
        colorMap.put("mediumseagreen", 0xFF3CB371);
        colorMap.put("mediumslateblue", 0xFF7B68EE);
        colorMap.put("mediumspringgreen", 0xFF00FA9A);
        colorMap.put("mediumturquoise", 0xFF48D1CC);
        colorMap.put("mediumvioletred", 0xFFC71585);
        colorMap.put("midnightblue", 0xFF191970);
        colorMap.put("mintcream", 0xFFF5FFFA);
        colorMap.put("mistyrose", 0xFFFFE4E1);
        colorMap.put("moccasin", 0xFFFFE4B5);
        colorMap.put("navajowhite", 0xFFFFDEAD);
        colorMap.put("navy", 0xFF000080);
        colorMap.put("oldlace", 0xFFFDF5E6);
        colorMap.put("olive", 0xFF808000);
        colorMap.put("olivedrab", 0xFF6B8E23);
        colorMap.put("orange", 0xFFFFA500);
        colorMap.put("orangered", 0xFFFF4500);
        colorMap.put("orchid", 0xFFDA70D6);
        colorMap.put("palegoldenrod", 0xFFEEE8AA);
        colorMap.put("palegreen", 0xFF98FB98);
        colorMap.put("paleturquoise", 0xFFAFEEEE);
        colorMap.put("palevioletred", 0xFFDB7093);
        colorMap.put("papayawhip", 0xFFFFEFD5);
        colorMap.put("peachpuff", 0xFFFFDAB9);
        colorMap.put("peru", 0xFFCD853F);
        colorMap.put("pink", 0xFFFFC0CB);
        colorMap.put("plum", 0xFFDDA0DD);
        colorMap.put("powderblue", 0xFFB0E0E6);
        colorMap.put("purple", 0xFF800080);
        colorMap.put("rebeccapurple", 0xFF663399);
        colorMap.put("red", 0xFFFF0000);
        colorMap.put("rosybrown", 0xFFBC8F8F);
        colorMap.put("royalblue", 0xFF4169E1);
        colorMap.put("saddlebrown", 0xFF8B4513);
        colorMap.put("salmon", 0xFFFA8072);
        colorMap.put("sandybrown", 0xFFF4A460);
        colorMap.put("seagreen", 0xFF2E8B57);
        colorMap.put("seashell", 0xFFFFF5EE);
        colorMap.put("sienna", 0xFFA0522D);
        colorMap.put("silver", 0xFFC0C0C0);
        colorMap.put("skyblue", 0xFF87CEEB);
        colorMap.put("slateblue", 0xFF6A5ACD);
        colorMap.put("slategray", 0xFF708090);
        colorMap.put("slategrey", 0xFF708090);
        colorMap.put("snow", 0xFFFFFAFA);
        colorMap.put("springgreen", 0xFF00FF7F);
        colorMap.put("steelblue", 0xFF4682B4);
        colorMap.put("tan", 0xFFD2B48C);
        colorMap.put("teal", 0xFF008080);
        colorMap.put("thistle", 0xFFD8BFD8);
        colorMap.put("tomato", 0xFFFF6347);
        colorMap.put("turquoise", 0xFF40E0D0);
        colorMap.put("violet", 0xFFEE82EE);
        colorMap.put("wheat", 0xFFF5DEB3);
        colorMap.put("white", 0xFFFFFFFF);
        colorMap.put("whitesmoke", 0xFFF5F5F5);
        colorMap.put("yellow", 0xFFFFFF00);
        colorMap.put("yellowgreen", 0xFF9ACD32);
    }

    @ColorInt
    public static int parseColor(String colorString) {
        if (TextUtils.isEmpty(colorString)) {
            return Color.TRANSPARENT;
        }

        if (colorString.startsWith("#")) {
            // reference : android.graphics.Color#parseColor()
            int length = colorString.length();
            if (length == 7 || length == 9) {
                return (int) parsingHexcode(colorString.substring(1));
            } else if (length == 4 || length == 5) {
                String substring = colorString.substring(1);
                StringBuilder builder = new StringBuilder();
                for (int idx = 0; idx < substring.length(); idx++) {
                    char ch = substring.charAt(idx);
                    builder.append(ch).append(ch);
                }
                return (int) parsingHexcode(builder.toString());
            } else {
                return Color.TRANSPARENT;
            }
        } else if (colorMap.containsKey(colorString.toLowerCase())) {
            return colorMap.get(colorString);
        } else {
            return Color.TRANSPARENT;
        }
    }

    private static long parsingHexcode(String hexcode) {
        try {
            long color = Long.parseLong(hexcode, 16);
            if (hexcode.length() == 6) {
                // Set the alpha value
                color |= 0x00000000ff000000;
            }
            return color;
        } catch (NumberFormatException e) {
            return Color.TRANSPARENT;
        }
    }
}
