(function() {
  const canvas = document.getElementById('priceChart');
  if (!canvas) return; // Only run on /charts page

  const ctx = canvas.getContext('2d');
  const symbolSelect = document.getElementById('symbolSelect');
  const timeframeButtons = Array.from(document.querySelectorAll('.timeframe'));
  const currentSymbolEl = document.getElementById('currentSymbol');
  const currentPriceEl = document.getElementById('currentPrice');

  let chart = null;
  let currentSymbol = canvas.getAttribute('data-selected-symbol') || (symbolSelect ? symbolSelect.value : 'AAPL');
  let currentPoints = 60;

  function fmtTime(iso) {
    try {
      const d = new Date(iso);
      return d.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
    } catch (e) {
      return iso;
    }
  }

  function fmtPrice(n) {
    const num = Number(n);
    if (Number.isNaN(num)) return String(n);
    return num.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  async function loadChartData(symbol, points) {
    try {
      const res = await fetch(`/api/chart-data?symbol=${encodeURIComponent(symbol)}&points=${encodeURIComponent(points)}`, { credentials: 'same-origin' });
      if (!res.ok) {
        // Try to parse error body
        try {
          const err = await res.json();
          // eslint-disable-next-line no-console
          console.warn('Chart data error:', err);
        } catch (_) {}
        return;
      }
      const payload = await res.json();
      const pts = Array.isArray(payload.points) ? payload.points : [];
      const labels = pts.map(p => fmtTime(p.timestamp));
      const data = pts.map(p => Number(p.price));
      if (!Array.isArray(data) || data.length === 0) return;

      // Update summary under chart
      if (currentSymbolEl) currentSymbolEl.textContent = payload.symbol || symbol;
      if (currentPriceEl) currentPriceEl.textContent = fmtPrice(payload.currentPrice);

      if (!chart) {
        chart = new Chart(ctx, {
          type: 'line',
          data: {
            labels: labels,
            datasets: [{
              label: symbol,
              data: data,
              borderColor: 'rgba(80, 200, 120, 1)',
              backgroundColor: 'rgba(80, 200, 120, 0.1)',
              borderWidth: 2,
              pointRadius: 0,
              tension: 0.25,
              fill: false
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
            plugins: {
              legend: { display: false },
              tooltip: {
                callbacks: {
                  label: function(context) {
                    const v = context.parsed.y;
                    return ' ' + fmtPrice(v);
                  }
                }
              }
            },
            scales: {
              x: { display: true, ticks: { maxTicksLimit: 8 } },
              y: { display: true, ticks: { callback: (v)=> fmtPrice(v) } }
            }
          }
        });
        // Make parent card give canvas some height if not already
        if (canvas.parentElement && !canvas.style.height) {
          canvas.style.height = '360px';
        }
      } else {
        chart.data.labels = labels;
        chart.data.datasets[0].data = data;
        chart.data.datasets[0].label = symbol;
        chart.update('none');
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.warn('Failed to load chart data', e);
    }
  }

  function setActiveTimeframe(btn) {
    timeframeButtons.forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
  }

  // Events
  if (symbolSelect) {
    symbolSelect.addEventListener('change', () => {
      currentSymbol = symbolSelect.value;
      loadChartData(currentSymbol, currentPoints);
    });
  }
  timeframeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const pts = parseInt(btn.getAttribute('data-points'), 10) || 60;
      currentPoints = pts;
      setActiveTimeframe(btn);
      loadChartData(currentSymbol, currentPoints);
    });
  });

  // Initial load
  loadChartData(currentSymbol, currentPoints);

  // Auto-refresh every 15s
  setInterval(() => loadChartData(currentSymbol, currentPoints), 15000);
})();
