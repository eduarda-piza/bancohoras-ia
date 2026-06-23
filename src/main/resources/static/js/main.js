/* ============================================================
   BancoHoras – scripts globais
   ============================================================ */

// Relógio em tempo real
(function () {
  var el = document.getElementById('relogio');
  function tick() {
    if (el) el.textContent = new Date().toLocaleTimeString('pt-BR');
  }
  tick();
  setInterval(tick, 1000);
})();

document.addEventListener('DOMContentLoaded', function () {

  // Tooltips Bootstrap
  if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
    document.querySelectorAll('[data-bs-toggle="tooltip"]')
      .forEach(function (el) { new bootstrap.Tooltip(el); });
  }

  // Auto-dismiss de alertas flash após 5s
  document.querySelectorAll('.alert:not(.alert-permanent)').forEach(function (alert) {
    setTimeout(function () {
      var bsAlert = bootstrap.Alert && bootstrap.Alert.getOrCreateInstance(alert);
      if (bsAlert) bsAlert.close();
    }, 5000);
  });

  // Confirmação antes de ação destrutiva
  document.querySelectorAll('[data-confirm]').forEach(function (el) {
    el.addEventListener('click', function (e) {
      var msg = el.dataset.confirm || 'Tem certeza?';
      if (!window.confirm(msg)) e.preventDefault();
    });
  });

  // Seleção em lote: marcar/desmarcar todos via checkbox-geral
  var selectAll = document.getElementById('selectAll');
  if (selectAll) {
    selectAll.addEventListener('change', function () {
      document.querySelectorAll('.row-check').forEach(function (cb) {
        cb.checked = selectAll.checked;
      });
    });
  }

});
