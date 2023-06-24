let fallbackCopyTextToClipboard = function (text) {
    let textArea = document.createElement("textarea");
    textArea.value = text;

    // Avoid scrolling to bottom
    textArea.style.top = "0";
    textArea.style.left = "0";
    textArea.style.position = "fixed";

    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        let successful = document.execCommand('copy');
        let msg = successful ? 'successful' : 'unsuccessful';
        console.log('Fallback: Copying text command was ' + msg);
    } catch (err) {
        console.error('Fallback: Oops, unable to copy', err);
    }

    document.body.removeChild(textArea);
};
let copyTextToClipboard = function (text) {
    if (!navigator.clipboard) {
        fallbackCopyTextToClipboard(text);
        return;
    }
    navigator.clipboard.writeText(text).then(function () {
        console.log('Async: Copying to clipboard was successful!');
    }, function (err) {
        console.error('Async: Could not copy text: ', err);
    });
}
let makeAdoc = function (text) {
    var str = "[echart]\n";
    str += "----\n";
    str += text;
    str += "\n----\n";
    return str;
};


let  handleErrors = function(response) {
    if (!response.ok) {
        throw Error(response.statusText);
    }
    return response;
};

let getImage = function(path, div) {
    fetch(path,
        {
            method: "POST",
            body: editor.getValue()
        })
        .then(handleErrors)
        .then(response => {
            response.text().then(function (text) {
                $('#'+div).html('').html(text);
            });
        }).catch(error => console.log(error));
};

document.addEventListener('click', function(event) {
    let id = event.target.dataset.toggleId;
    if (!id) return;

    let elem = document.getElementById(id);
    elem.checked = !elem.checked;
    let btnState = document.getElementById(id + "I");
    btnState.classList.toggle("activecheck");
    btnState.classList.toggle("button-secondary");
    btnState.classList.toggle("inset-btn");
});

function downloadSVGAsText(content) {
    const a = document.createElement('a');
    const e = new MouseEvent('click');
    a.download = 'strategy.svg';
    a.href = 'data:image/svg+xml;base64,' + content;
    a.dispatchEvent(e);
}
var myChart;
