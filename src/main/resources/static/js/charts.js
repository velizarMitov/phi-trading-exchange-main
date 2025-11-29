(function () {
  // Multiple compact charts inside cards on /charts page
  const canvases = Array.from(document.querySelectorAll('canvas.market-chart-canvas'));
  if (!canvases.length) return;

  const charts = new Map();
  const DEFAULT_POINTS = 60;

  function fmtTime(iso) {
    try {
      const d = new Date(iso);
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch (e) {
      return iso;
    }
  }

  function fmtPrice(n) {
    const num = Number(n);
    if (Number.isNaN(num)) return String(n);
    return num.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  async function fetchSeries(symbol, points) {
    const res = await fetch(`/api/chart-data?symbol=${encodeURIComponent(symbol)}&points=${encodeURIComponent(points)}`, { credentials: 'same-origin' });
    if (!res.ok) return null;
    try { return await res.json(); } catch (_) { return null; }
  }

  function buildConfig(symbol, labels, data) {
    return {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: symbol,
          data,
          borderColor: 'rgba(80, 200, 120, 1)',
          backgroundColor: 'rgba(80, 200, 120, 0.12)',
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
              label: (ctx) => ' ' + fmtPrice(ctx.parsed.y)
            }
          }
        },
        scales: {
          x: {
            ticks: { maxTicksLimit: 4 },
            grid: { display: false }
          },
          y: {
            ticks: { maxTicksLimit: 4, callback: (v) => fmtPrice(v) },
            grid: { color: 'rgba(148, 163, 184, 0.15)' }
          }
        }
      }
    };
  }

  async function loadCanvas(canvas) {
    const symbol = canvas.getAttribute('data-symbol');
    if (!symbol) return;
    const payload = await fetchSeries(symbol, DEFAULT_POINTS);
    if (!payload || !Array.isArray(payload.points) || !payload.points.length) return;
    const labels = payload.points.map(p => fmtTime(p.timestamp));
    const data = payload.points.map(p => Number(p.price));

    const ctx = canvas.getContext('2d');
    const chart = new Chart(ctx, buildConfig(symbol, labels, data));
    charts.set(canvas, { symbol, chart });
  }

  async function refreshAll() {
    for (const [canvas, meta] of charts.entries()) {
      const payload = await fetchSeries(meta.symbol, DEFAULT_POINTS);
      if (!payload || !Array.isArray(payload.points) || !payload.points.length) continue;
      const labels = payload.points.map(p => fmtTime(p.timestamp));
      const data = payload.points.map(p => Number(p.price));
      meta.chart.data.labels = labels;
      meta.chart.data.datasets[0].data = data;
      meta.chart.update('none');
    }
  }

  // Initialize all charts
  Promise.all(canvases.map(loadCanvas)).then(() => {
    // Periodic refresh
    setInterval(refreshAll, 15000);
  });
})();
