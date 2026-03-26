let selected = new Set();

function loadApps() {
    let apps = JSON.parse(Android.getApps());
    let container = document.getElementById("appList");
    container.innerHTML = "";

    apps.forEach(app => {
        let div = document.createElement("div");
        div.className = "app " + (app.system ? "system" : "user");

        let checkbox = document.createElement("input");
        checkbox.type = "checkbox";

        checkbox.onchange = () => {
            if (checkbox.checked) selected.add(app.package);
            else selected.delete(app.package);
        };

        let label = document.createElement("span");
        label.innerText = app.name + " (" + app.package + ")";

        div.appendChild(checkbox);
        div.appendChild(label);
        container.appendChild(div);
    });
}

function uninstallSelected() {
    let status = document.getElementById("status");
    status.innerText = "Processing...";

    selected.forEach(pkg => {
        let result = Android.uninstall(pkg);
        console.log(pkg, result);
    });

    status.innerText = "Done";
}