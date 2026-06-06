# ========================================
# Stage 1: Build
# ========================================
FROM node:20-alpine AS build
WORKDIR /app
COPY dorm-web/package.json dorm-web/package-lock.json ./
RUN npm ci
COPY dorm-web/ .
RUN npm run build

# ========================================
# Stage 2: Nginx
# ========================================
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY dorm-web/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
