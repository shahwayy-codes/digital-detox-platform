document.addEventListener('DOMContentLoaded', function () {

    // Count-up animation for the numbers in the stat boxes
    document.querySelectorAll('.stat strong').forEach(function (el) {
        var target = parseInt(el.textContent, 10);
        if (isNaN(target) || target <= 0) { return; }
        var start = null;
        var duration = 800;
        el.textContent = '0';
        function step(ts) {
            if (start === null) { start = ts; }
            var p = Math.min((ts - start) / duration, 1);
            el.textContent = Math.round(target * (1 - Math.pow(1 - p, 3)));
            if (p < 1) { requestAnimationFrame(step); }
        }
        requestAnimationFrame(step);
    });

    // Fade out success messages after a few seconds (not the temporary password)
    document.querySelectorAll('.alert.success').forEach(function (el) {
        if (el.textContent.indexOf('Temporary password') !== -1) { return; }
        setTimeout(function () { el.classList.add('hide'); }, 5000);
    });

    // Close the user menu when clicking elsewhere
    document.addEventListener('click', function (e) {
        document.querySelectorAll('details.usermenu[open]').forEach(function (d) {
            if (!d.contains(e.target)) { d.removeAttribute('open'); }
        });
    });
});
