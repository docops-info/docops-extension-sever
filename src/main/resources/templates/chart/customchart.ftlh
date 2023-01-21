<h1>My Own EChart</h1>
<div>
    <p>I would like my own <a href="https://echarts.apache.org">EChart</a>
        Grab your EChart and put the content in the editor bar.
        Server will take care of creating div and initializing chart</p>
</div>

<button class="button-success pure-button"   onclick="copyTextToClipboard(editor.getValue());">Copy</button>
<button class="button-secondary pure-button" onclick="copyTextToClipboard(makeAdoc(editor.getValue()))">Copy as AsciiDoc</button>
<button class="button-secondary pure-button" onclick="downloadBlob('snippet.kts')">Download Snippet</button>
<br/>
<br/>
<div class="pure-g">
    <div class="pure-u-8-24">
<pre id="editor">
var option;

option = {
  dataset: {
    source: [
      ['score', 'amount', 'product'],
      [89.3, 58212, 'Matcha Latte'],
      [57.1, 78254, 'Milk Tea'],
      [74.4, 41032, 'Cheese Cocoa'],
      [50.1, 12755, 'Cheese Brownie'],
      [89.7, 20145, 'Matcha Cocoa'],
      [68.1, 79146, 'Tea'],
      [19.6, 91852, 'Orange Juice'],
      [10.6, 101852, 'Lemon Juice'],
      [32.7, 20112, 'Walnut Brownie']
    ]
  },
  grid: { containLabel: true },
  xAxis: { name: 'amount' },
  yAxis: { type: 'category' },
  visualMap: {
    orient: 'horizontal',
    left: 'center',
    min: 10,
    max: 100,
    text: ['High Score', 'Low Score'],
    // Map the score column to color
    dimension: 0,
    inRange: {
      color: ['#65B581', '#FFCE34', '#FD665F']
    }
  },
  series: [
    {
      type: 'bar',
      encode: {
        // Map the "amount" column to X axis.
        x: 'amount',
        // Map the "product" column to Y axis
        y: 'product'
      }
    }
  ]
};

option && myChart.setOption(option);
</pre>
    </div>
    <div class="pure-u-16-24">
        <h3>Preview</h3>
        <div id="results"></div>
    </div>
</div>

<script>
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/clouds");
    editor.session.setMode("ace/mode/javascript");
    //editor.session.setMode("ace/mode/html");
    editor.setOptions({
        maxLines: 50
    });

    // or use data: url to handle things like doctype
    function showHTMLInIFrame() {
        getImage("api/chart/custom?width=900&height=500", 'results');
    }
    editor.on("input", showHTMLInIFrame);
</script>
<#include "chartcommon.ftlh">
<@chartCommon chart = "" treechart = "" stacked = "" mychart = "pure-button-primary pure-button-active"/>


