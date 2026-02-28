# Stage 1: Build the React app
FROM node:20-alpine AS builder
WORKDIR /app
COPY react/package*.json ./
RUN npm ci
COPY react/ ./
# VITE_GOOGLE_CLIENT_ID is passed as a build arg and baked into the bundle
ARG VITE_GOOGLE_CLIENT_ID
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY nginx_render.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
