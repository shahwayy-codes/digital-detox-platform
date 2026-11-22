/* Minimal service worker: makes the site installable.
   Pages always come from the network (so nothing gets stale while you develop).
   If the network is down, a small offline page is shown instead. */
var CACHE = 'detox-offline-v1';

self.addEventListener('install', function (event) {
    event.waitUntil(caches.open(CACHE).then(function (cache) { return cache.add('/offline.html'); }));
    self.skipWaiting();
});

self.addEventListener('activate', function (event) {
    event.waitUntil(
        caches.keys().then(function (keys) {
            return Promise.all(keys.filter(function (k) { return k !== CACHE; }).map(function (k) { return caches.delete(k); }));
        }).then(function () { return self.clients.claim(); })
    );
});

self.addEventListener('fetch', function (event) {
    if (event.request.mode === 'navigate') {
        event.respondWith(fetch(event.request).catch(function () { return caches.match('/offline.html'); }));
    }
});
