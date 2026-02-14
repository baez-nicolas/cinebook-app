# STAGE 1: BUILD (Compilar Angular)
FROM node:20-alpine AS build

WORKDIR /app

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias
RUN npm ci

# Copiar código fuente
COPY . .

# Compilar Angular en modo producción
RUN npm run build -- --configuration production

# STAGE 2: PRODUCTION (Servir con Nginx)
FROM nginx:alpine

# Copiar archivos compilados de Angular al directorio de Nginx
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copiar configuración personalizada de Nginx (como template)
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Exponer el puerto que Railway asigna dinámicamente
EXPOSE $PORT

# Nginx procesará el template y reemplazará $PORT automáticamente
CMD ["nginx", "-g", "daemon off;"]
