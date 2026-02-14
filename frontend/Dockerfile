# STAGE 1: BUILD (Compilar Angular)
FROM node:18-alpine AS build

# Establecer directorio de trabajo
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

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto 80
EXPOSE 80

# Comando para iniciar Nginx
CMD ["nginx", "-g", "daemon off;"]
