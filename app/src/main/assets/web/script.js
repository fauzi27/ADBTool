let apps = [];
let filteredApps = [];
let selected = new Set();

const $ = sel => document.querySelector(sel);
const listEl = $('#list');
const statusEl = $('#status');
const btnLoad = $('#btn_load');
const btnUninstall = $('#btn_uninstall');
const searchEl = $('#search');
const loadingEl = $('#loading');
const btnShizuku = $('#btn_shizuku');

function setStatus(msg, err = false) {
    statusEl.textContent = msg;
    statusEl.style.color = err ? "#FF2D2D" : "#1FD163";
}
function showLoading(show) {
    loadingEl.style.display = show ? "block" : "none";
}

btnLoad.onclick = () => {
    setStatus("");
    showLoading(true);
    if (window.Android && window.Android.getApps) {
        window.Android.getApps();
    }
};
btnShizuku.onclick = () => {
    setStatus("Checking Shizuku...");
    if (window.Android && window.Android.checkShizuku) {
        window.Android.checkShizuku();
    }
};
btnUninstall.onclick = () => {
    if (selected.size === 0) return;
    if (!window.Android || !window.Android.uninstall) return;
    showLoading(true);
    let pkgs = [...selected];
    let forCount = pkgs.length;
    setStatus(`Uninstalling ${forCount} app(s)...`);
    pkgs.forEach(pkg => {
        window.Android.uninstall(pkg);
    });
};

function renderList(arr) {
    listEl.innerHTML = "";
    arr.forEach((app, idx) => {
        const isChecked = selected.has(app.package);
        const card = document.createElement("div");
        card.className = "app-card " + (app.system ? "system" : "user");
        const name = document.createElement("div");
        name.textContent = app.name;
        name.className = "app-name";
        const pkg = document.createElement("div");
        pkg.textContent = app.package;
        pkg.className = "app-package";
        const checkboxWrap = document.createElement("div");
        checkboxWrap.className = "checkbox-wrap";
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = isChecked;
        checkbox.onclick = (event) => {
            if (event.target.checked) {
                selected.add(app.package);
            } else {
                selected.delete(app.package);
            }
            btnUninstall.disabled = selected.size === 0;
        };
        checkboxWrap.appendChild(checkbox);
        card.appendChild(name);
        card.appendChild(pkg);
        card.appendChild(checkboxWrap);
        listEl.appendChild(card);
    });
    btnUninstall.disabled = selected.size === 0;
}

searchEl.oninput = () => {
    const q = searchEl.value.trim().toLowerCase();
    filteredApps = !q ? apps : apps.filter(a =>
        a.name.toLowerCase().includes(q) || a.package.toLowerCase().includes(q)
    );
    renderList(filteredApps);
};

window.onAppsLoaded = function (arrJson) {
    showLoading(false);
    try {
        apps = JSON.parse(arrJson);
        apps.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));
        filteredApps = apps;
        selected.clear();
        searchEl.value = "";
        renderList(apps);
        setStatus(`Loaded ${apps.length} apps`);
    } catch (e) {
        setStatus("Failed to parse app list", true);
        filteredApps = [];
        renderList(filteredApps);
    }
};

window.onError = function (msg) {
    showLoading(false);
    setStatus(msg, true);
    renderList(filteredApps || []);
};

window.onUninstallResult = function (json) {
    let obj = {};
    try { obj = JSON.parse(json); } catch (_) { }
    showLoading(false);
    if (obj.success) {
        setStatus(`✔️ Uninstalled ${obj.package}`);
        selected.delete(obj.package);
        btnLoad.onclick();
    } else {
        setStatus(`❌ Failed to uninstall ${obj.package}:\n${obj.message || "Unknown error"}`, true);
    }
};

window.onShizukuStatus = function (json) {
    let obj = {};
    try { obj = JSON.parse(json); } catch (_) { }
    if (!obj.active) {
        setStatus("Shizuku is NOT running. Start Shizuku first!", true);
    } else if (!obj.permission) {
        setStatus("Shizuku active. Permission NOT granted.", true);
    } else {
        setStatus("Shizuku is active and permission granted.");
    }
};

window.onload = () => {
    setStatus("Ready");
    btnLoad.onclick();
};