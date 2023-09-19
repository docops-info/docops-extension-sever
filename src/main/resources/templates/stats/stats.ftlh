<h1>Usage Metrics View - Per JVM</h1>
<script>

    var updateMetrics = function () {
        let httpRequest = new XMLHttpRequest();
        httpRequest.open("GET", "actuator/metrics");
        httpRequest.send();
        httpRequest.onreadystatechange = function(){
            if (httpRequest.readyState === XMLHttpRequest.DONE) {
                if (httpRequest.status === 200) {
                    const items = JSON.parse(this.responseText);
                    let select = document.getElementById("metrics");
                    var itemArray = items["names"];
                    for(var i = 0; i < itemArray.length; i++) {
                        var opt = itemArray[i];
                        var el = document.createElement("option");
                        el.textContent = opt;
                        el.value = opt;
                        select.appendChild(el);
                    }

                } else {
                    alert("There was a problem with the request.");
                }
            }
        };
    };
    updateMetrics();

    var getMetrics = function () {
        let select = document.getElementById("metrics");
        //makeRequest("/"+select.value, 'panel')
        htmx.ajax("GET", "actuator/metrics"+"/"+select.value, '#output').then(() => {
            var content = document.getElementById("output")
            htmx.ajax("GET", "api/jsonToSvg?payload="+encodeURI(content.innerHTML), {target: "#panel"});
        });
    }

</script>
<table class="pure-table">
    <thead>
    <tr>
        <th><label for="metrics"></label></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <div>
                <select id="metrics" name="metrics"
                        class="select select-primary" onchange="getMetrics()">
                    <option>Select</option>
                </select>
            </div>
            <div id="output" style="display: none"></div>
        </td>
    </tr>

    </tbody>
</table>

<hr/>
<h3>Results</h3>

<div id="panel"></div>

