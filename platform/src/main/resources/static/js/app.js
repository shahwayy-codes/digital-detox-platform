/* Detox UI behaviour ("Sunny Path"): theme, slide-out menu, active menu link, toasts, confetti,
   command palette, count-up numbers, scroll reveal, page fade, installable app (PWA).
   Everything is optional: pages still work without it. */
(function () {
    'use strict';

    var root = document.documentElement;
    var reduceMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var canHover = window.matchMedia && window.matchMedia('(hover: hover) and (pointer: fine)').matches;

    function $(sel, ctx) { return (ctx || document).querySelector(sel); }
    function $$(sel, ctx) { return Array.prototype.slice.call((ctx || document).querySelectorAll(sel)); }
    function ready(fn) {
        if (document.readyState === 'loading') { document.addEventListener('DOMContentLoaded', fn); } else { fn(); }
    }

    /* ---------- Theme (dark / light) ---------- */
    var THEME_KEY = 'detox-theme';
    function storedTheme() { try { return localStorage.getItem(THEME_KEY); } catch (e) { return null; } }
    function saveTheme(t) { try { localStorage.setItem(THEME_KEY, t); } catch (e) { /* ignore */ } }
    function systemTheme() {
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    function applyTheme(t) {
        root.setAttribute('data-theme', t);
        var meta = $('meta[name="theme-color"]');
        if (meta) { meta.setAttribute('content', t === 'dark' ? '#0D1230' : '#FFF9EF'); }
    }
    function toggleTheme(btn) {
        var next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
        applyTheme(next);
        saveTheme(next);
        if (btn) {
            btn.classList.remove('spin');
            void btn.offsetWidth;
            btn.classList.add('spin');
        }
    }
    applyTheme(storedTheme() || systemTheme());

    ready(function () {
        setupHead();
        setupTheme();
        setupShell();
        setupCountUp();
        setupHeatmapIndex();
        setupToasts();
        setupReveal();
        setupPageTransitions();
        setupPalette();
        setupCelebrations();
        setupServiceWorker();
    });

    /* ---------- Head: manifest, theme colour ---------- */
    function setupHead() {
        if (!$('meta[name="theme-color"]')) {
            var m = document.createElement('meta');
            m.name = 'theme-color';
            m.content = root.getAttribute('data-theme') === 'light' ? '#FFF9EF' : '#0D1230';
            document.head.appendChild(m);
        }
        if (!$('link[rel="manifest"]')) {
            var l = document.createElement('link');
            l.rel = 'manifest';
            l.href = '/manifest.json';
            document.head.appendChild(l);
            var icon = document.createElement('link');
            icon.rel = 'icon';
            icon.type = 'image/svg+xml';
            icon.href = '/images/icon.svg';
            document.head.appendChild(icon);
            var apple = document.createElement('link');
            apple.rel = 'apple-touch-icon';
            apple.href = '/images/icon-192.png';
            document.head.appendChild(apple);
        }
    }

    /* ---------- Theme button (also on pages without the top bar) ---------- */
    var SUN = '<svg class="ico sun" viewBox="0 0 24 24"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>';
    var MOON = '<svg class="ico moon" viewBox="0 0 24 24"><path d="M21 12.8A9 9 0 1111.2 3a7 7 0 009.8 9.8z"/></svg>';
    function setupTheme() {
        var btn = $('#themeToggle');
        if (!btn) {
            var wrap = document.createElement('div');
            wrap.className = 'floating-tools';
            btn = document.createElement('button');
            btn.type = 'button';
            btn.id = 'themeToggle';
            btn.className = 'icon-btn theme-toggle';
            btn.setAttribute('aria-label', 'Switch theme');
            btn.innerHTML = SUN + MOON;
            wrap.appendChild(btn);
            document.body.appendChild(wrap);
        }
        btn.addEventListener('click', function () { toggleTheme(btn); });
    }

    /* ---------- Shell: slide-out menu, active link, admin pages without the side panel ---------- */
    function setupShell() {
        var sidebar = $('#sidebar');
        if (!sidebar) { return; }

        if (location.pathname.indexOf('/admin') === 0) { document.body.classList.add('no-rail'); }

        // mark the menu link that matches the current page
        var best = null, bestLen = -1;
        $$('.side-nav .side-link, .side-foot .side-link', sidebar).forEach(function (a) {
            var p = a.pathname || '';
            if (!p || p === '/') { return; }
            var match = location.pathname === p || location.pathname.indexOf(p + '/') === 0;
            if (match && p.length > bestLen) { best = a; bestLen = p.length; }
        });
        if (best) {
            $$('.side-nav .side-link.active', sidebar).forEach(function (a) { a.classList.remove('active'); });
            best.classList.add('active');
        }

        // slide-out menu on small screens
        var menuBtn = $('#menuBtn');
        var scrim = $('#scrim');
        function closeMenu() { document.body.classList.remove('drawer-open'); }
        if (menuBtn) {
            menuBtn.addEventListener('click', function () { document.body.classList.toggle('drawer-open'); });
        }
        if (scrim) { scrim.addEventListener('click', closeMenu); }
        document.addEventListener('keydown', function (e) { if (e.key === 'Escape') { closeMenu(); } });
        $$('.side-link', sidebar).forEach(function (a) { a.addEventListener('click', closeMenu); });
    }

    /* ---------- Count-up numbers in stat boxes ---------- */
    function setupCountUp() {
        $$('.stat strong').forEach(function (el) {
            if (el.hasAttribute('data-static') || reduceMotion) { return; }
            var target = parseInt(el.textContent, 10);
            if (isNaN(target) || target <= 0 || String(target) !== el.textContent.trim()) { return; }
            var start = null;
            var duration = 900;
            el.textContent = '0';
            function step(ts) {
                if (start === null) { start = ts; }
                var p = Math.min((ts - start) / duration, 1);
                el.textContent = Math.round(target * (1 - Math.pow(1 - p, 4)));
                if (p < 1) { requestAnimationFrame(step); } else { el.textContent = target; }
            }
            requestAnimationFrame(step);
        });
    }

    /* ---------- Heatmap: stagger the squares ---------- */
    function setupHeatmapIndex() {
        $$('.heatmap .heat-cell').forEach(function (c, i) { c.style.setProperty('--i', i); });
    }

    /* ---------- Toasts for flash messages ---------- */
    var toastBox = null;
    function setupToasts() {
        var alerts = $$('.container.wide > .alert').filter(function (a) {
            return a.textContent.indexOf('Temporary password') === -1 && !a.hasAttribute('data-keep');
        });
        if (!alerts.length) { return; }
        toastBox = document.createElement('div');
        toastBox.className = 'toasts';
        toastBox.setAttribute('role', 'status');
        toastBox.setAttribute('aria-live', 'polite');
        document.body.appendChild(toastBox);

        alerts.forEach(function (a, i) {
            var isError = a.classList.contains('danger');
            var ms = isError ? 8000 : 6000;
            a.style.setProperty('--t', ms + 'ms');
            a.style.animationDelay = (i * 90) + 'ms';
            toastBox.appendChild(a);
            var done = false;
            function close() {
                if (done) { return; }
                done = true;
                a.classList.add('leaving');
                setTimeout(function () { if (a.parentNode) { a.parentNode.removeChild(a); } }, 300);
            }
            a.addEventListener('click', close);
            setTimeout(close, ms);
        });
    }

    /* ---------- Reveal items that start below the screen ---------- */
    function setupReveal() {
        if (reduceMotion || !('IntersectionObserver' in window)) { return; }
        var items = $$('.card, .stat, .badge-card, table, .day-row, .coach, .heatmap-wrap, .certificate');
        var vh = window.innerHeight || 800;
        var below = items.filter(function (el) { return el.getBoundingClientRect().top > vh * 0.95; });
        if (!below.length) { return; }
        var io = new IntersectionObserver(function (entries) {
            entries.forEach(function (en) {
                if (en.isIntersecting) {
                    en.target.classList.add('in');
                    io.unobserve(en.target);
                }
            });
        }, { threshold: 0.08 });
        below.forEach(function (el) { el.classList.add('pre-reveal'); io.observe(el); });
    }

    /* ---------- Soft fade between pages ---------- */
    function setupPageTransitions() {
        window.addEventListener('pageshow', function () { document.body.classList.remove('leaving'); });
        if (reduceMotion) { return; }
        document.addEventListener('click', function (e) {
            if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) { return; }
            var a = e.target.closest ? e.target.closest('a[href]') : null;
            if (!a || a.target === '_blank' || a.hasAttribute('download')) { return; }
            var href = a.getAttribute('href');
            if (!href || href.charAt(0) === '#' || /^(mailto:|tel:|javascript:)/.test(href)) { return; }
            if (a.origin !== location.origin || href.indexOf('/export/') !== -1 || /\.csv(\?|$)/.test(href)) { return; }
            e.preventDefault();
            document.body.classList.add('leaving');
            setTimeout(function () { location.href = a.href; }, 170);
        });
    }

    /* ---------- Confetti ---------- */
    var confettiCanvas = null;
    function confetti(count, originY) {
        if (reduceMotion) { return; }
        if (!confettiCanvas) {
            confettiCanvas = document.createElement('canvas');
            confettiCanvas.id = 'confetti';
            document.body.appendChild(confettiCanvas);
        }
        var cv = confettiCanvas;
        var ctx = cv.getContext('2d');
        var w = cv.width = window.innerWidth;
        var h = cv.height = window.innerHeight;
        var cs = getComputedStyle(root);
        var colors = [cs.getPropertyValue('--brand').trim(), cs.getPropertyValue('--sun').trim(),
            cs.getPropertyValue('--good').trim(), cs.getPropertyValue('--accent').trim(), '#FF6B8B', '#FFFFFF'];
        var parts = [];
        for (var i = 0; i < count; i++) {
            var angle = (Math.random() * Math.PI) + Math.PI;
            var speed = 6 + Math.random() * 9;
            parts.push({
                x: w / 2 + (Math.random() - 0.5) * 120,
                y: h * (originY || 0.35),
                vx: Math.cos(angle) * speed * (0.6 + Math.random()),
                vy: Math.sin(angle) * speed * 0.9,
                size: 5 + Math.random() * 7,
                rot: Math.random() * 6.28,
                vr: (Math.random() - 0.5) * 0.4,
                color: colors[i % colors.length] || '#2F6BFF',
                life: 1
            });
        }
        function frame() {
            ctx.clearRect(0, 0, w, h);
            var alive = 0;
            parts.forEach(function (p) {
                p.vy += 0.28;
                p.vx *= 0.99;
                p.x += p.vx;
                p.y += p.vy;
                p.rot += p.vr;
                p.life -= 0.006;
                if (p.life > 0 && p.y < h + 20) {
                    alive++;
                    ctx.save();
                    ctx.globalAlpha = Math.max(0, Math.min(1, p.life * 1.6));
                    ctx.translate(p.x, p.y);
                    ctx.rotate(p.rot);
                    ctx.fillStyle = p.color;
                    ctx.fillRect(-p.size / 2, -p.size / 4, p.size, p.size / 2);
                    ctx.restore();
                }
            });
            if (alive > 0) {
                requestAnimationFrame(frame);
            } else {
                ctx.clearRect(0, 0, w, h);
                if (cv.parentNode) { cv.parentNode.removeChild(cv); }
                confettiCanvas = null;
            }
        }
        requestAnimationFrame(frame);
    }
    window.detoxConfetti = confetti;

    function setupCelebrations() {
        var celebrate = /well done|goal met|reached|finished|completed|congrat|great teamwork|nice work|you earned|badge/i;
        var fire = false;
        $$('.toasts .alert.success').forEach(function (a) { if (celebrate.test(a.textContent)) { fire = true; } });
        if ($('[data-celebrate]') || $('.certificate')) { fire = true; }
        if (fire) { setTimeout(function () { confetti(130, 0.3); }, 350); }
    }

    /* ---------- Command palette (Ctrl+K or /) ---------- */
    var deferredInstall = null;
    window.addEventListener('beforeinstallprompt', function (e) {
        e.preventDefault();
        deferredInstall = e;
    });

    function setupPalette() {
        var brand = $('.brand');
        if (!$('.sidebar') || !brand) { return; }
        var base = (brand.getAttribute('href') || '/dashboard').replace(/dashboard$/, '');
        if (base.charAt(base.length - 1) !== '/') { base += '/'; }
        var isAdmin = !!$('.sidebar a[href$="/admin/challenges"]');

        var items = [
            { t: 'Home', s: 'Dashboard', go: base + 'dashboard' },
            { t: 'Challenges', s: 'Browse and join', go: base + 'challenges' },
            { t: 'Daily check-in', s: 'Log today', go: base + 'checkin' },
            { t: 'My progress', s: 'Streaks and heatmap', go: base + 'progress' },
            { t: 'Weekly insights', s: 'Report, mood, triggers', go: base + 'insights' },
            { t: 'Focus timer', s: 'Phone-down session', go: base + 'focus' },
            { t: 'Friends', s: 'Groups, leaderboard, team goal', go: base + 'groups' },
            { t: 'Badges and points', s: 'Achievements', go: base + 'achievements' },
            { t: 'Reflections', s: 'Journal', go: base + 'reflections' },
            { t: 'Check-in history', s: 'Export CSV', go: base + 'history' },
            { t: 'Tips and offline ideas', s: 'Inspiration', go: base + 'tips' },
            { t: 'My profile', s: 'Goal and password', go: base + 'profile' }
        ];
        if (isAdmin) {
            items.push({ t: 'Admin panel', s: 'Manage', go: base + 'admin/challenges' });
            items.push({ t: 'Admin analytics', s: 'Platform charts', go: base + 'admin/analytics' });
        }
        items.push({ t: 'Switch theme', s: 'Dark / light', run: function () { toggleTheme($('#themeToggle')); } });
        items.push({ t: 'Celebrate', s: 'Confetti', run: function () { confetti(140, 0.4); } });
        var logout = $('.menu-logout');
        if (logout) { items.push({ t: 'Log out', s: 'End session', run: function () { logout.click(); } }); }

        var overlay = null, input = null, list = null, shown = [], idx = 0;

        function build() {
            overlay = document.createElement('div');
            overlay.className = 'palette';
            overlay.setAttribute('role', 'dialog');
            overlay.setAttribute('aria-label', 'Quick search');
            overlay.innerHTML = '<div class="palette-box"><input type="text" placeholder="Jump to a page or action..." aria-label="Search" autocomplete="off">' +
                '<ul class="palette-list"></ul><div class="palette-foot">Up and down to move, Enter to open, Esc to close</div></div>';
            document.body.appendChild(overlay);
            input = $('input', overlay);
            list = $('.palette-list', overlay);
            overlay.addEventListener('click', function (e) { if (e.target === overlay) { close(); } });
            input.addEventListener('input', render);
            input.addEventListener('keydown', function (e) {
                if (e.key === 'ArrowDown') { idx = Math.min(shown.length - 1, idx + 1); mark(); e.preventDefault(); }
                else if (e.key === 'ArrowUp') { idx = Math.max(0, idx - 1); mark(); e.preventDefault(); }
                else if (e.key === 'Enter') { choose(shown[idx]); e.preventDefault(); }
            });
        }
        function render() {
            var q = input.value.trim().toLowerCase();
            var all = items.slice();
            if (deferredInstall) { all.push({ t: 'Install app', s: 'Add to home screen', run: function () { deferredInstall.prompt(); deferredInstall = null; } }); }
            shown = all.filter(function (it) { return !q || (it.t + ' ' + it.s).toLowerCase().indexOf(q) !== -1; });
            idx = 0;
            list.innerHTML = '';
            shown.forEach(function (it, i) {
                var li = document.createElement('li');
                li.className = 'palette-item';
                li.innerHTML = '<span></span><small></small>';
                li.firstChild.textContent = it.t;
                li.lastChild.textContent = it.s;
                li.addEventListener('mousemove', function () { idx = i; mark(); });
                li.addEventListener('click', function () { choose(it); });
                list.appendChild(li);
            });
            if (!shown.length) {
                var empty = document.createElement('li');
                empty.className = 'palette-item';
                empty.textContent = 'Nothing found';
                list.appendChild(empty);
            }
            mark();
        }
        function mark() {
            $$('.palette-item', list).forEach(function (li, i) {
                li.classList.toggle('active', i === idx);
                if (i === idx && li.scrollIntoView) { li.scrollIntoView({ block: 'nearest' }); }
            });
        }
        function choose(it) {
            if (!it) { return; }
            close();
            if (it.run) { it.run(); } else { document.body.classList.add('leaving'); setTimeout(function () { location.href = it.go; }, 120); }
        }
        function open() {
            if (!overlay) { build(); }
            overlay.classList.add('open');
            input.value = '';
            render();
            input.focus();
        }
        function close() { if (overlay) { overlay.classList.remove('open'); } }
        function isOpen() { return overlay && overlay.classList.contains('open'); }

        var btn = $('#paletteBtn');
        if (btn) { btn.addEventListener('click', open); }
        document.addEventListener('keydown', function (e) {
            var tag = (e.target.tagName || '').toLowerCase();
            var typing = tag === 'input' || tag === 'textarea' || tag === 'select' || e.target.isContentEditable;
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') { e.preventDefault(); isOpen() ? close() : open(); }
            else if (e.key === '/' && !typing && !isOpen()) { e.preventDefault(); open(); }
            else if (e.key === 'Escape' && isOpen()) { close(); }
        });
    }

    /* ---------- Installable app (service worker) ---------- */
    function setupServiceWorker() {
        if (!('serviceWorker' in navigator)) { return; }
        if (location.protocol !== 'https:' && location.hostname !== 'localhost' && location.hostname !== '127.0.0.1') { return; }
        window.addEventListener('load', function () {
            navigator.serviceWorker.register('/sw.js').catch(function () { /* ignore */ });
        });
    }
})();
