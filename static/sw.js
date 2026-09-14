const CACHE = "personal-cyber-ai-v2";
const APP_SHELL = ["/", "/manifest.webmanifest", "/static/icon.svg"];

self.addEventListener("install", (event) => {
  event.waitUntil(caches.open(CACHE).then((cache) => cache.addAll(APP_SHELL)));
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(caches.keys().then((keys) => Promise.all(keys.filter((key) => key !== CACHE).map((key) => caches.delete(key)))));
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  const request = event.request;
  const url = new URL(request.url);
  if (request.method === "POST" && url.pathname === "/share") {
    event.respondWith((async () => {
      const data = await request.formData();
      const title = data.get("title") || "";
      const text = data.get("text") || "";
      const sharedUrl = data.get("url") || "";
      const shared = [title, text, sharedUrl].filter(Boolean).join("\n");
      const target = new URL("/", self.location.origin);
      target.searchParams.set("shared_text", shared);
      return Response.redirect(target.href, 303);
    })());
    return;
  }
  if (request.method !== "GET" || url.pathname.startsWith("/api/")) return;
  event.respondWith(fetch(request).then((response) => {
    const copy = response.clone();
    caches.open(CACHE).then((cache) => cache.put(request, copy));
    return response;
  }).catch(() => caches.match(request).then((cached) => cached || caches.match("/"))));
});
