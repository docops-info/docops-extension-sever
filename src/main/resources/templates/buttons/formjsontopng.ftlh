<div>
    <h1>Create Buttons from JSON</h1>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/buttons/form/png" data-hx-target="#contentBox" >

        <div class="flex flex-col col-span-2">
        <label for="payload" class="mb-2">JSON</label>
        <textarea id="payload" name="payload" class="join-item textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox"></textarea>
        </div>
        <div class="flex flex-col">
            <button class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
        </div>
    </form>
</div>
<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>