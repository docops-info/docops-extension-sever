<script>
    function checkUserKeydown(event) {
        return event instanceof KeyboardEvent
    }
</script>


<div>
    <form>
<label for="search">Search:</label>
<input class="p-2 border rounded-md" id="search"
        list="wordList"
        name="wordInput"
        autocomplete="off"
        hx-get="api/typeahead"
        hx-target="#wordList"
        hx-trigger="keyup[checkUserKeydown.call(this, event)] changed delay:25ms"
></input>
    </form>
</div>
<table class="table">
    <tr class="table-header">
        <td class="table-cell">Search</td>
        <td class="table-cell">Result</td>
    </tr>
    <tbody id="wordList"></tbody>
</table>
<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>
