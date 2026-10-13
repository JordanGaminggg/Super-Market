// ─── API Helper ───────────────────────────────────────────────────────────────
async function api(path, options = {}) {
  const res = await fetch('/api' + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  if (!res.ok) {
    let msg = 'Something went wrong';
    try { msg = (await res.json()).message || msg; } catch (e) {}
    throw new Error(msg);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

// ─── Formatters ───────────────────────────────────────────────────────────────
const money = n => '₹' + Number(n || 0).toFixed(2);

function fmtDate(iso) {
  return new Date(iso).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

function expiryBadge(d) {
  if (!d) return '';
  const days = Math.ceil((new Date(d) - new Date()) / 86400000);
  if (days < 0)  return `<span class="badge badge-expired">Expired</span>`;
  if (days <= 30) return `<span class="badge badge-expiring">Exp: ${d}</span>`;
  return `<span class="badge badge-ok">Exp: ${d}</span>`;
}

function payBadge(mode) {
  const cls = { Cash: 'badge-cash', UPI: 'badge-upi', Card: 'badge-card' }[mode] || 'badge-cash';
  return `<span class="badge ${cls}">${mode || 'Cash'}</span>`;
}

// ─── Toast Notifications ──────────────────────────────────────────────────────
function showMsg(text, ok = true) {
  // legacy banner (used on pages that have #msg)
  const box = document.getElementById('msg');
  if (box) {
    box.textContent = text;
    box.className = 'msg ' + (ok ? 'ok' : 'err');
    setTimeout(() => (box.className = 'msg'), 4000);
  }
  toast(text, ok ? 'ok' : 'err');
}

function toast(text, type = 'ok') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  const t = document.createElement('div');
  t.className = `toast toast-${type}`;
  t.innerHTML = `<span class="toast-icon">${type === 'ok' ? '✓' : '✕'}</span><span>${text}</span>`;
  container.appendChild(t);
  // trigger animation
  requestAnimationFrame(() => t.classList.add('toast-show'));
  setTimeout(() => {
    t.classList.remove('toast-show');
    t.classList.add('toast-hide');
    setTimeout(() => t.remove(), 400);
  }, 3500);
}

// ─── Custom Confirm Modal ─────────────────────────────────────────────────────
function confirm2(message, onConfirm) {
  let overlay = document.getElementById('modal-overlay');
  if (!overlay) {
    overlay = document.createElement('div');
    overlay.id = 'modal-overlay';
    overlay.innerHTML = `
      <div class="modal-box">
        <p class="modal-msg" id="modal-msg"></p>
        <div class="modal-actions">
          <button class="gray" id="modal-cancel">Cancel</button>
          <button class="danger" id="modal-ok">Delete</button>
        </div>
      </div>`;
    document.body.appendChild(overlay);
  }
  document.getElementById('modal-msg').textContent = message;
  overlay.classList.add('modal-open');
  const close = () => overlay.classList.remove('modal-open');
  document.getElementById('modal-cancel').onclick = close;
  document.getElementById('modal-ok').onclick = () => { close(); onConfirm(); };
}

// ─── Navigation ───────────────────────────────────────────────────────────────
function renderNav(active) {
  const links = [
    ['index.html',    '🏠 Dashboard'],
    ['products.html', '📦 Products'],
    ['billing.html',  '🧾 Billing'],
    ['bills.html',    '📋 Bill History'],
    ['reports.html',  '📊 Reports'],
  ];
  document.getElementById('nav').innerHTML =
    '<span class="brand">🛒 SuperMart</span>' +
    links.map(l => `<a href="${l[0]}" class="${l[1] === active ? 'active' : ''}">${l[1]}</a>`).join('');
}

// ─── Invoice Builder ──────────────────────────────────────────────────────────
function invoiceHtml(b) {
  const d = fmtDate(b.billDate);
  const rows = b.items.map(i => `<tr><td>${i.productName}</td><td class="num">${i.quantity}</td>
      <td class="num">${Number(i.price).toFixed(2)}</td><td class="num">${Number(i.lineTotal).toFixed(2)}</td></tr>`).join('');
  return `<h3>SUPERMART</h3><p class="c">Kalyan, Maharashtra</p><hr>
    <p>Bill No: <b>#${b.id}</b><br>Date: ${d}<br>Customer: ${b.customerName}
    ${b.customerPhone ? '<br>Phone: ' + b.customerPhone : ''}<br>Payment: ${b.paymentMode}</p><hr>
    <table><tr><th>Item</th><th class="num">Qty</th><th class="num">Rate</th><th class="num">Amt</th></tr>${rows}</table><hr>
    <div class="totals" style="margin:0;border:0">Subtotal: ${money(b.subtotal)}<br>GST: ${money(b.gstAmount)}<br>
    Discount: -${money(b.discount)}<br><b>TOTAL: ${money(b.grandTotal)}</b></div><hr>
    <p class="c">Thank you! Visit again.</p>`;
}
