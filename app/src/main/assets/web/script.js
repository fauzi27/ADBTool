function test() {
    Android.showToast("Hello dari WebView 🔥");
}

function loadApps() {
    let apps = JSON.parse(Android.getApps());
    let list = document.getElementById("list");
    list.innerHTML = "";

    apps.forEach(app => {
        let li = document.createElement("li");
        li.innerText = app;

        li.onclick = () => {
            Android.uninstall(app);
        };

        list.appendChild(li);
    });
}