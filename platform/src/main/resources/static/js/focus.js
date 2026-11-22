document.addEventListener('DOMContentLoaded', function () {
    var timer = document.getElementById('timer');
    if (!timer) { return; }

    var remaining = parseInt(timer.getAttribute('data-remaining'), 10);
    if (isNaN(remaining)) { remaining = 0; }
    var wrap = document.querySelector('.ring-wrap');
    var total = wrap ? parseInt(wrap.getAttribute('data-total'), 10) : 0;
    var bar = document.getElementById('ringBar');
    var circumference = 2 * Math.PI * 112;
    if (bar) {
        bar.style.strokeDasharray = circumference;
    }

    var endAt = Date.now() + remaining * 1000;
    var completeForm = document.getElementById('completeForm');
    var hint = document.getElementById('timerHint');

    function pad(n) { return (n < 10 ? '0' : '') + n; }

    function tick() {
        var left = Math.max(0, Math.round((endAt - Date.now()) / 1000));
        timer.textContent = pad(Math.floor(left / 60)) + ':' + pad(left % 60);
        document.title = (left > 0 ? timer.textContent + ' - ' : '') + 'Focus timer';
        if (bar && total > 0) {
            bar.style.strokeDashoffset = circumference * (1 - left / total);
        }
        if (left <= 0) {
            clearInterval(handle);
            timer.classList.add('done');
            if (hint) { hint.textContent = 'Time is up. Saving your session...'; }
            if (window.detoxConfetti) { window.detoxConfetti(120, 0.35); }
            if (completeForm) {
                completeForm.classList.remove('hidden-form');
                setTimeout(function () { completeForm.submit(); }, 1200);
            }
        }
    }

    var handle = setInterval(tick, 1000);
    tick();
});
