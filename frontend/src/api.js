// URL del backend. En local usa http://localhost:8080; en Vercel se
// define la variable de entorno VITE_API_URL con la URL de Railway.
export const API_URL = (
    import.meta.env.VITE_API_URL || "http://localhost:8080"
).replace(/\/+$/, "");

// Las fotos se guardan como rutas relativas ("/archivos/..."). Esta
// función les antepone la URL del backend. También corrige las fotos
// antiguas que quedaron guardadas con "http://localhost:8080".
export function urlArchivo(ruta) {
  if (!ruta) return ruta;
  if (ruta.startsWith("data:")) return ruta;

  const relativa = ruta.replace(/^https?:\/\/localhost:8080/, "");

  return relativa.startsWith("/") ? API_URL + relativa : relativa;
}
